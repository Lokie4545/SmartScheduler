package com.example.smartscheduler.presentation.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

internal fun buildCalendarMonthDays(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    markers: Map<LocalDate, CalendarDayMarker>,
    today: LocalDate = LocalDate.now(),
    weekStartsOnMonday: Boolean = true,
): List<CalendarDayUiModel> {
    val firstDay = yearMonth.atDay(1)
    val leadingEmptyCells = if (weekStartsOnMonday) {
        firstDay.dayOfWeek.mondayBasedIndex
    } else {
        firstDay.dayOfWeek.sundayBasedIndex
    }
    val daysInMonth = yearMonth.lengthOfMonth()
    val cells = mutableListOf<CalendarDayUiModel>()

    repeat(leadingEmptyCells) {
        cells += emptyDayCell()
    }

    for (day in 1..daysInMonth) {
        val date = yearMonth.atDay(day)
        val marker = markers[date] ?: CalendarDayMarker()
        cells += CalendarDayUiModel(
            date = date,
            dayNumber = day,
            isToday = date == today,
            isSelected = date == selectedDate,
            taskCount = marker.taskCount,
            eventCount = marker.eventCount,
            hasHighPriorityTask = marker.hasHighPriorityTask,
        )
    }

    val trailingEmptyCells = (7 - cells.size % 7).takeIf { it < 7 } ?: 0
    repeat(trailingEmptyCells) {
        cells += emptyDayCell()
    }

    return cells
}

private fun emptyDayCell(): CalendarDayUiModel {
    return CalendarDayUiModel(
        date = null,
        dayNumber = null,
        isToday = false,
        isSelected = false,
        taskCount = 0,
        eventCount = 0,
        hasHighPriorityTask = false,
    )
}

private val DayOfWeek.mondayBasedIndex: Int
    get() = value - 1

private val DayOfWeek.sundayBasedIndex: Int
    get() = value % 7
