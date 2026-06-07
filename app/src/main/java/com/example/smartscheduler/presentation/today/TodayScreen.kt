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
import androidx.compose.ui.res.painterResource
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
import com.example.smartscheduler.presentation.smartreschedule.SmartReschedulePreviewBottomSheetRoute
import com.example.smartscheduler.presentation.smartreschedule.SmartRescheduleViewModel
import com.example.smartscheduler.presentation.today.components.FastAddEventBottomSheet
import com.example.smartscheduler.presentation.today.components.FastAddTaskBottomSheet
import com.example.smartscheduler.presentation.today.components.SmartFabMenu
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme
import java.time.Duration
import java.time.LocalDate
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
    onNavigateToTaskDetail: (String, String) -> Unit,
    onNavigateToSmartRescheduleDiff: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSmartRescheduleSheet by remember { mutableStateOf(false) }

    TodayScreen(uiState = uiState) { action ->
        when (action) {
            is TodayAction.NavigateToTaskDetail -> {
                onNavigateToTaskDetail(action.draftTitle, action.draftDescription)
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
    data object AddTask : TodayBottomSheetConfig
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

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
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
                        bottomSheetConfig = TodayBottomSheetConfig.AddTask
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
                })
        }

        when (val config = bottomSheetConfig) {
            is TodayBottomSheetConfig.AddTask -> {
                FastAddTaskBottomSheet(onDismissRequest = {
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
                        TodayAction.NavigateToTaskDetail(
                            draftTitle, draftDescription
                        )
                    )
                    bottomSheetConfig = TodayBottomSheetConfig.None
                }
            }

            is TodayBottomSheetConfig.AddEvent -> {
                FastAddEventBottomSheet(
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
                        TodayAction.NavigateToTaskDetail(
                            draftTitle, draftDescription
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
            text = message.ifBlank { "Something went wrong" },
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
) {
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

        todayScheduleSection(
            title = "Morning",
            iconResId = R.drawable.ic_app_section_morning,
            items = uiState.morningTasks,
            onTaskCheckedChange = onTaskCheckedChange,
            sectionTopPadding = TodaySpacing.ExtraLarge
        )

        todayScheduleSection(
            title = "Afternoon",
            iconResId = R.drawable.ic_app_section_afternoon,
            items = uiState.afternoonTasks,
            onTaskCheckedChange = onTaskCheckedChange,
            sectionTopPadding = TodaySpacing.ExtraLarge
        )
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
                    text = formatUnscheduledTitle(taskCount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "~ ${formatDuration(totalDuration)} total",
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
                    contentDescription = "Smart reschedule backlog tasks",
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = TodaySpacing.Large,
                        end = TodaySpacing.Large,
                        bottom = TodaySpacing.Medium
                    )
            )

            is Event -> TodayEventRow(
                event = item, modifier = Modifier
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
private fun TodayEventRow(event: Event, modifier: Modifier = Modifier) {
    SmartTaskCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
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
    task: ScheduledTask, modifier: Modifier = Modifier, onCheckedChange: (Boolean) -> Unit
) {
    SmartTaskCard(modifier = modifier, trailingContent = {
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
    }
}

private fun formatTimeSlotTime(task: TimeSlot): String {
    val formatter = DateTimeFormatter.ofPattern("H:mm")
    return "${task.startTime.format(formatter)} - ${task.endTime.format(formatter)} \u2022 ${
        formatDuration(
            task.duration
        )
    }"
}

private fun formatUnscheduledTitle(taskCount: Int): String {
    val noun = if (taskCount == 1) "task" else "tasks"
    return "$taskCount unscheduled $noun"
}

private fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "$hours h"
        else -> "${minutes.coerceAtLeast(0)} min"
    }
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
            deadline = null,
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
                modifier = Modifier.padding(TodaySpacing.Large)
            )
        }
    }
}
