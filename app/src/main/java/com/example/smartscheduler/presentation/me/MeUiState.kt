package com.example.smartscheduler.presentation.me

import com.example.smartscheduler.domain.model.AppSettings
import java.time.Duration

sealed interface MeUiState {
    data object Loading : MeUiState

    data class Error(val message: String) : MeUiState

    data class Success(
        val settings: AppSettings,
        val taskDurationOptions: List<Duration> = DefaultDurationOptions,
        val eventDurationOptions: List<Duration> = DefaultDurationOptions,
    ) : MeUiState
}

private val DefaultDurationOptions = listOf(
    Duration.ofMinutes(15),
    Duration.ofMinutes(30),
    Duration.ofMinutes(45),
    Duration.ofMinutes(60),
    Duration.ofMinutes(90),
    Duration.ofMinutes(120),
)
