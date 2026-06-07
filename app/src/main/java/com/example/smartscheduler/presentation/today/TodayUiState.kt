package com.example.smartscheduler.presentation.today

import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Task
import com.example.smartscheduler.domain.model.TimeSlot
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

sealed interface TodayUiState {
    val currentDate: LocalDate

    data class Loading(override val currentDate: LocalDate) : TodayUiState
    data class Error(
        override val currentDate: LocalDate,
        val message: String
    ) : TodayUiState

    data class Success(
        override val currentDate: LocalDate,
        val tasks: List<ScheduledTask>,
        val suggestedEventTimeSlot: TimeSlot
    ) : TodayUiState
}
