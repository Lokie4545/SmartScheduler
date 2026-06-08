package com.example.smartscheduler.presentation.scheduleitemdetail

import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.Status
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

sealed interface ScheduleItemDetailUiState {
    data object Loading : ScheduleItemDetailUiState

    data class Error(
        val message: String,
    ) : ScheduleItemDetailUiState

    data class Content(
        val mode: ScheduleItemDetailMode,
        val itemKind: ScheduleItemKind,
        val itemId: String?,
        val title: String,
        val description: String,
        val date: LocalDate,
        val deadlineDate: LocalDate?,
        val taskDuration: Duration,
        val taskStartTime: LocalTime?,
        val isScheduledTask: Boolean,
        val eventStartTime: LocalTime,
        val eventEndTime: LocalTime,
        val isLocked: Boolean,
        val priority: Priority,
        val status: Status,
        val canSave: Boolean,
        val isSaving: Boolean = false,
        val errorMessage: String? = null,
    ) : ScheduleItemDetailUiState {
        val canChangeKind: Boolean = mode == ScheduleItemDetailMode.CREATE && !isSaving
        val canDelete: Boolean = mode == ScheduleItemDetailMode.EDIT && !isSaving
        val canMarkCompleted: Boolean =
            mode == ScheduleItemDetailMode.EDIT && itemKind == ScheduleItemKind.TASK && !isSaving
        val isEventTimeValid: Boolean = eventStartTime.isBefore(eventEndTime)
    }
}
