package com.example.smartscheduler.presentation.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.TimeSlot
import com.example.smartscheduler.presentation.calendar.components.CalendarMonthChipRow
import com.example.smartscheduler.presentation.calendar.components.CalendarMonthGrid
import com.example.smartscheduler.presentation.calendar.components.CalendarYearSelector
import com.example.smartscheduler.presentation.calendar.components.calendarAgendaItems
import com.example.smartscheduler.presentation.components.SmartLoadingCircularIndicator
import com.example.smartscheduler.presentation.scheduleitemdetail.ScheduleItemKind
import com.example.smartscheduler.presentation.today.components.FastAddEventBottomSheet
import com.example.smartscheduler.presentation.today.components.FastAddTaskBottomSheet
import com.example.smartscheduler.presentation.today.components.SmartFabMenu
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.Locale

private object CalendarSpacing {
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val ExtraLarge = 24.dp
    val ContentBottom = 112.dp
}

@Composable
fun CalendarRoute(
    viewModel: CalendarViewModel,
    onNavigateToScheduleItemCreate: (ScheduleItemKind, String, String, TimeSlot?) -> Unit,
    onNavigateToScheduleItemEdit: (ScheduleItemKind, String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CalendarScreen(uiState = uiState) { action ->
        when (action) {
            is CalendarAction.NavigateToScheduleItemCreate -> {
                onNavigateToScheduleItemCreate(
                    action.kind,
                    action.draftTitle,
                    action.draftDescription,
                    action.draftTimeSlot,
                )
            }

            is CalendarAction.NavigateToScheduleItemEdit -> {
                onNavigateToScheduleItemEdit(action.kind, action.itemId)
            }

            else -> viewModel.handleAction(action)
        }
    }
}

private sealed interface CalendarBottomSheetConfig {
    data object None : CalendarBottomSheetConfig
    data object AddTask : CalendarBottomSheetConfig
    data object AddEvent : CalendarBottomSheetConfig
}

@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    modifier: Modifier = Modifier,
    onAction: (CalendarAction) -> Unit,
) {
    var bottomSheetConfig by remember { mutableStateOf<CalendarBottomSheetConfig>(CalendarBottomSheetConfig.None) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Top
        ),
        floatingActionButton = {
            if (uiState is CalendarUiState.Success) {
                SmartFabMenu(
                    modifier = Modifier.padding(
                        end = CalendarSpacing.Small,
                        bottom = CalendarSpacing.Small,
                    ),
                    onAddTaskClick = {
                        bottomSheetConfig = CalendarBottomSheetConfig.AddTask
                    },
                    onAddEventClick = {
                        bottomSheetConfig = CalendarBottomSheetConfig.AddEvent
                    },
                )
            }
        },
    ) { innerPadding ->
        when (uiState) {
            is CalendarUiState.Loading -> CalendarLoadingContent(
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
            )

            is CalendarUiState.Error -> CalendarErrorContent(
                message = uiState.message,
                onRetry = { onAction(CalendarAction.Retry) },
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
            )

            is CalendarUiState.Success -> CalendarSuccessContent(
                uiState = uiState,
                onAction = onAction,
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
            )
        }

        if (uiState is CalendarUiState.Success) {
            when (bottomSheetConfig) {
                CalendarBottomSheetConfig.AddTask -> {
                    FastAddTaskBottomSheet(
                        dateLabel = uiState.selectedDate.formatFastAddDateLabel(),
                        durationLabel = uiState.suggestedTaskTimeSlot.duration.formatFastAddDuration(),
                        onDismissRequest = {
                            onAction(CalendarAction.DismissFastAddRequest)
                            bottomSheetConfig = CalendarBottomSheetConfig.None
                        },
                        onSaveDefaultTask = { title, description ->
                            onAction(CalendarAction.AddQuickTask(title, description))
                            bottomSheetConfig = CalendarBottomSheetConfig.None
                        },
                        onNavigateToFullscreenTask = { draftTitle, draftDescription ->
                            onAction(
                                CalendarAction.NavigateToScheduleItemCreate(
                                    kind = ScheduleItemKind.TASK,
                                    draftTitle = draftTitle,
                                    draftDescription = draftDescription,
                                    draftTimeSlot = uiState.suggestedTaskTimeSlot,
                                )
                            )
                            bottomSheetConfig = CalendarBottomSheetConfig.None
                        },
                    )
                }

                CalendarBottomSheetConfig.AddEvent -> {
                    FastAddEventBottomSheet(
                        dateLabel = uiState.selectedDate.formatFastAddDateLabel(),
                        onDismissRequest = {
                            onAction(CalendarAction.DismissFastAddRequest)
                            bottomSheetConfig = CalendarBottomSheetConfig.None
                        },
                        currentTimeSlot = uiState.suggestedEventTimeSlot,
                        onSaveDefaultEvent = { title, description ->
                            onAction(CalendarAction.AddQuickEvent(title, description, uiState.suggestedEventTimeSlot))
                            bottomSheetConfig = CalendarBottomSheetConfig.None
                        },
                        onNavigateToFullscreenTask = { draftTitle, draftDescription ->
                            onAction(
                                CalendarAction.NavigateToScheduleItemCreate(
                                    kind = ScheduleItemKind.EVENT,
                                    draftTitle = draftTitle,
                                    draftDescription = draftDescription,
                                    draftTimeSlot = uiState.suggestedEventTimeSlot,
                                )
                            )
                            bottomSheetConfig = CalendarBottomSheetConfig.None
                        },
                    )
                }

                CalendarBottomSheetConfig.None -> Unit
            }
        }
    }
}

@Composable
private fun CalendarSuccessContent(
    uiState: CalendarUiState.Success,
    onAction: (CalendarAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = CalendarSpacing.ContentBottom),
        verticalArrangement = Arrangement.spacedBy(CalendarSpacing.Medium),
    ) {
        item(key = "header") {
            CalendarHeader(
                year = uiState.visibleYear,
                yearOptions = uiState.yearOptions,
                onYearSelected = { onAction(CalendarAction.SelectYear(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = CalendarSpacing.Large,
                        top = CalendarSpacing.ExtraLarge,
                        end = CalendarSpacing.Large,
                    ),
            )
        }

        item(key = "months") {
            CalendarMonthChipRow(
                months = uiState.months,
                onMonthClick = { onAction(CalendarAction.SelectMonth(it)) },
            )
        }

        item(key = "month_grid") {
            CalendarMonthGrid(
                days = uiState.days,
                weekStartsOnMonday = uiState.weekStartsOnMonday,
                onDateClick = { onAction(CalendarAction.SelectDate(it)) },
                modifier = Modifier.padding(horizontal = CalendarSpacing.Large),
            )
        }

        calendarAgendaItems(
            items = uiState.selectedDayItems,
            onTaskCheckedChange = { taskId -> onAction(CalendarAction.MarkTaskCompleted(taskId)) },
            onItemClick = { item ->
                when (item) {
                    is CalendarAgendaItemUiModel.TaskItem -> onAction(
                        CalendarAction.NavigateToScheduleItemEdit(ScheduleItemKind.TASK, item.id)
                    )
                    is CalendarAgendaItemUiModel.EventItem -> onAction(
                        CalendarAction.NavigateToScheduleItemEdit(ScheduleItemKind.EVENT, item.id)
                    )
                }
            },
        )
    }
}

@Composable
private fun CalendarHeader(
    year: Int,
    yearOptions: List<Int>,
    onYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        CalendarYearSelector(
            year = year,
            yearOptions = yearOptions,
            onYearSelected = onYearSelected,
        )
    }
}

@Composable
private fun CalendarLoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        SmartLoadingCircularIndicator(isLoading = true)
    }
}

@Composable
private fun CalendarErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(CalendarSpacing.ExtraLarge),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CalendarSpacing.Medium),
        ) {
            Text(
                text = message.ifBlank { "Unable to load calendar" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

private fun LocalDate.formatFastAddDateLabel(): String {
    val today = LocalDate.now()
    return when (this) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        else -> format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
    }
}

private fun Duration.formatFastAddDuration(): String {
    val hours = toHours()
    val minutes = minusHours(hours).toMinutes()
    return when {
        hours > 0 && minutes > 0 -> "${hours} h ${minutes} min"
        hours > 0 -> "${hours} h"
        else -> "${minutes.coerceAtLeast(0)} min"
    }
}

private fun LocalDate.toTaskDraftTimeSlot(): TimeSlot {
    return CalendarDraftTimeSlot(
        startTime = atTime(10, 0),
        endTime = atTime(10, 30),
    )
}

private data class CalendarDraftTimeSlot(
    override val startTime: LocalDateTime,
    override val endTime: LocalDateTime,
) : TimeSlot

@Preview(name = "Calendar screen", widthDp = 412, heightDp = 892, apiLevel = 35, showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    val selectedDate = LocalDate.of(2026, 4, 17)
    SmartSchedulerTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CalendarScreen(
                uiState = previewCalendarUiState(selectedDate),
                onAction = {},
            )
        }
    }
}

@Preview(name = "Calendar empty day", widthDp = 412, heightDp = 892, apiLevel = 35, showBackground = true)
@Composable
private fun CalendarScreenEmptyPreview() {
    val selectedDate = LocalDate.of(2026, 4, 20)
    SmartSchedulerTheme(dynamicColor = false) {
        CalendarScreen(
            uiState = previewCalendarUiState(selectedDate, emptyItems = true),
            onAction = {},
        )
    }
}

private fun previewCalendarUiState(
    selectedDate: LocalDate,
    emptyItems: Boolean = false,
): CalendarUiState.Success {
    val yearMonth = java.time.YearMonth.from(selectedDate)
    val markers = mapOf(
        LocalDate.of(2026, 4, 1) to CalendarDayMarker(taskCount = 2, eventCount = 1, hasHighPriorityTask = true),
        LocalDate.of(2026, 4, 2) to CalendarDayMarker(taskCount = 1, eventCount = 1),
        LocalDate.of(2026, 4, 3) to CalendarDayMarker(taskCount = 1, eventCount = 2),
    )

    val items = if (emptyItems) {
        emptyList()
    } else {
        listOf(
            CalendarAgendaItemUiModel.TaskItem(
                id = "task-1",
                title = "Card design",
                description = null,
                startTime = selectedDate.atTime(10, 0),
                endTime = selectedDate.atTime(12, 0),
                duration = Duration.ofHours(2),
                status = Status.SCHEDULED,
                priority = Priority.HIGH,
            ),
            CalendarAgendaItemUiModel.EventItem(
                id = "event-1",
                title = "Some Event",
                description = null,
                startTime = selectedDate.atTime(10, 0),
                endTime = selectedDate.atTime(12, 0),
                duration = Duration.ofHours(2),
            ),
        )
    }

    return CalendarUiState.Success(
        selectedDate = selectedDate,
        visibleYear = selectedDate.year,
        visibleMonth = selectedDate.month,
        yearOptions = (selectedDate.year - 5..selectedDate.year + 5).toList(),
        months = Month.values().map { month ->
            CalendarMonthChipUiModel(
                month = month,
                label = month.name.lowercase().replaceFirstChar { it.uppercase() },
                selected = month == selectedDate.month,
            )
        },
        days = buildCalendarMonthDays(
            yearMonth = yearMonth,
            selectedDate = selectedDate,
            today = selectedDate,
            markers = markers,
        ),
        selectedDayItems = items,
        suggestedTaskTimeSlot = selectedDate.toTaskDraftTimeSlot(),
        suggestedEventTimeSlot = selectedDate.toTaskDraftTimeSlot(),
        weekStartsOnMonday = true,
    )
}
