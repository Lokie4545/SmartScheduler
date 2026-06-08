package com.example.smartscheduler.presentation.scheduleitemdetail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartscheduler.R
import com.example.smartscheduler.domain.model.Event
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.Task
import com.example.smartscheduler.domain.model.UnscheduledTask
import com.example.smartscheduler.domain.repository.EventRepository
import com.example.smartscheduler.domain.repository.SettingsRepository
import com.example.smartscheduler.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ScheduleItemDetailViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val taskRepository: TaskRepository,
    private val eventRepository: EventRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleItemDetailUiState>(ScheduleItemDetailUiState.Loading)
    val uiState: StateFlow<ScheduleItemDetailUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ScheduleItemDetailEffect>()
    val effects: SharedFlow<ScheduleItemDetailEffect> = _effects.asSharedFlow()

    private var currentArgs: ScheduleItemDetailArgs? = null
    private var originalTask: Task? = null
    private var defaultTaskStartTime: LocalTime = LocalTime.of(9, 0)

    fun initialize(args: ScheduleItemDetailArgs) {
        if (currentArgs == args) return

        currentArgs = args
        originalTask = null

        when (args.mode) {
            ScheduleItemDetailMode.CREATE -> showCreateContent(args)
            ScheduleItemDetailMode.EDIT -> loadExistingItem(args)
        }
    }

    fun handleAction(action: ScheduleItemDetailAction) {
        when (action) {
            is ScheduleItemDetailAction.TitleChanged -> updateContent {
                it.copy(title = action.title)
            }

            is ScheduleItemDetailAction.DescriptionChanged -> updateContent {
                it.copy(description = action.description)
            }

            is ScheduleItemDetailAction.KindChanged -> updateContent {
                if (it.canChangeKind) it.copy(itemKind = action.kind) else it
            }

            is ScheduleItemDetailAction.DateChanged -> updateContent { content ->
                when (content.itemKind) {
                    ScheduleItemKind.TASK -> content.copy(
                        date = action.date,
                        taskStartTime = content.taskStartTime ?: defaultTaskStartTime,
                        isScheduledTask = true,
                        status = if (content.status == Status.PENDING) Status.SCHEDULED else content.status,
                    )

                    ScheduleItemKind.EVENT -> content.copy(date = action.date)
                }
            }

            is ScheduleItemDetailAction.DeadlineChanged -> updateContent {
                it.copy(deadlineDate = action.date)
            }

            is ScheduleItemDetailAction.TaskDurationChanged -> updateContent {
                it.copy(taskDuration = action.duration)
            }

            is ScheduleItemDetailAction.TaskStartTimeChanged -> updateContent {
                it.copy(taskStartTime = action.time)
            }

            is ScheduleItemDetailAction.EventStartTimeChanged -> updateContent {
                it.copy(eventStartTime = action.time)
            }

            is ScheduleItemDetailAction.EventEndTimeChanged -> updateContent {
                it.copy(eventEndTime = action.time)
            }

            is ScheduleItemDetailAction.LockChanged -> updateContent {
                it.copy(isLocked = action.locked)
            }

            is ScheduleItemDetailAction.PriorityChanged -> updateContent {
                it.copy(priority = action.priority)
            }

            ScheduleItemDetailAction.Save -> save()
            ScheduleItemDetailAction.Delete -> delete()
            ScheduleItemDetailAction.MarkCompleted -> markCompleted()
            ScheduleItemDetailAction.Close -> navigateBack()
            ScheduleItemDetailAction.Retry -> retry()
        }
    }

    private fun showCreateContent(args: ScheduleItemDetailArgs) {
        _uiState.value = ScheduleItemDetailUiState.Loading
        viewModelScope.launch {
            val settings = settingsRepository.settingsStream.first()
            defaultTaskStartTime = settings.workDayStart
            val now = LocalDateTime.now().withSecond(0).withNano(0)
            val startTime = args.draftStartTime ?: now
            val eventEndTime = args.draftEndTime ?: startTime.plus(settings.defaultEventDuration)
            val initialDuration = if (args.draftStartTime != null && args.draftEndTime != null) {
                Duration.between(startTime, args.draftEndTime)
                    .takeIf { !it.isNegative && !it.isZero }
            } else {
                null
            } ?: settings.defaultTaskDuration
            val createsScheduledTask = args.kind == ScheduleItemKind.TASK && args.draftStartTime != null

            val content = ScheduleItemDetailUiState.Content(
                mode = ScheduleItemDetailMode.CREATE,
                itemKind = args.kind,
                itemId = null,
                title = args.draftTitle,
                description = args.draftDescription,
                date = startTime.toLocalDate(),
                deadlineDate = null,
                taskDuration = initialDuration,
                taskStartTime = if (createsScheduledTask) startTime.toLocalTime() else null,
                isScheduledTask = createsScheduledTask,
                eventStartTime = startTime.toLocalTime(),
                eventEndTime = eventEndTime.toLocalTime(),
                isLocked = false,
                priority = Priority.MEDIUM,
                status = if (createsScheduledTask) Status.SCHEDULED else Status.PENDING,
                canSave = false,
            )

            _uiState.value = renderContent(content)
        }
    }

    private fun loadExistingItem(args: ScheduleItemDetailArgs) {
        val itemId = args.itemId
        if (itemId == null) {
            _uiState.value = ScheduleItemDetailUiState.Error(context.getString(R.string.detail_missing_item_id))
            return
        }

        _uiState.value = ScheduleItemDetailUiState.Loading
        viewModelScope.launch {
            runCatching {
                when (args.kind) {
                    ScheduleItemKind.TASK -> loadTask(itemId)
                    ScheduleItemKind.EVENT -> loadEvent(itemId)
                }
            }.onFailure { error ->
                _uiState.value = ScheduleItemDetailUiState.Error(
                    error.message ?: context.getString(R.string.detail_load_error)
                )
            }
        }
    }

    private suspend fun loadTask(taskId: String) {
        val task = taskRepository.getTask(taskId) ?: error(context.getString(R.string.detail_task_not_found))
        val settings = settingsRepository.settingsStream.first()
        defaultTaskStartTime = settings.workDayStart
        originalTask = task

        val content = when (task) {
            is ScheduledTask -> ScheduleItemDetailUiState.Content(
                mode = ScheduleItemDetailMode.EDIT,
                itemKind = ScheduleItemKind.TASK,
                itemId = task.id,
                title = task.name,
                description = task.description.orEmpty(),
                date = task.startTime.toLocalDate(),
                deadlineDate = task.deadline?.toLocalDate(),
                taskDuration = task.duration,
                taskStartTime = task.startTime.toLocalTime(),
                isScheduledTask = true,
                eventStartTime = task.startTime.toLocalTime(),
                eventEndTime = task.endTime.toLocalTime(),
                isLocked = task.isLocked,
                priority = task.priority,
                status = task.status,
                canSave = false,
            )

            is UnscheduledTask -> ScheduleItemDetailUiState.Content(
                mode = ScheduleItemDetailMode.EDIT,
                itemKind = ScheduleItemKind.TASK,
                itemId = task.id,
                title = task.name,
                description = task.description.orEmpty(),
                date = LocalDate.now(),
                deadlineDate = task.deadline?.toLocalDate(),
                taskDuration = task.duration ?: settings.defaultTaskDuration,
                taskStartTime = null,
                isScheduledTask = false,
                eventStartTime = DefaultEventStart,
                eventEndTime = DefaultEventEnd,
                isLocked = task.isLocked,
                priority = task.priority,
                status = task.status,
                canSave = false,
            )
        }

        _uiState.value = renderContent(content)
    }

    private suspend fun loadEvent(eventId: String) {
        val event = eventRepository.getEvent(eventId) ?: error(context.getString(R.string.detail_event_not_found))
        val settings = settingsRepository.settingsStream.first()
        defaultTaskStartTime = settings.workDayStart
        val content = ScheduleItemDetailUiState.Content(
            mode = ScheduleItemDetailMode.EDIT,
            itemKind = ScheduleItemKind.EVENT,
            itemId = event.id,
            title = event.name,
            description = event.description.orEmpty(),
            date = event.startTime.toLocalDate(),
            deadlineDate = null,
            taskDuration = settings.defaultTaskDuration,
            taskStartTime = null,
            isScheduledTask = false,
            eventStartTime = event.startTime.toLocalTime(),
            eventEndTime = event.endTime.toLocalTime(),
            isLocked = false,
            priority = Priority.MEDIUM,
            status = Status.PENDING,
            canSave = false,
        )

        _uiState.value = renderContent(content)
    }

    private fun save() {
        val content = _uiState.value as? ScheduleItemDetailUiState.Content ?: return
        if (!content.canSave) return

        viewModelScope.launch {
            setSaving(true)
            runCatching {
                when (content.itemKind) {
                    ScheduleItemKind.TASK -> saveTask(content)
                    ScheduleItemKind.EVENT -> saveEvent(content)
                }
            }.onSuccess {
                navigateBack()
            }.onFailure { error ->
                showContentError(error.message ?: context.getString(R.string.detail_save_error))
            }
        }
    }

    private suspend fun saveTask(content: ScheduleItemDetailUiState.Content, statusOverride: Status? = null) {
        val id = content.itemId ?: UUID.randomUUID().toString()
        val task = content.toDomainTask(
            id = id,
            existingTask = originalTask,
            statusOverride = statusOverride,
        )

        if (content.mode == ScheduleItemDetailMode.CREATE) {
            taskRepository.createTask(task)
        } else {
            taskRepository.updateTask(task)
        }
    }

    private suspend fun saveEvent(content: ScheduleItemDetailUiState.Content) {
        require(content.eventStartTime.isBefore(content.eventEndTime)) {
            context.getString(R.string.detail_end_after_start)
        }

        val event = Event(
            id = content.itemId ?: UUID.randomUUID().toString(),
            name = content.title.trim(),
            description = content.description.normalizedDescription(),
            startTime = content.date.atTime(content.eventStartTime),
            endTime = content.date.atTime(content.eventEndTime),
        )

        if (content.mode == ScheduleItemDetailMode.CREATE) {
            eventRepository.createEvent(event)
        } else {
            eventRepository.updateEvent(event)
        }
    }

    private fun delete() {
        val content = _uiState.value as? ScheduleItemDetailUiState.Content ?: return
        val itemId = content.itemId ?: return
        if (!content.canDelete) return

        viewModelScope.launch {
            setSaving(true)
            runCatching {
                when (content.itemKind) {
                    ScheduleItemKind.TASK -> taskRepository.deleteTask(itemId)
                    ScheduleItemKind.EVENT -> eventRepository.deleteEvent(itemId)
                }
            }.onSuccess {
                navigateBack()
            }.onFailure { error ->
                showContentError(error.message ?: context.getString(R.string.detail_delete_error))
            }
        }
    }

    private fun markCompleted() {
        val content = _uiState.value as? ScheduleItemDetailUiState.Content ?: return
        if (!content.canMarkCompleted) return

        val newStatus = if (content.status == Status.COMPLETED) {
            if (content.isScheduledTask) Status.SCHEDULED else Status.PENDING
        } else {
            Status.COMPLETED
        }

        viewModelScope.launch {
            setSaving(true)
            runCatching {
                saveTask(content, statusOverride = newStatus)
            }.onSuccess {
                navigateBack()
            }.onFailure { error ->
                showContentError(error.message ?: context.getString(R.string.detail_update_error))
            }
        }
    }

    private fun retry() {
        val args = currentArgs ?: return
        currentArgs = null
        initialize(args)
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effects.emit(ScheduleItemDetailEffect.NavigateBack)
        }
    }

    private fun updateContent(
        transform: (ScheduleItemDetailUiState.Content) -> ScheduleItemDetailUiState.Content,
    ) {
        val content = _uiState.value as? ScheduleItemDetailUiState.Content ?: return
        _uiState.value = renderContent(transform(content).copy(errorMessage = null))
    }

    private fun setSaving(isSaving: Boolean) {
        val content = _uiState.value as? ScheduleItemDetailUiState.Content ?: return
        _uiState.value = renderContent(content.copy(isSaving = isSaving))
    }

    private fun showContentError(message: String) {
        val content = _uiState.value as? ScheduleItemDetailUiState.Content ?: return
        _uiState.value = renderContent(
            content.copy(
                isSaving = false,
                errorMessage = message,
            )
        )
    }

    private fun renderContent(content: ScheduleItemDetailUiState.Content): ScheduleItemDetailUiState.Content {
        val hasTitle = content.title.isNotBlank()
        val hasValidTaskDuration = !content.taskDuration.isNegative && !content.taskDuration.isZero
        val hasValidEventTime = content.itemKind != ScheduleItemKind.EVENT || content.isEventTimeValid
        return content.copy(
            canSave = hasTitle && hasValidTaskDuration && hasValidEventTime && !content.isSaving,
        )
    }

    private fun ScheduleItemDetailUiState.Content.toDomainTask(
        id: String,
        existingTask: Task?,
        statusOverride: Status?,
    ): Task {
        val taskStatus = statusOverride ?: if (isScheduledTask && status == Status.PENDING) {
            Status.SCHEDULED
        } else {
            status
        }
        val description = description.normalizedDescription()

        return if (isScheduledTask && taskStartTime != null) {
            val startTime = date.atTime(taskStartTime)
            ScheduledTask(
                id = id,
                name = title.trim(),
                description = description,
                status = taskStatus,
                priority = priority,
                isLocked = isLocked,
                deadline = deadlineDate?.atTime(TaskDeadlineTime),
                preferredPlaceTime = existingTask?.preferredPlaceTime,
                startTime = startTime,
                endTime = startTime.plus(taskDuration),
            )
        } else {
            UnscheduledTask(
                id = id,
                name = title.trim(),
                description = description,
                status = taskStatus,
                priority = priority,
                isLocked = isLocked,
                deadline = deadlineDate?.atTime(TaskDeadlineTime),
                preferredPlaceTime = existingTask?.preferredPlaceTime,
                duration = taskDuration,
            )
        }
    }

    private fun String.normalizedDescription(): String? {
        return trim().takeIf { it.isNotBlank() }
    }

    private companion object {
        val DefaultEventStart: LocalTime = LocalTime.of(9, 0)
        val DefaultEventEnd: LocalTime = LocalTime.of(9, 30)
        val TaskDeadlineTime: LocalTime = LocalTime.of(23, 59)
    }
}
