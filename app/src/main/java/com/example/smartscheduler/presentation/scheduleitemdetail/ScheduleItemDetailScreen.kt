@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.smartscheduler.presentation.scheduleitemdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smartscheduler.R
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.presentation.components.SmartLoadingCircularIndicator
import com.example.smartscheduler.presentation.components.SmartPriorityChip
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private object DetailSpacing {
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val ExtraLarge = 24.dp
    val ContentTop = 32.dp
    val SectionGap = 32.dp
}

private object DetailDimensions {
    val ContentMaxWidth = 640.dp
    val TopAppBarHeight = 64.dp
    val SaveButtonHeight = 48.dp
    val BottomButtonHeight = 48.dp
    val PropertyRowMinHeight = 64.dp
    val PropertyIconSlot = 48.dp
    val PropertyIconSize = 32.dp
    val PriorityChipWidth = 120.dp
}

private const val DescriptionCollapsedMaxLines = 4
private const val DescriptionExpandedMaxLines = 8
private const val DescriptionCollapsedLengthThreshold = 120

private sealed interface DetailDialogState {
    data object None : DetailDialogState
    data object PickDate : DetailDialogState
    data object PickDeadlineDate : DetailDialogState
    data object PickTaskDuration : DetailDialogState
    data object PickTaskStartTime : DetailDialogState
    data object PickEventTimeChoice : DetailDialogState
    data class PickEventTimeTarget(val target: ScheduleItemTimeTarget) : DetailDialogState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleItemDetailRoute(
    viewModel: ScheduleItemDetailViewModel,
    args: ScheduleItemDetailArgs,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(args) {
        viewModel.initialize(args)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ScheduleItemDetailEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    ScheduleItemDetailScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleItemDetailScreen(
    uiState: ScheduleItemDetailUiState,
    onAction: (ScheduleItemDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var dialogState by remember { mutableStateOf<DetailDialogState>(DetailDialogState.None) }

    val content = uiState as? ScheduleItemDetailUiState.Content

    LaunchedEffect(content?.errorMessage) {
        content?.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Top
        ),
        topBar = {
            ScheduleItemTopBar(
                uiState = uiState,
                onAction = onAction,
            )
        },
        bottomBar = {
            if (content != null && (content.canDelete || content.canMarkCompleted)) {
                ScheduleItemBottomActions(
                    content = content,
                    onAction = onAction,
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (uiState) {
                ScheduleItemDetailUiState.Loading -> DetailLoadingContent()

                is ScheduleItemDetailUiState.Error -> DetailErrorContent(
                    message = uiState.message,
                    onRetry = { onAction(ScheduleItemDetailAction.Retry) },
                    onClose = { onAction(ScheduleItemDetailAction.Close) },
                )

                is ScheduleItemDetailUiState.Content -> ScheduleItemDetailContent(
                    content = uiState,
                    onAction = onAction,
                    onOpenDatePicker = { dialogState = DetailDialogState.PickDate },
                    onOpenDeadlinePicker = { dialogState = DetailDialogState.PickDeadlineDate },
                    onOpenTaskDurationPicker = { dialogState = DetailDialogState.PickTaskDuration },
                    onOpenTaskStartTimePicker = { dialogState = DetailDialogState.PickTaskStartTime },
                    onOpenEventTimeChoice = { dialogState = DetailDialogState.PickEventTimeChoice },
                )
            }
        }
    }

    when (val state = dialogState) {
        DetailDialogState.None -> Unit

        DetailDialogState.PickDate -> {
            if (content != null) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = content.date
                        .atStartOfDay()
                        .toInstant(ZoneOffset.UTC)
                        .toEpochMilli(),
                )
                val confirmEnabled = remember(datePickerState) {
                    androidx.compose.runtime.derivedStateOf {
                        datePickerState.selectedDateMillis != null
                    }
                }

                DatePickerDialog(
                    onDismissRequest = { dialogState = DetailDialogState.None },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val selectedMillis = datePickerState.selectedDateMillis ?: return@TextButton
                                val selectedDate = Instant.ofEpochMilli(selectedMillis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                                onAction(ScheduleItemDetailAction.DateChanged(selectedDate))
                                dialogState = DetailDialogState.None
                            },
                            enabled = confirmEnabled.value,
                        ) {
                            Text(stringResource(R.string.common_ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { dialogState = DetailDialogState.None }) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    },
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }

        DetailDialogState.PickDeadlineDate -> {
            if (content != null) {
                val initialDate = content.deadlineDate ?: LocalDate.now()
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = initialDate
                        .atStartOfDay()
                        .toInstant(ZoneOffset.UTC)
                        .toEpochMilli(),
                )
                val confirmEnabled = remember(datePickerState) {
                    androidx.compose.runtime.derivedStateOf {
                        datePickerState.selectedDateMillis != null
                    }
                }

                DatePickerDialog(
                    onDismissRequest = { dialogState = DetailDialogState.None },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val selectedMillis = datePickerState.selectedDateMillis ?: return@TextButton
                                val selectedDate = Instant.ofEpochMilli(selectedMillis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                                onAction(ScheduleItemDetailAction.DeadlineChanged(selectedDate))
                                dialogState = DetailDialogState.None
                            },
                            enabled = confirmEnabled.value,
                        ) {
                            Text(stringResource(R.string.common_ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { dialogState = DetailDialogState.None }) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    },
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }

        DetailDialogState.PickTaskDuration -> {
            if (content != null) {
                DurationTimePickerDialog(
                    currentDuration = content.taskDuration,
                    onDurationSelected = { duration ->
                        onAction(ScheduleItemDetailAction.TaskDurationChanged(duration))
                        dialogState = DetailDialogState.None
                    },
                    onDismiss = { dialogState = DetailDialogState.None },
                )
            }
        }

        DetailDialogState.PickTaskStartTime -> {
            if (content != null) {
                TimePickerForTargetDialog(
                    title = stringResource(R.string.detail_task_start_time),
                    currentTime = content.taskStartTime ?: LocalTime.now(),
                    onConfirm = { time ->
                        onAction(ScheduleItemDetailAction.TaskStartTimeChanged(time))
                        dialogState = DetailDialogState.None
                    },
                    onDismiss = { dialogState = DetailDialogState.None },
                )
            }
        }

        DetailDialogState.PickEventTimeChoice -> {
            if (content != null) {
                EventTimeChoiceDialog(
                    startTime = content.eventStartTime,
                    endTime = content.eventEndTime,
                    onPickStart = {
                        dialogState = DetailDialogState.PickEventTimeTarget(
                            ScheduleItemTimeTarget.EVENT_START,
                        )
                    },
                    onPickEnd = {
                        dialogState = DetailDialogState.PickEventTimeTarget(
                            ScheduleItemTimeTarget.EVENT_END,
                        )
                    },
                    onDismiss = { dialogState = DetailDialogState.None },
                )
            }
        }

        is DetailDialogState.PickEventTimeTarget -> {
            if (content != null) {
                val currentTime = when (state.target) {
                    ScheduleItemTimeTarget.EVENT_START -> content.eventStartTime
                    ScheduleItemTimeTarget.EVENT_END -> content.eventEndTime
                }

                TimePickerForTargetDialog(
                    title = if (state.target == ScheduleItemTimeTarget.EVENT_START) {
                        stringResource(R.string.detail_event_start_time)
                    } else {
                        stringResource(R.string.detail_event_end_time)
                    },
                    currentTime = currentTime,
                    onConfirm = { time ->
                        when (state.target) {
                            ScheduleItemTimeTarget.EVENT_START -> onAction(
                                ScheduleItemDetailAction.EventStartTimeChanged(time)
                            )

                            ScheduleItemTimeTarget.EVENT_END -> onAction(
                                ScheduleItemDetailAction.EventEndTimeChanged(time)
                            )
                        }
                        dialogState = DetailDialogState.None
                    },
                    onDismiss = { dialogState = DetailDialogState.None },
                )
            }
        }
    }
}

@Composable
private fun ScheduleItemTopBar(
    uiState: ScheduleItemDetailUiState,
    onAction: (ScheduleItemDetailAction) -> Unit,
) {
    val content = uiState as? ScheduleItemDetailUiState.Content

    TopAppBar(
        modifier = Modifier.height(DetailDimensions.TopAppBarHeight),
        navigationIcon = {
            IconButton(onClick = { onAction(ScheduleItemDetailAction.Close) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_app_close),
                    contentDescription = stringResource(R.string.detail_close_content_description),
                )
            }
        },
        title = {},
        actions = {
            Button(
                onClick = { onAction(ScheduleItemDetailAction.Save) },
                enabled = content?.canSave == true,
                modifier = Modifier
                    .height(DetailDimensions.SaveButtonHeight)
                    .padding(end = DetailSpacing.Small),
                contentPadding = PaddingValues(horizontal = DetailSpacing.Large),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_app_check_small),
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Text(
                    text = if (content?.isSaving == true) {
                        stringResource(R.string.common_saving)
                    } else {
                        stringResource(R.string.common_save)
                    },
                    modifier = Modifier.padding(start = DetailSpacing.Small),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

@Composable
private fun ScheduleItemDetailContent(
    content: ScheduleItemDetailUiState.Content,
    onAction: (ScheduleItemDetailAction) -> Unit,
    onOpenDatePicker: () -> Unit,
    onOpenDeadlinePicker: () -> Unit,
    onOpenTaskDurationPicker: () -> Unit,
    onOpenTaskStartTimePicker: () -> Unit,
    onOpenEventTimeChoice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    var descriptionExpanded by remember(content.itemId) { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = DetailDimensions.ContentMaxWidth)
            .verticalScroll(scrollState)
            .padding(
                start = DetailSpacing.Large,
                top = DetailSpacing.ContentTop,
                end = DetailSpacing.Large,
                bottom = DetailSpacing.Large,
            ),
    ) {
        TextField(
            value = content.title,
            onValueChange = { onAction(ScheduleItemDetailAction.TitleChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            placeholder = {
                Text(
                    text = if (content.itemKind == ScheduleItemKind.EVENT) {
                        stringResource(R.string.detail_add_event_title)
                    } else {
                        stringResource(R.string.detail_add_task_title)
                    },
                    style = MaterialTheme.typography.headlineMedium,
                )
            },
            singleLine = true,
            colors = detailTextFieldColors(),
        )

        Spacer(modifier = Modifier.height(DetailSpacing.SectionGap))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            ScheduleItemKind.entries.forEachIndexed { index, kind ->
                SegmentedButton(
                    selected = content.itemKind == kind,
                    onClick = { onAction(ScheduleItemDetailAction.KindChanged(kind)) },
                    enabled = content.canChangeKind,
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ScheduleItemKind.entries.size,
                    ),
                    label = {
                        Text(
                            text = kind.title(),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(DetailSpacing.SectionGap))

        DescriptionContent(
            description = content.description,
            expanded = descriptionExpanded,
            onExpandedChange = { descriptionExpanded = it },
            onDescriptionChange = { onAction(ScheduleItemDetailAction.DescriptionChanged(it)) },
        )

        Spacer(modifier = Modifier.height(DetailSpacing.Small))

        Column(verticalArrangement = Arrangement.spacedBy(DetailSpacing.Small)) {
            when (content.itemKind) {
                ScheduleItemKind.TASK -> DetailTaskScheduleDateItem(
                    scheduleDate = if (content.isScheduledTask) content.date else null,
                    onClick = onOpenDatePicker,
                )

                ScheduleItemKind.EVENT -> DetailListItem(
                    iconResId = R.drawable.ic_app_bottom_nav_calendar,
                    title = stringResource(R.string.detail_date),
                    value = formatDetailDate(content.date),
                    onClick = onOpenDatePicker,
                )
            }

            when (content.itemKind) {
                ScheduleItemKind.TASK -> {
                    DetailDeadlineItem(
                        deadlineDate = content.deadlineDate,
                        onClick = onOpenDeadlinePicker,
                    )

                    DetailListItem(
                        iconResId = R.drawable.ic_app_chip_clock,
                        title = stringResource(R.string.detail_duration),
                        value = if (content.isScheduledTask && content.taskStartTime != null) {
                            formatTimeRange(
                                start = content.date.atTime(content.taskStartTime),
                                end = content.date.atTime(content.taskStartTime).plus(content.taskDuration),
                            )
                        } else {
                            formatDuration(content.taskDuration)
                        },
                        onClick = onOpenTaskDurationPicker,
                    )

                    DetailToggleItem(
                        iconResId = R.drawable.ic_app_lock,
                        title = stringResource(R.string.detail_lock_task),
                        checked = content.isLocked,
                        onCheckedChange = { onAction(ScheduleItemDetailAction.LockChanged(it)) },
                    )

                    PriorityRow(
                        priority = content.priority,
                        onPriorityClick = {
                            onAction(ScheduleItemDetailAction.PriorityChanged(content.priority.nextPriority()))
                        },
                    )
                }

                ScheduleItemKind.EVENT -> {
                    DetailListItem(
                        iconResId = R.drawable.ic_app_chip_clock,
                        title = stringResource(R.string.detail_duration),
                        value = formatTimeRange(
                            start = content.date.atTime(content.eventStartTime),
                            end = content.date.atTime(content.eventEndTime),
                        ),
                        onClick = onOpenEventTimeChoice,
                    )

                    if (!content.isEventTimeValid) {
                        Text(
                            text = stringResource(R.string.detail_end_after_start),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = DetailSpacing.ExtraLarge),
                        )
                    }
                }
            }
        }

        if (content.errorMessage != null) {
            Text(
                text = content.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun DescriptionContent(
    description: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasOverflow = description.length > DescriptionCollapsedLengthThreshold

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DetailSpacing.ExtraSmall),
    ) {
        if (description.isNotBlank() && !expanded) {
            Text(
                text = description,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(true) },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = DescriptionCollapsedMaxLines,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            TextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                placeholder = {
                    Text(
                        text = stringResource(R.string.detail_add_description),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                minLines = 1,
                maxLines = if (expanded) DescriptionExpandedMaxLines else DescriptionCollapsedMaxLines,
                colors = detailTextFieldColors(),
            )
        }

        if (hasOverflow) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { onExpandedChange(!expanded) }) {
                    Text(
                        text = if (expanded) {
                            stringResource(R.string.detail_show_less)
                        } else {
                            stringResource(R.string.detail_view_more)
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailListItem(
    iconResId: Int,
    title: String,
    value: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    DetailPropertyRow(
        iconResId = iconResId,
        title = title,
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DetailTaskScheduleDateItem(
    scheduleDate: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailPropertyRow(
        iconResId = R.drawable.ic_app_bottom_nav_calendar,
        title = stringResource(R.string.detail_schedule_date),
        onClick = if (scheduleDate != null) onClick else null,
        modifier = modifier,
    ) {
        if (scheduleDate == null) {
            TextButton(onClick = onClick) {
                Text(stringResource(R.string.common_add_lowercase))
            }
        } else {
            Text(
                text = formatDetailDate(scheduleDate),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DetailDeadlineItem(
    deadlineDate: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailPropertyRow(
        iconResId = R.drawable.ic_app_bottom_nav_calendar,
        title = stringResource(R.string.detail_deadline),
        onClick = if (deadlineDate != null) onClick else null,
        modifier = modifier,
    ) {
        if (deadlineDate == null) {
            TextButton(onClick = onClick) {
                Text(stringResource(R.string.common_add_lowercase))
            }
        } else {
            Text(
                text = formatDetailDate(deadlineDate),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DetailToggleItem(
    iconResId: Int,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailPropertyRow(
        iconResId = iconResId,
        title = title,
        modifier = modifier,
    ) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PriorityRow(
    priority: Priority,
    onPriorityClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailPropertyRow(
        iconResId = R.drawable.ic_app_general_priority,
        title = stringResource(R.string.detail_priority),
        modifier = modifier,
    ) {
        SmartPriorityChip(
            priority = priority,
            onClick = onPriorityClick,
        )
    }
}

@Composable
private fun DetailPropertyRow(
    iconResId: Int,
    title: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit,
) {
    val rowModifier = modifier
        .fillMaxWidth()
        .heightIn(min = DetailDimensions.PropertyRowMinHeight)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DetailSpacing.Small),
    ) {
        Box(
            modifier = Modifier.width(DetailDimensions.PropertyIconSlot),
            contentAlignment = Alignment.CenterStart,
        ) {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = null,
                modifier = Modifier.size(DetailDimensions.PropertyIconSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Box(
            modifier = Modifier.weight(1.45f),
            contentAlignment = Alignment.CenterEnd,
        ) {
            trailingContent()
        }
    }
}

@Composable
private fun ScheduleItemBottomActions(
    content: ScheduleItemDetailUiState.Content,
    onAction: (ScheduleItemDetailAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = DetailSpacing.Large,
                top = DetailSpacing.Small,
                end = DetailSpacing.Large,
                bottom = DetailSpacing.Large,
            ),
        verticalArrangement = Arrangement.spacedBy(DetailSpacing.Small),
    ) {
        HorizontalDivider()

        when {
            content.canDelete && content.canMarkCompleted -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DetailSpacing.Small),
                ) {
                    Button(
                        onClick = { onAction(ScheduleItemDetailAction.Delete) },
                        modifier = Modifier
                            .weight(1f)
                            .height(DetailDimensions.BottomButtonHeight),
                        contentPadding = PaddingValues(horizontal = DetailSpacing.Large),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_app_delete),
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                        Text(
                            text = stringResource(R.string.common_delete),
                            modifier = Modifier.padding(start = DetailSpacing.Small),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                        )
                    }

                    Button(
                        onClick = { onAction(ScheduleItemDetailAction.MarkCompleted) },
                        modifier = Modifier
                            .weight(1f)
                            .height(DetailDimensions.BottomButtonHeight),
                        contentPadding = PaddingValues(horizontal = DetailSpacing.Large),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_app_check_small),
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                        Text(
                            text = if (content.status == Status.COMPLETED) {
                                stringResource(R.string.detail_mark_pending)
                            } else {
                                stringResource(R.string.detail_mark_completed)
                            },
                            modifier = Modifier.padding(start = DetailSpacing.Small),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                        )
                    }
                }
            }

            content.canDelete -> {
                Button(
                    onClick = { onAction(ScheduleItemDetailAction.Delete) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DetailDimensions.BottomButtonHeight),
                    contentPadding = PaddingValues(horizontal = DetailSpacing.Large),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_app_delete),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Text(
                        text = stringResource(R.string.common_delete),
                        modifier = Modifier.padding(start = DetailSpacing.Small),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        SmartLoadingCircularIndicator(isLoading = true)
    }
}

@Composable
private fun DetailErrorContent(
    message: String,
    onRetry: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DetailSpacing.ExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DetailSpacing.Medium),
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(DetailSpacing.Small)) {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.common_close))
            }
            Button(onClick = onRetry) {
                Text(stringResource(R.string.common_retry))
            }
        }
    }
}

@Composable
private fun DurationTimePickerDialog(
    currentDuration: Duration,
    onDurationSelected: (Duration) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialHour = currentDuration.toHours().coerceIn(0, 23).toInt()
    val initialMinute = currentDuration.minusHours(currentDuration.toHours()).toMinutes().coerceIn(0, 59).toInt()
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )

    TimePickerDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.detail_task_duration)) },
        confirmButton = {
            TextButton(
                onClick = {
                    val duration = Duration.ofHours(state.hour.toLong())
                        .plusMinutes(state.minute.toLong())
                    if (!duration.isZero) {
                        onDurationSelected(duration)
                    }
                },
            ) {
                Text(stringResource(R.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    ) {
        if (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp > 480) {
            TimePicker(state = state)
        } else {
            TimeInput(state = state)
        }
    }
}

@Composable
private fun TimePickerForTargetDialog(
    title: String,
    currentTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = currentTime.hour,
        initialMinute = currentTime.minute,
        is24Hour = true,
    )

    TimePickerDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(LocalTime.of(state.hour, state.minute))
                },
            ) {
                Text(stringResource(R.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    ) {
        if (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp > 480) {
            TimePicker(state = state)
        } else {
            TimeInput(state = state)
        }
    }
}

@Composable
private fun EventTimeChoiceDialog(
    startTime: LocalTime,
    endTime: LocalTime,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit,
    onDismiss: () -> Unit,
) {
    val timeFormatter = DateTimeFormatter.ofPattern(
        stringResource(R.string.time_format_24h),
        LocalLocale.current.platformLocale,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.detail_event_time)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(DetailSpacing.Small)) {
                Text(
                    text = stringResource(R.string.detail_event_time_supporting),
                    style = MaterialTheme.typography.bodyMedium,
                )
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPickStart() },
                    headlineContent = { Text(stringResource(R.string.detail_start_time)) },
                    trailingContent = {
                        Text(startTime.format(timeFormatter))
                    },
                )
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPickEnd() },
                    headlineContent = { Text(stringResource(R.string.detail_end_time)) },
                    trailingContent = {
                        Text(endTime.format(timeFormatter))
                    },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_close))
            }
        },
    )
}

@Composable
private fun detailTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    errorContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    errorIndicatorColor = Color.Transparent,
    cursorColor = MaterialTheme.colorScheme.primary,
)

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
private fun formatTimeRange(start: LocalDateTime, end: LocalDateTime): String {
    val duration = Duration.between(start, end).abs()
    val timeFormatter = DateTimeFormatter.ofPattern(
        stringResource(R.string.time_format_24h),
        LocalLocale.current.platformLocale,
    )
    return stringResource(
        R.string.common_time_range_duration,
        start.toLocalTime().format(timeFormatter),
        end.toLocalTime().format(timeFormatter),
        formatDuration(duration),
    )
}

@Composable
private fun formatDetailDate(date: LocalDate): String {
    val pattern = stringResource(R.string.date_format_detail)
    return date.format(DateTimeFormatter.ofPattern(pattern, LocalLocale.current.platformLocale))
}

@Composable
private fun ScheduleItemKind.title(): String {
    return when (this) {
        ScheduleItemKind.TASK -> stringResource(R.string.detail_task_kind)
        ScheduleItemKind.EVENT -> stringResource(R.string.detail_event_kind)
    }
}

private fun Priority.nextPriority(): Priority {
    return when (this) {
        Priority.LOW -> Priority.MEDIUM
        Priority.MEDIUM -> Priority.HIGH
        Priority.HIGH -> Priority.LOW
    }
}

@Preview(name = "Create task detail", widthDp = 393, apiLevel = 35, showBackground = true)
@Composable
private fun CreateTaskDetailPreview() {
    SmartSchedulerTheme(dynamicColor = false) {
        ScheduleItemDetailScreen(
            uiState = ScheduleItemDetailUiState.Content(
                mode = ScheduleItemDetailMode.CREATE,
                itemKind = ScheduleItemKind.TASK,
                itemId = null,
                title = "Super Important Task",
                description = "",
                date = LocalDate.of(2026, 5, 10),
                deadlineDate = null,
                taskDuration = Duration.ofMinutes(75),
                taskStartTime = null,
                isScheduledTask = false,
                eventStartTime = LocalTime.of(12, 0),
                eventEndTime = LocalTime.of(14, 0),
                isLocked = false,
                priority = Priority.HIGH,
                status = Status.PENDING,
                canSave = true,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Create event detail", widthDp = 393, apiLevel = 35, showBackground = true)
@Composable
private fun CreateEventDetailPreview() {
    SmartSchedulerTheme(dynamicColor = false) {
        ScheduleItemDetailScreen(
            uiState = ScheduleItemDetailUiState.Content(
                mode = ScheduleItemDetailMode.CREATE,
                itemKind = ScheduleItemKind.EVENT,
                itemId = null,
                title = "Team sync",
                description = "Discuss roadmap and blockers.",
                date = LocalDate.of(2026, 5, 10),
                deadlineDate = null,
                taskDuration = Duration.ofMinutes(30),
                taskStartTime = null,
                isScheduledTask = false,
                eventStartTime = LocalTime.of(12, 0),
                eventEndTime = LocalTime.of(14, 0),
                isLocked = false,
                priority = Priority.MEDIUM,
                status = Status.PENDING,
                canSave = true,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Edit task detail", widthDp = 393, apiLevel = 35, showBackground = true)
@Composable
private fun EditTaskDetailPreview() {
    SmartSchedulerTheme(dynamicColor = false) {
        ScheduleItemDetailScreen(
            uiState = ScheduleItemDetailUiState.Content(
                mode = ScheduleItemDetailMode.EDIT,
                itemKind = ScheduleItemKind.TASK,
                itemId = "task-1",
                title = "Super Important Task",
                description = "Funny description provided Funny description provided Funny description provided",
                date = LocalDate.of(2026, 5, 10),
                deadlineDate = LocalDate.of(2026, 5, 12),
                taskDuration = Duration.ofHours(2),
                taskStartTime = LocalTime.of(12, 0),
                isScheduledTask = true,
                eventStartTime = LocalTime.of(12, 0),
                eventEndTime = LocalTime.of(14, 0),
                isLocked = true,
                priority = Priority.HIGH,
                status = Status.SCHEDULED,
                canSave = true,
            ),
            onAction = {},
        )
    }
}
