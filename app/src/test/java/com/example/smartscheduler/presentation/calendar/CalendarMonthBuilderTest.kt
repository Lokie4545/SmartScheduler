package com.example.smartscheduler.presentation.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class CalendarMonthBuilderTest {

    @Test
    fun `month grid starts on Monday`() {
        val days = buildCalendarMonthDays(
            yearMonth = YearMonth.of(2026, 4),
            selectedDate = LocalDate.of(2026, 4, 17),
            today = LocalDate.of(2026, 4, 1),
            markers = emptyMap(),
        )

        assertNull(days[0].date)
        assertNull(days[1].date)
        assertEquals(LocalDate.of(2026, 4, 1), days[2].date)
    }

    @Test
    fun `month grid can start on Sunday`() {
        val days = buildCalendarMonthDays(
            yearMonth = YearMonth.of(2026, 4),
            selectedDate = LocalDate.of(2026, 4, 17),
            today = LocalDate.of(2026, 4, 1),
            markers = emptyMap(),
            weekStartsOnMonday = false,
        )

        assertNull(days[0].date)
        assertNull(days[1].date)
        assertNull(days[2].date)
        assertEquals(LocalDate.of(2026, 4, 1), days[3].date)
    }

    @Test
    fun `leap year February includes day 29`() {
        val days = buildCalendarMonthDays(
            yearMonth = YearMonth.of(2028, 2),
            selectedDate = LocalDate.of(2028, 2, 29),
            today = LocalDate.of(2028, 2, 1),
            markers = emptyMap(),
        )

        val leapDay = days.first { it.date == LocalDate.of(2028, 2, 29) }

        assertEquals(29, leapDay.dayNumber)
        assertTrue(leapDay.isSelected)
        assertEquals(35, days.size)
    }

    @Test
    fun `markers are mapped to matching day cells`() {
        val markedDate = LocalDate.of(2026, 4, 3)
        val days = buildCalendarMonthDays(
            yearMonth = YearMonth.of(2026, 4),
            selectedDate = LocalDate.of(2026, 4, 17),
            today = LocalDate.of(2026, 4, 1),
            markers = mapOf(
                markedDate to CalendarDayMarker(
                    taskCount = 2,
                    eventCount = 1,
                    hasHighPriorityTask = true,
                )
            ),
        )

        val markedDay = days.first { it.date == markedDate }
        val unmarkedDay = days.first { it.date == LocalDate.of(2026, 4, 4) }

        assertEquals(2, markedDay.taskCount)
        assertEquals(1, markedDay.eventCount)
        assertTrue(markedDay.hasHighPriorityTask)
        assertFalse(unmarkedDay.hasHighPriorityTask)
        assertEquals(0, unmarkedDay.taskCount)
        assertEquals(0, unmarkedDay.eventCount)
    }
}
