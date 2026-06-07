package com.example.smartscheduler.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.example.smartscheduler.di.fake.FakeRepository
import com.example.smartscheduler.domain.algorithm.toDefaultEventSlot
import com.example.smartscheduler.domain.model.Event
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.TimeSlot
import com.example.smartscheduler.domain.model.UnscheduledTask
import com.example.smartscheduler.domain.repository.EventRepository
import com.example.smartscheduler.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
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
    @param:FakeRepository
    private val taskRepository: TaskRepository,
    @param:FakeRepository
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val taskTodayFlow = taskRepository.getDayTasksStream(LocalDate.now())
    private val taskUnallocatedFlow = taskRepository.getUnallocatedTasksStream()
    private val eventsTodayFlow = eventRepository.observeEvents(
        startTime = LocalDate.now().atStartOfDay(),
        endTime = LocalDate.now().plusDays(1).atStartOfDay()
    )

    val uiState: StateFlow<TodayUiState> =
        combine(
            taskTodayFlow,
            taskUnallocatedFlow,
            eventsTodayFlow

        ) { tasks, unscheduledTasks, events ->
            val currentDate = LocalDate.now()

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
                suggestedEventTimeSlot = LocalDateTime.now().toDefaultEventSlot(),
                morningTasks = morning,
                afternoonTasks = afternoon
            )
        }
            .map<TodayUiState.Success, TodayUiState> { it }
            .catch { ex ->
                emit(
                    TodayUiState.Error(
                        currentDate = LocalDate.now(),
                        message = ex.message ?: "Unknown Error"
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TodayUiState.Loading(LocalDate.now())
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
            val newTask = UnscheduledTask(
                id = UUID.randomUUID().toString(),
                name = title,
                description = description,
                status = Status.PENDING,
                priority = Priority.MEDIUM,
                isLocked = false,
                deadline = null,
                preferredPlaceTime = null,
                duration = Duration.ofMinutes(30),
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
