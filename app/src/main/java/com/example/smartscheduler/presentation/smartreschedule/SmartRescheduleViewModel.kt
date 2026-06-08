package com.example.smartscheduler.presentation.smartreschedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartscheduler.R
import com.example.smartscheduler.domain.model.AppSettings
import com.example.smartscheduler.domain.model.DiffItem
import com.example.smartscheduler.domain.model.Event
import com.example.smartscheduler.domain.model.ReschedulePreview
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Task
import com.example.smartscheduler.domain.model.UnscheduledTask
import com.example.smartscheduler.domain.repository.EventRepository
import com.example.smartscheduler.domain.repository.SettingsRepository
import com.example.smartscheduler.domain.repository.TaskRepository
import com.example.smartscheduler.domain.usecase.ApplyRescheduleUseCase
import com.example.smartscheduler.domain.usecase.PreviewRescheduleDayUseCase
import com.example.smartscheduler.domain.usecase.ReschedulePreviewResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class SmartRescheduleViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    taskRepository: TaskRepository,
    eventRepository: EventRepository,
    settingsRepository: SettingsRepository,
    private val previewRescheduleDayUseCase: PreviewRescheduleDayUseCase,
    private val applyRescheduleUseCase: ApplyRescheduleUseCase,
) : ViewModel() {

    private val currentDate: LocalDate = LocalDate.now()
    private val startOfDay: LocalDateTime = currentDate.atStartOfDay()
    private val endOfDay: LocalDateTime = currentDate.plusDays(1).atStartOfDay()
    private var currentPreview: ReschedulePreview? = null
    private val deselectedBacklogTaskIds = MutableStateFlow<Set<String>>(emptySet())
    private val rejectedTaskIds = MutableStateFlow<Set<String>>(emptySet())
    private val isApplying = MutableStateFlow(false)
    private val applyError = MutableStateFlow<String?>(null)
    private val retryRequests = MutableStateFlow(0)

    private val planningInputs = combine(
        taskRepository.getDayTasksStream(currentDate),
        taskRepository.getUnallocatedTasksStream(),
        eventRepository.observeEvents(startOfDay, endOfDay),
        settingsRepository.settingsStream,
        retryRequests,
    ) { dayTasks, backlog, events, settings, _ ->
        PlanningInputs(
            dayTasks = dayTasks,
            backlog = backlog,
            events = events,
            settings = settings,
        )
    }

    val uiState: StateFlow<SmartRescheduleUiState> = combine(
        planningInputs,
        deselectedBacklogTaskIds,
        rejectedTaskIds,
        isApplying,
        applyError,
    ) { inputs, deselectedBacklogTaskIds, rejectedTaskIds, isApplying, applyError ->
        buildUiState(
            inputs = inputs,
            deselectedBacklogTaskIds = deselectedBacklogTaskIds,
            rejectedTaskIds = rejectedTaskIds,
            isApplying = isApplying,
            applyError = applyError,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SmartRescheduleUiState.Loading(currentDate),
    )

    private val _effects = MutableSharedFlow<SmartRescheduleEffect>()
    val effects: SharedFlow<SmartRescheduleEffect> = _effects.asSharedFlow()

    fun handleAction(action: SmartRescheduleAction) {
        when (action) {
            SmartRescheduleAction.ApplySchedule -> applySchedule()
            SmartRescheduleAction.Cancel -> cancel()
            SmartRescheduleAction.ResetSession -> resetSession()
            is SmartRescheduleAction.ToggleBacklogCandidate -> toggleBacklogCandidate(
                action.taskId,
                action.selected,
            )
            is SmartRescheduleAction.RejectChange -> rejectChange(action.taskId)
            is SmartRescheduleAction.RestoreChange -> restoreChange(action.taskId)
            SmartRescheduleAction.Retry -> {
                applyError.value = null
                retryRequests.update { it + 1 }
            }
        }
    }

    private suspend fun buildUiState(
        inputs: PlanningInputs,
        deselectedBacklogTaskIds: Set<String>,
        rejectedTaskIds: Set<String>,
        isApplying: Boolean,
        applyError: String?,
    ): SmartRescheduleUiState {
        if (applyError != null) {
            return SmartRescheduleUiState.Error(currentDate, applyError)
        }

        val backlogCandidates = inputs.backlog.map { task ->
            task.toBacklogCandidateUiModel(
                isSelected = task.id !in deselectedBacklogTaskIds,
            )
        }

        if (backlogCandidates.isEmpty()) {
            currentPreview = null
            return SmartRescheduleUiState.Empty(currentDate)
        }

        val selectedBacklog = inputs.backlog.filterSelectedForSmartReschedule(deselectedBacklogTaskIds)

        if (!inputs.settings.isValidWorkday) {
            currentPreview = null
            return SmartRescheduleUiState.Error(
                currentDate = currentDate,
                message = context.getString(R.string.smart_reschedule_workday_error),
            )
        }

        return try {
            when (val result = previewRescheduleDayUseCase.fromSnapshot(
                date = currentDate,
                workDayStart = inputs.settings.workDayStart,
                workDayEnd = inputs.settings.workDayEnd,
                tasksOnToday = inputs.dayTasks,
                eventsOnToday = inputs.events,
                backlog = selectedBacklog,
            )) {
                is ReschedulePreviewResult.Failure -> {
                    currentPreview = null
                    SmartRescheduleUiState.Error(currentDate, result.message)
                }

                is ReschedulePreviewResult.Success -> {
                    currentPreview = result.preview
                    renderSuccess(
                        preview = result.preview,
                        backlogCandidates = backlogCandidates,
                        rejectedTaskIds = rejectedTaskIds,
                        isApplying = isApplying,
                    )
                }
            }
        } catch (error: Exception) {
            currentPreview = null
            SmartRescheduleUiState.Error(
                currentDate = currentDate,
                message = error.message ?: context.getString(R.string.smart_reschedule_calculate_error),
            )
        }
    }

    private fun toggleBacklogCandidate(taskId: String, selected: Boolean) {
        applyError.value = null
        deselectedBacklogTaskIds.update { currentIds ->
            if (selected) currentIds - taskId else currentIds + taskId
        }
        rejectedTaskIds.update { currentIds -> currentIds - taskId }
    }

    private fun rejectChange(taskId: String) {
        applyError.value = null
        val preview = currentPreview ?: return

        if (!preview.canRejectChange(taskId)) return

        rejectedTaskIds.update { it + taskId }
    }

    private fun restoreChange(taskId: String) {
        applyError.value = null
        rejectedTaskIds.update { it - taskId }
    }

    private fun applySchedule() {
        val preview = currentPreview ?: return

        viewModelScope.launch {
            applyError.value = null
            isApplying.value = true

            runCatching {
                applyRescheduleUseCase(
                    buildSmartRescheduleTasksToApply(
                        preview = preview,
                        rejectedTaskIds = rejectedTaskIds.value,
                    )
                )
            }.onSuccess {
                isApplying.value = false
                resetSession()
                _effects.emit(SmartRescheduleEffect.NavigateBack)
            }.onFailure { error ->
                isApplying.value = false
                applyError.value = error.message ?: context.getString(R.string.smart_reschedule_apply_error)
            }
        }
    }

    private fun cancel() {
        resetSession()
        navigateBack()
    }

    private fun resetSession() {
        currentPreview = null
        deselectedBacklogTaskIds.value = emptySet()
        rejectedTaskIds.value = emptySet()
        isApplying.value = false
        applyError.value = null
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effects.emit(SmartRescheduleEffect.NavigateBack)
        }
    }

    private fun renderSuccess(
        preview: ReschedulePreview,
        backlogCandidates: List<SmartRescheduleBacklogCandidateUiModel>,
        rejectedTaskIds: Set<String>,
        isApplying: Boolean = false,
    ): SmartRescheduleUiState {
        val changes = preview.diff
            .filterNot { it is DiffItem.Unchanged }
            .map { it.toUiModel(preview, rejectedTaskIds) }
        val activeChanges = changes.filterNot { it.isRejected }

        return SmartRescheduleUiState.Success(
            currentDate = preview.date,
            backlogCandidates = backlogCandidates,
            summary = SmartRescheduleSummaryUiModel(
                movedCount = activeChanges.count { it.type == SmartRescheduleChangeType.MOVED },
                addedCount = activeChanges.count { it.type == SmartRescheduleChangeType.ADDED },
                deferredCount = activeChanges.count { it.type == SmartRescheduleChangeType.DEFERRED },
            ),
            changes = changes,
            isApplying = isApplying,
        )
    }

    private fun UnscheduledTask.toBacklogCandidateUiModel(
        isSelected: Boolean,
    ): SmartRescheduleBacklogCandidateUiModel {
        return SmartRescheduleBacklogCandidateUiModel(
            taskId = id,
            taskName = name,
            priority = priority,
            duration = duration,
            deadline = deadline,
            isSelected = isSelected,
        )
    }

    private fun DiffItem.toUiModel(
        preview: ReschedulePreview,
        rejectedTaskIds: Set<String>,
    ): SmartRescheduleChangeUiModel {
        val proposedTask = preview.proposedSchedule
            .filterIsInstance<ScheduledTask>()
            .firstOrNull { it.id == taskId }
        val oldTask = preview.oldPlan.firstOrNull { it.id == taskId }
        val backlogTask = preview.backlog.firstOrNull { it.id == taskId }
        val unallocatedTask = preview.proposedUnallocated.firstOrNull { it.id == taskId }
        val sourceTask = proposedTask ?: oldTask ?: backlogTask ?: unallocatedTask
        val changeType = when (this) {
            is DiffItem.Added -> SmartRescheduleChangeType.ADDED
            is DiffItem.Moved -> SmartRescheduleChangeType.MOVED
            is DiffItem.Evicted -> SmartRescheduleChangeType.DEFERRED
            is DiffItem.Deferred -> SmartRescheduleChangeType.DEFERRED
            is DiffItem.Unchanged -> SmartRescheduleChangeType.UNCHANGED
        }

        return SmartRescheduleChangeUiModel(
            taskId = taskId,
            taskName = taskName,
            type = changeType,
            priority = sourceTask?.priority,
            duration = proposedTask?.duration ?: oldTask?.duration ?: backlogTask?.duration ?: unallocatedTask?.duration,
            deadline = sourceTask?.deadline,
            oldStartTime = when (this) {
                is DiffItem.Deferred -> oldStartTime
                else -> oldTask?.startTime
            },
            oldEndTime = oldTask?.endTime,
            newStartTime = when (this) {
                is DiffItem.Deferred -> newStartTime
                else -> proposedTask?.startTime
            },
            newEndTime = when (this) {
                is DiffItem.Deferred -> newEndTime
                else -> proposedTask?.endTime
            },
            reason = when (this) {
                is DiffItem.Deferred -> context.getString(R.string.smart_reschedule_reason_future_slot)
                is DiffItem.Evicted -> context.getString(R.string.smart_reschedule_reason_no_fit_today)
                else -> buildReason(changeType, sourceTask?.deadline)
            },
            isRejected = taskId in rejectedTaskIds,
        )
    }

    private fun buildReason(
        changeType: SmartRescheduleChangeType,
        deadline: LocalDateTime?,
    ): String {
        return when (changeType) {
            SmartRescheduleChangeType.ADDED -> {
                if (deadline?.toLocalDate() == currentDate) {
                    context.getString(R.string.smart_reschedule_reason_deadline_today)
                } else {
                    context.getString(R.string.smart_reschedule_reason_free_window)
                }
            }

            SmartRescheduleChangeType.MOVED -> context.getString(R.string.smart_reschedule_reason_move_priority)
            SmartRescheduleChangeType.DEFERRED -> context.getString(R.string.smart_reschedule_reason_future_slot)
            SmartRescheduleChangeType.UNCHANGED -> context.getString(R.string.smart_reschedule_reason_unchanged)
        }
    }

    private data class PlanningInputs(
        val dayTasks: List<Task>,
        val backlog: List<UnscheduledTask>,
        val events: List<Event>,
        val settings: AppSettings,
    )
}
