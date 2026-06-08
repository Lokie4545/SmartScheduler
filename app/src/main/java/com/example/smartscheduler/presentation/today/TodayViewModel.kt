package com.example.smartscheduler.presentation.today

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartscheduler.R
import com.example.smartscheduler.domain.algorithm.toDefaultEventSlot
import com.example.smartscheduler.domain.model.Event
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.TimeSlot
import com.example.smartscheduler.domain.model.UnscheduledTask
import com.example.smartscheduler.domain.repository.EventRepository
import com.example.smartscheduler.domain.repository.SettingsRepository
import com.example.smartscheduler.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val taskRepository: TaskRepository,
    private val eventRepository: EventRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val currentDate = LocalDate.now()
    private val taskTodayFlow = taskRepository.getDayTasksStream(currentDate)
    private val taskUnallocatedFlow = taskRepository.getUnallocatedTasksStream()
    private val eventsTodayFlow = eventRepository.observeEvents(
        startTime = currentDate.atStartOfDay(),
        endTime = currentDate.plusDays(1).atStartOfDay()
    )

    val uiState: StateFlow<TodayUiState> =
        combine(
            taskTodayFlow,
            taskUnallocatedFlow,
            eventsTodayFlow,
            settingsRepository.settingsStream,

        ) { tasks, unscheduledTasks, events, settings ->
            val unscheduledDuration = unscheduledTasks
                .mapNotNull { it.duration }
                .fold(Duration.ZERO, Duration::plus)

            val scheduledTasks = tasks.filterIsInstance<ScheduledTask>()
            val fullSchedule: List<TimeSlot> = (scheduledTasks + events).sortedBy { it.startTime }

            val (morning, afternoon) = fullSchedule.partition {
                it.startTime.toLocalTime().isBefore(LocalTime.NOON)
            }

            TodayUiState.Success(
                currentDate = currentDate,
                unscheduledTaskCount = unscheduledTasks.size,
                unscheduledDuration = unscheduledDuration,
                defaultTaskDuration = settings.defaultTaskDuration,
                suggestedEventTimeSlot = LocalDateTime.now().toDefaultEventSlot(
                    durationMinutes = settings.defaultEventDuration.toMinutes(),
                ),
                morningTasks = morning,
                afternoonTasks = afternoon
            )
        }
            .map<TodayUiState.Success, TodayUiState> { it }
            .catch { ex ->
                emit(
                    TodayUiState.Error(
                        currentDate = currentDate,
                        message = ex.message ?: context.getString(R.string.today_error_fallback)
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TodayUiState.Loading(currentDate)
            )

    fun handleAction(action: TodayAction) {
        when (action) {
            is TodayAction.AddQuickTask -> addQuickTask(action.title, action.description)
            is TodayAction.AddQuickEvent -> addQuickEvent(
                action.title,
                action.description,
                action.timeSlot
            )

            is TodayAction.MarkTaskCompleted -> markTaskCompleted(action.taskId)
            TodayAction.RequestSmartReschedule -> Unit // TODO: wire up reschedule use case
            else -> {}
        }
    }

    private fun markTaskCompleted(taskId: String) {
        viewModelScope.launch {
            val currentTasks = taskTodayFlow.first().filterIsInstance<ScheduledTask>()

            val task = currentTasks.firstOrNull { it.id == taskId } ?: return@launch

            val newStatus = if (task.status == Status.COMPLETED) {
                Status.SCHEDULED
            } else {
                Status.COMPLETED
            }
            taskRepository.updateTask(task.copy(status = newStatus))
        }
    }

    private fun addQuickTask(title: String, description: String) {
        viewModelScope.launch {
            val settings = settingsRepository.settingsStream.first()
            val newTask = UnscheduledTask(
                id = UUID.randomUUID().toString(),
                name = title,
                description = description,
                status = Status.PENDING,
                priority = Priority.MEDIUM,
                isLocked = false,
                deadline = null,
                preferredPlaceTime = null,
                duration = settings.defaultTaskDuration,
            )
            taskRepository.createTask(newTask)
        }
    }

    private fun addQuickEvent(title: String, description: String, timeSlot: TimeSlot) {
        viewModelScope.launch {
            val newEvent = Event(
                id = UUID.randomUUID().toString(),
                name = title,
                description = description,
                startTime = timeSlot.startTime,
                endTime = timeSlot.endTime
            )
            eventRepository.createEvent(newEvent)
        }
    }
}
