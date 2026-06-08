package com.example.smartscheduler.presentation.today

import com.example.smartscheduler.domain.model.Event
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.TimeSlot
import java.time.Duration
import java.time.LocalDate

sealed interface TodayUiState {
    val currentDate: LocalDate

    data class Loading(override val currentDate: LocalDate) : TodayUiState
    data class Error(
        override val currentDate: LocalDate,
        val message: String
    ) : TodayUiState

    data class Success(
        override val currentDate: LocalDate,
        val morningTasks: List<TimeSlot>,
        val afternoonTasks: List<TimeSlot>,
        val unscheduledTaskCount: Int,
        val unscheduledDuration: Duration,
        val defaultTaskDuration: Duration,
        val suggestedEventTimeSlot: TimeSlot
    ) : TodayUiState
}
