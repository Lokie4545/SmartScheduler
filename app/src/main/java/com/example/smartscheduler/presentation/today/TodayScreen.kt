package com.example.smartscheduler.presentation.today

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smartscheduler.R
import com.example.smartscheduler.domain.model.Event
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.TimeSlot
import com.example.smartscheduler.presentation.components.SmartLoadingCircularIndicator
import com.example.smartscheduler.presentation.components.SmartPriorityChip
import com.example.smartscheduler.presentation.components.SmartTaskCard
import com.example.smartscheduler.presentation.scheduleitemdetail.ScheduleItemKind
import com.example.smartscheduler.presentation.smartreschedule.SmartReschedulePreviewBottomSheetRoute
import com.example.smartscheduler.presentation.smartreschedule.SmartRescheduleViewModel
import com.example.smartscheduler.presentation.today.components.FastAddEventBottomSheet
import com.example.smartscheduler.presentation.today.components.FastAddTaskBottomSheet
import com.example.smartscheduler.presentation.today.components.SmartFabMenu
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private object TodaySpacing {
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val ExtraLarge = 24.dp
    val Huge = 32.dp
    val ContentBottom = 112.dp
}

private object TodayDimensions {
    val HeaderHeight = 176.dp
    val SummaryIcon = 32.dp
    val SectionIcon = 32.dp
    val TaskMinHeight = 72.dp
}

@Composable
fun TodayRoute(
    viewModel: TodayViewModel,
    smartRescheduleViewModel: SmartRescheduleViewModel,
    onNavigateToScheduleItemCreate: (ScheduleItemKind, String, String, TimeSlot?) -> Unit,
    onNavigateToScheduleItemEdit: (ScheduleItemKind, String) -> Unit,
    onNavigateToSmartRescheduleDiff: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSmartRescheduleSheet by remember { mutableStateOf(false) }

    TodayScreen(uiState = uiState) { action ->
        when (action) {
            is TodayAction.NavigateToScheduleItemCreate -> {
                onNavigateToScheduleItemCreate(
                    action.kind,
                    action.draftTitle,
                    action.draftDescription,
                    action.draftTimeSlot,
                )
            }

            is TodayAction.NavigateToScheduleItemEdit -> {
                onNavigateToScheduleItemEdit(action.kind, action.itemId)
            }

            TodayAction.RequestSmartReschedule -> {
                showSmartRescheduleSheet = true
            }

            else -> viewModel.handleAction(action)
        }
    }

    if (showSmartRescheduleSheet) {
        SmartReschedulePreviewBottomSheetRoute(
            viewModel = smartRescheduleViewModel,
            onDismissRequest = { showSmartRescheduleSheet = false },
            onViewChanges = {
                showSmartRescheduleSheet = false
                onNavigateToSmartRescheduleDiff()
            },
        )
    }
}

sealed interface TodayBottomSheetConfig {
    data object None : TodayBottomSheetConfig
    data class AddTask(val defaultDuration: Duration) : TodayBottomSheetConfig
    data class AddEvent(val defaultTimeSlot: TimeSlot) : TodayBottomSheetConfig
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    modifier: Modifier = Modifier, uiState: TodayUiState, onAction: (TodayAction) -> Unit
) {
    var bottomSheetConfig by remember {
        mutableStateOf<TodayBottomSheetConfig>(TodayBottomSheetConfig.None)
    }

    val datePattern = stringResource(R.string.date_format_full)
    val dateFormatter = remember(datePattern) {
        DateTimeFormatter.ofPattern(datePattern, Locale.getDefault())
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Top
        ),
        topBar = {
            TopAppBar(
                title = {
                    val title = uiState.currentDate.format(dateFormatter)
                    Text(
                        text = title,
                        modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (uiState is TodayUiState.Success) {
                SmartFabMenu(
                    modifier = Modifier.padding(
                        end = TodaySpacing.Small, bottom = TodaySpacing.Small
                    ), onAddTaskClick = {
                        bottomSheetConfig = TodayBottomSheetConfig.AddTask(uiState.defaultTaskDuration)
                    }, onAddEventClick = {
                        bottomSheetConfig = TodayBottomSheetConfig.AddEvent(
                            uiState.suggestedEventTimeSlot
                        )
                    })
            }
        }) { innerPadding ->
        when (uiState) {
            is TodayUiState.Loading -> TodayLoadingContent(
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize()
            )

            is TodayUiState.Error -> TodayErrorContent(
                message = uiState.message,
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize()
            )

            is TodayUiState.Success -> TodayContent(
                uiState = uiState,
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
                onRescheduleClick = { onAction(TodayAction.RequestSmartReschedule) },
                onTaskCheckedChange = { taskId, _ ->
                    onAction(TodayAction.MarkTaskCompleted(taskId))
                },
                onTaskClick = { task ->
                    onAction(TodayAction.NavigateToScheduleItemEdit(ScheduleItemKind.TASK, task.id))
                },
                onEventClick = { event ->
                    onAction(TodayAction.NavigateToScheduleItemEdit(ScheduleItemKind.EVENT, event.id))
                },
            )
        }

        when (val config = bottomSheetConfig) {
            is TodayBottomSheetConfig.AddTask -> {
                FastAddTaskBottomSheet(
                    dateLabel = stringResource(R.string.common_today),
                    durationLabel = formatDuration(config.defaultDuration),
                    onDismissRequest = {
                    onAction(TodayAction.DismissFastAddRequest)
                    bottomSheetConfig = TodayBottomSheetConfig.None
                }, onSaveDefaultTask = { title, description ->
                    onAction(
                        TodayAction.AddQuickTask(
                            title = title, description = description
                        )
                    )
                    bottomSheetConfig = TodayBottomSheetConfig.None
                }) { draftTitle, draftDescription ->
                    onAction(
                        TodayAction.NavigateToScheduleItemCreate(
                            kind = ScheduleItemKind.TASK,
                            draftTitle = draftTitle,
                            draftDescription = draftDescription,
                        )
                    )
                    bottomSheetConfig = TodayBottomSheetConfig.None
                }
            }

            is TodayBottomSheetConfig.AddEvent -> {
                FastAddEventBottomSheet(
                    dateLabel = stringResource(R.string.common_today),
                    onDismissRequest = {
                        onAction(TodayAction.DismissFastAddRequest)
                        bottomSheetConfig = TodayBottomSheetConfig.None
                    }, onSaveDefaultEvent = { title, description ->
                        onAction(
                            TodayAction.AddQuickEvent(
                                title = title,
                                description = description,
                                timeSlot = config.defaultTimeSlot
                            )
                        )
                        bottomSheetConfig = TodayBottomSheetConfig.None
                    }, currentTimeSlot = config.defaultTimeSlot
                ) { draftTitle, draftDescription ->
                    onAction(
                        TodayAction.NavigateToScheduleItemCreate(
                            kind = ScheduleItemKind.EVENT,
                            draftTitle = draftTitle,
                            draftDescription = draftDescription,
                            draftTimeSlot = config.defaultTimeSlot,
                        )
                    )
                    bottomSheetConfig = TodayBottomSheetConfig.None
                }
            }

            TodayBottomSheetConfig.None -> Unit
        }
    }
}

@Composable
private fun TodayLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier, contentAlignment = Alignment.Center
    ) {
        SmartLoadingCircularIndicator(true)
    }
}

@Composable
private fun TodayErrorContent(
    message: String, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(TodaySpacing.ExtraLarge), contentAlignment = Alignment.Center
    ) {
        Text(
            text = message.ifBlank { stringResource(R.string.today_error_fallback) },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun TodayContent(
    uiState: TodayUiState.Success,
    modifier: Modifier = Modifier,
    onRescheduleClick: () -> Unit,
    onTaskCheckedChange: (taskId: String, completed: Boolean) -> Unit,
    onTaskClick: (ScheduledTask) -> Unit,
    onEventClick: (Event) -> Unit,
) {
    val morningTitle = stringResource(R.string.today_section_morning)
    val afternoonTitle = stringResource(R.string.today_section_afternoon)

    LazyColumn(
        modifier = modifier, contentPadding = PaddingValues(bottom = TodaySpacing.ContentBottom)
    ) {
        item {
            UnscheduledSummaryCard(
                taskCount = uiState.unscheduledTaskCount,
                totalDuration = uiState.unscheduledDuration,
                onRescheduleClick = onRescheduleClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = TodaySpacing.Large,
                        top = TodaySpacing.ExtraLarge,
                        end = TodaySpacing.Large
                    )
            )
        }

        if (uiState.morningTasks.isEmpty() && uiState.afternoonTasks.isEmpty()) {
            item(key = "today_empty_state") {
                TodayEmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = TodaySpacing.Large,
                            top = TodaySpacing.ExtraLarge,
                            end = TodaySpacing.Large,
                        )
                )
            }
        }

        todayScheduleSection(
            title = morningTitle,
            iconResId = R.drawable.ic_app_section_morning,
            items = uiState.morningTasks,
            onTaskCheckedChange = onTaskCheckedChange,
            onTaskClick = onTaskClick,
            onEventClick = onEventClick,
            sectionTopPadding = TodaySpacing.ExtraLarge
        )

        todayScheduleSection(
            title = afternoonTitle,
            iconResId = R.drawable.ic_app_section_afternoon,
            items = uiState.afternoonTasks,
            onTaskCheckedChange = onTaskCheckedChange,
            onTaskClick = onTaskClick,
            onEventClick = onEventClick,
            sectionTopPadding = TodaySpacing.ExtraLarge
        )
    }
}

@Composable
private fun TodayEmptyState(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(TodaySpacing.ExtraLarge),
            verticalArrangement = Arrangement.spacedBy(TodaySpacing.Small),
        ) {
            Text(
                text = stringResource(R.string.today_empty_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.today_empty_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


@Composable
private fun UnscheduledSummaryCard(
    taskCount: Int,
    totalDuration: Duration,
    onRescheduleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier, shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = TodaySpacing.ExtraLarge,
                    top = TodaySpacing.Large,
                    end = TodaySpacing.Large,
                    bottom = TodaySpacing.Large
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TodaySpacing.Large)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_app_bottom_nav_today),
                contentDescription = null,
                modifier = Modifier.size(TodayDimensions.SummaryIcon),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(TodaySpacing.ExtraSmall)
            ) {
                Text(
                    text = pluralStringResource(
                        R.plurals.today_unscheduled_title,
                        taskCount,
                        taskCount,
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.today_unscheduled_total, formatDuration(totalDuration)),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onRescheduleClick, colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_app_reschedule_magic),
                    contentDescription = stringResource(R.string.today_smart_reschedule_content_description),
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )

            }
        }
    }
}

private fun LazyListScope.todayScheduleSection(
    title: String,
    @androidx.annotation.DrawableRes iconResId: Int,
    items: List<TimeSlot>,
    onTaskCheckedChange: (taskId: String, completed: Boolean) -> Unit,
    onTaskClick: (ScheduledTask) -> Unit,
    onEventClick: (Event) -> Unit,
    sectionTopPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    if (items.isEmpty()) return

    item(key = "section_header_$title") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = TodaySpacing.Large,
                    top = sectionTopPadding,
                    end = TodaySpacing.Large,
                    bottom = TodaySpacing.Medium
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TodaySpacing.Medium)
        ) {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = null,
                modifier = Modifier.size(TodayDimensions.SectionIcon),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    items(
        items, key = {
            when (it) {
                is ScheduledTask -> it.id
                is Event -> it.id
                else -> it.hashCode()
            }
        }) { item ->
        when (item) {
            is ScheduledTask -> TodayTaskRow(
                task = item,
                onCheckedChange = { completed -> onTaskCheckedChange(item.id, completed) },
                onClick = { onTaskClick(item) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = TodaySpacing.Large,
                        end = TodaySpacing.Large,
                        bottom = TodaySpacing.Medium
                    )
            )

            is Event -> TodayEventRow(
                event = item,
                onClick = { onEventClick(item) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = TodaySpacing.Large,
                        end = TodaySpacing.Large,
                        bottom = TodaySpacing.Medium
                    )
            )
        }

    }
}

@Composable
private fun TodayEventRow(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SmartTaskCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        onClick = onClick,
        leadingContent = {
            Icon(
                modifier = Modifier.padding(8.dp),
                painter = painterResource(R.drawable.ic_app_event),
                contentDescription = null
            )
        }) {
        Text(
            text = event.name,
            style = MaterialTheme.typography.titleSmallEmphasized,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = formatTimeSlotTime(event),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
private fun TodayTaskRow(
    task: ScheduledTask,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    SmartTaskCard(modifier = modifier, onClick = onClick, trailingContent = {
        SmartPriorityChip(
            priority = task.priority, modifier = Modifier.wrapContentWidth()
        )
    }, leadingContent = {
        Checkbox(
            checked = task.status == Status.COMPLETED,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }

    ) {
        Text(
            text = task.name,
            style = MaterialTheme.typography.titleSmallEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (task.status == Status.COMPLETED) {
                TextDecoration.LineThrough
            } else {
                null
            }
        )
        Text(
            text = formatTimeSlotTime(task),
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (task.status == Status.COMPLETED) {
                TextDecoration.LineThrough
            } else {
                null
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        task.deadline?.let { deadline ->
            Text(
                text = stringResource(R.string.today_task_deadline, formatTaskDeadlineDate(deadline)),
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (task.status == Status.COMPLETED) {
                    TextDecoration.LineThrough
                } else {
                    null
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun formatTimeSlotTime(task: TimeSlot): String {
    val timePattern = stringResource(R.string.time_format_24h)
    val formatter = remember(timePattern) { DateTimeFormatter.ofPattern(timePattern) }
    return "${task.startTime.format(formatter)} - ${task.endTime.format(formatter)} \u2022 ${
        formatDuration(
            task.duration
        )
    }"
}

@Composable
private fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()

    return when {
        hours > 0 && minutes > 0 -> stringResource(R.string.common_hour_minute, hours, minutes)
        hours > 0 -> stringResource(R.string.common_hour, hours)
        else -> stringResource(R.string.common_minute, minutes.coerceAtLeast(0))
    }
}

@Composable
private fun formatTaskDeadlineDate(deadline: LocalDateTime): String {
    val datePattern = stringResource(R.string.date_format_short)
    val formatter = remember(datePattern) { DateTimeFormatter.ofPattern(datePattern, Locale.getDefault()) }
    return deadline.toLocalDate().format(formatter)
}


private fun previewTasks(date: LocalDate = LocalDate.of(2026, 9, 1)): List<ScheduledTask> {
    return listOf(
        ScheduledTask(
            id = "card-design",
            name = "Card design",
            description = null,
            status = Status.SCHEDULED,
            priority = Priority.HIGH,
            isLocked = false,
            deadline = date.atTime(23, 59),
            preferredPlaceTime = null,
            startTime = date.atTime(10, 0),
            endTime = date.atTime(12, 0)
        ), ScheduledTask(
            id = "super-task-morning",
            name = "Super Task",
            description = null,
            status = Status.SCHEDULED,
            priority = Priority.MEDIUM,
            isLocked = false,
            deadline = null,
            preferredPlaceTime = null,
            startTime = date.atTime(12, 0),
            endTime = date.atTime(12, 10)
        ), ScheduledTask(
            id = "super-task-afternoon",
            name = "Super Task",
            description = null,
            status = Status.SCHEDULED,
            priority = Priority.HIGH,
            isLocked = false,
            deadline = null,
            preferredPlaceTime = null,
            startTime = date.atTime(15, 0),
            endTime = date.atTime(15, 10)
        )
    )
}


@Preview(name = "Unscheduled summary", widthDp = 393, apiLevel = 35, showBackground = true)
@Composable
private fun UnscheduledSummaryCardPreview() {
    SmartSchedulerTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            UnscheduledSummaryCard(
                taskCount = 3,
                totalDuration = Duration.ofHours(4).plusMinutes(30),
                onRescheduleClick = {},
                modifier = Modifier
                    .padding(TodaySpacing.Large)
                    .fillMaxWidth()
            )
        }
    }
}

@Preview(name = "Task section", widthDp = 393, apiLevel = 35, showBackground = true)
@Composable
private fun TodayTaskSectionPreview() {
    SmartSchedulerTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            LazyColumn {
                todayScheduleSection(
                    title = "Morning",
                    iconResId = R.drawable.ic_app_section_morning,
                    items = previewTasks().take(2),
                    onTaskCheckedChange = { _, _ -> },
                    onTaskClick = { _ -> },
                    onEventClick = { _ -> },
                    sectionTopPadding = TodaySpacing.Large
                )
            }
        }
    }
}

@Preview(name = "Task row", widthDp = 393, apiLevel = 35, showBackground = true)
@Composable
private fun TodayTaskRowPreview() {
    SmartSchedulerTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            TodayTaskRow(
                task = previewTasks().first(),
                onCheckedChange = {},
                onClick = {},
                modifier = Modifier.padding(TodaySpacing.Large)
            )
        }
    }
}
