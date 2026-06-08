package com.example.smartscheduler.presentation.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartscheduler.presentation.calendar.CalendarDayMarker
import com.example.smartscheduler.presentation.calendar.CalendarDayUiModel
import com.example.smartscheduler.presentation.calendar.buildCalendarMonthDays
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle

@Composable
fun CalendarMonthGrid(
    days: List<CalendarDayUiModel>,
    weekStartsOnMonday: Boolean = true,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val weekdayLabels = rememberWeekdayLabels(weekStartsOnMonday)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdayLabels.forEach { label ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        days.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    CalendarDayCell(
                        day = day,
                        onDateClick = onDateClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberWeekdayLabels(weekStartsOnMonday: Boolean): List<String> {
    val locale = LocalLocale.current.platformLocale
    return androidx.compose.runtime.remember(weekStartsOnMonday, locale) {
        val days = if (weekStartsOnMonday) {
            DayOfWeek.entries
        } else {
            listOf(DayOfWeek.SUNDAY) + DayOfWeek.entries.dropLast(1)
        }
        days.map { it.getDisplayName(TextStyle.NARROW_STANDALONE, locale) }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDayUiModel,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val date = day.date

    Column(
        modifier = modifier
            .height(56.dp)
            .then(if (date != null) Modifier.clickable { onDateClick(date) } else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(if (day.isSelected) 44.dp else 36.dp)
                .clip(CircleShape)
                .background(selectedDayColor(day)),
            contentAlignment = Alignment.Center,
        ) {
            if (day.dayNumber != null) {
                Text(
                    text = day.dayNumber.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = selectedDayContentColor(day),
                )
            }
        }

        CalendarEventDots(
            taskCount = day.taskCount,
            eventCount = day.eventCount,
            hasHighPriorityTask = day.hasHighPriorityTask,
        )
    }
}

@Composable
private fun CalendarEventDots(
    taskCount: Int,
    eventCount: Int,
    hasHighPriorityTask: Boolean,
) {
    Row(
        modifier = Modifier.height(8.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val taskDots = taskCount.coerceAtMost(2)
        repeat(taskDots) {
            CalendarDot(
                color = if (hasHighPriorityTask) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
        }
        if (eventCount > 0) {
            CalendarDot(color = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun CalendarDot(color: Color) {
    Box(
        modifier = Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun selectedDayColor(day: CalendarDayUiModel): Color {
    return when {
        day.isSelected -> MaterialTheme.colorScheme.primary
        day.isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
}

@Composable
private fun selectedDayContentColor(day: CalendarDayUiModel): Color {
    return when {
        day.isSelected -> MaterialTheme.colorScheme.onPrimary
        day.isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
}

@Preview(name = "Month grid", apiLevel = 35, showBackground = true)
@Composable
private fun CalendarMonthGridPreview() {
    val yearMonth = YearMonth.of(2026, 4)
    val selectedDate = LocalDate.of(2026, 4, 17)
    SmartSchedulerTheme(dynamicColor = false) {
        CalendarMonthGrid(
            days = buildCalendarMonthDays(
                yearMonth = yearMonth,
                selectedDate = selectedDate,
                today = selectedDate,
                markers = mapOf(
                    LocalDate.of(2026, 4, 1) to CalendarDayMarker(taskCount = 2, eventCount = 1, hasHighPriorityTask = true),
                    LocalDate.of(2026, 4, 3) to CalendarDayMarker(taskCount = 1, eventCount = 1),
                ),
            ),
            onDateClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Leap year February", apiLevel = 35, showBackground = true)
@Composable
private fun CalendarMonthGridLeapYearPreview() {
    SmartSchedulerTheme(dynamicColor = false) {
        CalendarMonthGrid(
            days = buildCalendarMonthDays(
                yearMonth = YearMonth.of(2028, 2),
                selectedDate = LocalDate.of(2028, 2, 29),
                today = LocalDate.of(2028, 2, 1),
                markers = emptyMap(),
            ),
            onDateClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
