package com.example.smartscheduler.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Task
import com.example.smartscheduler.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
) : ViewModel() {
    val uiState: StateFlow<TodayUiState> =
        taskRepository.getDayTasksStream(LocalDate.now()).map<List<Task>, TodayUiState> { tasks ->
            val scheduledTasks = tasks.filterIsInstance<ScheduledTask>()
            TodayUiState.Success(tasks = scheduledTasks, date = LocalDate.now())
        }.catch { ex ->
            emit(
                TodayUiState.Error(
                    date = LocalDate.now(),
                    message = ex.message ?: "Unknown Error"
                )
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodayUiState.Loading(LocalDate.now())
        )
}