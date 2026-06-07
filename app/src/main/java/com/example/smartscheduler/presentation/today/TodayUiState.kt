package com.example.smartscheduler.presentation.today

import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Task
import java.time.LocalDate
import java.time.LocalDateTime

sealed interface TodayUiState {
    val currentDateTime: LocalDateTime

    data class Loading(override val currentDateTime: LocalDateTime) : TodayUiState
    data class Error(
        override val currentDateTime: LocalDateTime, val
        message: String
    ) : TodayUiState

    data class Success(
        override val currentDateTime: LocalDateTime,
        val tasks: List<ScheduledTask>,
    ) : TodayUiState
}
