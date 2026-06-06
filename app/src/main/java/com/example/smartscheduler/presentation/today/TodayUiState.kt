package com.example.smartscheduler.presentation.today

import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Task
import java.time.LocalDate

sealed interface TodayUiState {
    val date: LocalDate

    data class Loading(override val date: LocalDate) : TodayUiState
    data class Error(
        override val date: LocalDate, val
        message: String
    ) : TodayUiState

    data class Success(
        override val date: LocalDate,
        val tasks: List<ScheduledTask>,
    ) : TodayUiState
}
