package com.example.smartscheduler.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartscheduler.di.fake.FakeRepository
import com.example.smartscheduler.domain.algorithm.toDefaultEventSlot
import com.example.smartscheduler.domain.model.Event
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.Task
import com.example.smartscheduler.domain.model.TimeSlot
import com.example.smartscheduler.domain.model.UnscheduledTask
import com.example.smartscheduler.domain.repository.EventRepository
import com.example.smartscheduler.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    @param:FakeRepository
    private val taskRepository: TaskRepository,
    @param:FakeRepository
    private val eventRepository: EventRepository,
) : ViewModel() {
    val uiState: StateFlow<TodayUiState> =
        taskRepository.getDayTasksStream(LocalDate.now()).map<List<Task>, TodayUiState> { tasks ->
            val scheduledTasks = tasks.filterIsInstance<ScheduledTask>()
            TodayUiState.Success(
                tasks = scheduledTasks,
                currentDate = LocalDate.now(),
                suggestedEventTimeSlot = LocalDateTime.now().toDefaultEventSlot()
            )
        }.catch { ex ->
            emit(
                TodayUiState.Error(
                    currentDate = LocalDate.now(),
                    message = ex.message ?: "Unknown Error"
                )
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodayUiState.Loading(LocalDate.now())
        )


    fun handleAction(action: TodayAction) {
        when (action) {
            is TodayAction.AddQuickTask -> addQuickTask(action.title, action.description)
            is TodayAction.AddQuickEvent -> addQuickEvent(action.title, action.description, action.timeSlot)
            else -> {}
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
                duration = null,
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
