package com.example.smartscheduler.presentation.calendar

import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.TimeSlot
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

sealed interface CalendarUiState {
    data class Loading(val selectedDate: LocalDate) : CalendarUiState

    data class Error(
        val selectedDate: LocalDate,
        val message: String,
    ) : CalendarUiState

    data class Success(
        val selectedDate: LocalDate,
        val visibleYear: Int,
        val visibleMonth: Month,
        val yearOptions: List<Int>,
        val months: List<CalendarMonthChipUiModel>,
        val days: List<CalendarDayUiModel>,
        val selectedDayItems: List<CalendarAgendaItemUiModel>,
        val suggestedTaskTimeSlot: TimeSlot,
        val suggestedEventTimeSlot: TimeSlot,
        val weekStartsOnMonday: Boolean,
    ) : CalendarUiState
}

data class CalendarMonthChipUiModel(
    val month: Month,
    val label: String,
    val selected: Boolean,
)

data class CalendarDayUiModel(
    val date: LocalDate?,
    val dayNumber: Int?,
    val isToday: Boolean,
    val isSelected: Boolean,
    val taskCount: Int,
    val eventCount: Int,
    val hasHighPriorityTask: Boolean,
)

sealed interface CalendarAgendaItemUiModel {
    val id: String
    val title: String
    val description: String?
    val startTime: LocalDateTime
    val endTime: LocalDateTime
    val duration: Duration

    data class TaskItem(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val startTime: LocalDateTime,
        override val endTime: LocalDateTime,
        override val duration: Duration,
        val status: Status,
        val priority: Priority,
    ) : CalendarAgendaItemUiModel

    data class EventItem(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val startTime: LocalDateTime,
        override val endTime: LocalDateTime,
        override val duration: Duration,
    ) : CalendarAgendaItemUiModel
}

data class CalendarDayMarker(
    val taskCount: Int = 0,
    val eventCount: Int = 0,
    val hasHighPriorityTask: Boolean = false,
)
