package com.example.smartscheduler.presentation.calendar

import com.example.smartscheduler.domain.model.TimeSlot
import com.example.smartscheduler.presentation.scheduleitemdetail.ScheduleItemKind
import java.time.Month

sealed interface CalendarAction {
    data class SelectYear(val year: Int) : CalendarAction
    data class SelectMonth(val month: Month) : CalendarAction
    data class SelectDate(val date: java.time.LocalDate) : CalendarAction
    data class MarkTaskCompleted(val taskId: String) : CalendarAction
    data class AddQuickTask(val title: String, val description: String) : CalendarAction
    data class AddQuickEvent(val title: String, val description: String, val timeSlot: TimeSlot) : CalendarAction
    data object RequestAddTask : CalendarAction
    data object RequestAddEvent : CalendarAction
    data object DismissFastAddRequest : CalendarAction
    data object Retry : CalendarAction
    data class NavigateToScheduleItemCreate(
        val kind: ScheduleItemKind,
        val draftTitle: String,
        val draftDescription: String,
        val draftTimeSlot: TimeSlot? = null,
    ) : CalendarAction
    data class NavigateToScheduleItemEdit(
        val kind: ScheduleItemKind,
        val itemId: String,
    ) : CalendarAction
}
