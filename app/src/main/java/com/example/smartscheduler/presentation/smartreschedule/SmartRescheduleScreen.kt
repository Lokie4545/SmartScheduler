package com.example.smartscheduler.presentation.smartreschedule

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smartscheduler.R
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.presentation.components.SmartLoadingCircularIndicator
import com.example.smartscheduler.presentation.components.SmartTaskCard
import java.time.LocalDate

private object SmartRescheduleSpacing {
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val ExtraLarge = 24.dp
}

private object SmartRescheduleDimensions {
    val ContentMaxWidth = 640.dp
    val CardHeight = 80.dp
    val SectionIcon = 28.dp
    val ChangeIcon = 18.dp
    val ActionIcon = 24.dp
    val BottomButtonHeight = 40.dp
}

@Composable
fun SmartRescheduleRoute(
    viewModel: SmartRescheduleViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SmartRescheduleEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    SmartRescheduleScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
    )
}

@Composable
fun SmartRescheduleScreen(
    uiState: SmartRescheduleUiState,
    onAction: (SmartRescheduleAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Top
        ),
        bottomBar = {
            SmartRescheduleBottomActions(
                uiState = uiState,
                onAction = onAction,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (uiState) {
                is SmartRescheduleUiState.Loading -> SmartRescheduleLoadingContent()
                is SmartRescheduleUiState.Error -> SmartRescheduleMessageContent(
                    message = uiState.message,
                    onRetry = { onAction(SmartRescheduleAction.Retry) },
                )
                is SmartRescheduleUiState.Empty -> SmartRescheduleMessageContent(
                    stringResource(R.string.smart_reschedule_no_changes)
                )
                is SmartRescheduleUiState.Success -> SmartRescheduleDiffContent(
                    uiState = uiState,
                    onAction = onAction,
                )
            }
        }
    }
}

@Composable
private fun SmartRescheduleDiffContent(
    uiState: SmartRescheduleUiState.Success,
    onAction: (SmartRescheduleAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val todayChanges = remember(uiState.changes) {
        uiState.changes.filter { it.type != SmartRescheduleChangeType.DEFERRED }
    }
    val scheduledDeferredGroups = remember(uiState.changes) {
        uiState.changes
            .filter { it.type == SmartRescheduleChangeType.DEFERRED && it.newStartTime != null }
            .groupBy { checkNotNull(it.newStartTime).toLocalDate() }
            .entries
            .sortedBy { it.key }
    }
    val unscheduledDeferredChanges = remember(uiState.changes) {
        uiState.changes.filter { it.type == SmartRescheduleChangeType.DEFERRED && it.newStartTime == null }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = SmartRescheduleDimensions.ContentMaxWidth),
        contentPadding = PaddingValues(
            start = SmartRescheduleSpacing.Large,
            top = SmartRescheduleSpacing.Large,
            end = SmartRescheduleSpacing.Large,
            bottom = SmartRescheduleSpacing.Large,
        ),
        verticalArrangement = Arrangement.spacedBy(SmartRescheduleSpacing.Medium),
    ) {
        item(key = "header") {
            SmartRescheduleHeader(
                summary = uiState.summary,
                onClose = { onAction(SmartRescheduleAction.Cancel) },
            )
        }

        if (todayChanges.isNotEmpty()) {
            item(key = "today_section") {
                SmartRescheduleSectionHeader(
                    title = uiState.currentDate.formatDiffSectionTitle(uiState.currentDate),
                    iconResId = R.drawable.ic_app_section_morning,
                )
            }
            items(
                items = todayChanges,
                key = { change -> "today_${change.taskId}_${change.type}" },
            ) { change ->
                SmartRescheduleChangeCard(
                    change = change,
                    onReject = { onAction(SmartRescheduleAction.RejectChange(change.taskId)) },
                    onRestore = { onAction(SmartRescheduleAction.RestoreChange(change.taskId)) },
                )
            }
        }

        scheduledDeferredGroups.forEach { (date, changes) ->
            item(key = "deferred_section_$date") {
                SmartRescheduleSectionHeader(
                    title = date.formatDiffSectionTitle(uiState.currentDate),
                    iconResId = R.drawable.ic_app_sun,
                )
            }
            items(
                items = changes,
                key = { change -> "deferred_${change.taskId}_${change.type}" },
            ) { change ->
                SmartRescheduleChangeCard(
                    change = change,
                    onReject = { onAction(SmartRescheduleAction.RejectChange(change.taskId)) },
                    onRestore = { onAction(SmartRescheduleAction.RestoreChange(change.taskId)) },
                )
            }
        }

        if (unscheduledDeferredChanges.isNotEmpty()) {
            item(key = "deferred_backlog_section") {
                SmartRescheduleSectionHeader(
                    title = stringResource(R.string.smart_reschedule_still_backlog),
                    iconResId = R.drawable.ic_app_circle_minus,
                )
            }
            items(
                items = unscheduledDeferredChanges,
                key = { change -> "deferred_backlog_${change.taskId}_${change.type}" },
            ) { change ->
                SmartRescheduleChangeCard(
                    change = change,
                    onReject = { onAction(SmartRescheduleAction.RejectChange(change.taskId)) },
                    onRestore = { onAction(SmartRescheduleAction.RestoreChange(change.taskId)) },
                )
            }
        }
    }
}

@Composable
private fun SmartRescheduleHeader(
    summary: SmartRescheduleSummaryUiModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SmartRescheduleSpacing.Small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SmartRescheduleSpacing.Medium),
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.ic_app_close),
                    contentDescription = stringResource(R.string.smart_reschedule_cancel_content_description),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = stringResource(R.string.smart_reschedule_proposed_schedule),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = summary.formatSummary(),
            modifier = Modifier.padding(start = SmartRescheduleSpacing.ExtraSmall),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SmartRescheduleSectionHeader(
    title: String,
    iconResId: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = SmartRescheduleSpacing.Large),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SmartRescheduleSpacing.Medium),
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            modifier = Modifier.size(SmartRescheduleDimensions.SectionIcon),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SmartRescheduleChangeCard(
    change: SmartRescheduleChangeUiModel,
    onReject: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (change.isRejected) {
        SmartRescheduleRejectedCard(
            change = change,
            onRestore = onRestore,
            modifier = modifier,
        )
        return
    }

    SmartTaskCard(
        modifier = modifier
            .fillMaxWidth()
            .height(SmartRescheduleDimensions.CardHeight),
        containerColor = change.containerColor(),
        leadingContent = {
            SmartRescheduleStatusChip(change.type.label())
        },
        trailingContent = {
            IconButton(onClick = onReject) {
                Icon(
                    painter = painterResource(R.drawable.ic_app_undo),
                    contentDescription = stringResource(
                        R.string.smart_reschedule_reject_content_description,
                        change.taskName,
                    ),
                    modifier = Modifier.size(SmartRescheduleDimensions.ActionIcon),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SmartRescheduleSpacing.ExtraSmall),
        ) {
            Icon(
                painter = painterResource(change.leadingIconResId()),
                contentDescription = null,
                modifier = Modifier.size(SmartRescheduleDimensions.ChangeIcon),
                tint = change.leadingIconTint(),
            )
            Text(
                text = change.taskName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = change.formatMeta(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = change.reason,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SmartRescheduleRejectedCard(
    change: SmartRescheduleChangeUiModel,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SmartTaskCard(
        modifier = modifier
            .fillMaxWidth()
            .height(SmartRescheduleDimensions.CardHeight),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        trailingContent = {
            IconButton(onClick = onRestore) {
                Icon(
                    painter = painterResource(R.drawable.ic_app_plus_circle),
                    contentDescription = stringResource(
                        R.string.smart_reschedule_restore_content_description,
                        change.taskName,
                    ),
                    modifier = Modifier.size(SmartRescheduleDimensions.ActionIcon),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) {
        Text(
            text = change.taskName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = TextDecoration.LineThrough,
        )
        Text(
            text = if (change.type == SmartRescheduleChangeType.ADDED || change.oldStartTime == null) {
                stringResource(R.string.smart_reschedule_task_returned_backlog)
            } else {
                stringResource(R.string.smart_reschedule_previous_slot_restored)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SmartRescheduleStatusChip(
    label: String,
    modifier: Modifier = Modifier,
) {
    AssistChip(
        onClick = {},
        enabled = false,
        modifier = modifier,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = Color.Transparent,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun SmartRescheduleBottomActions(
    uiState: SmartRescheduleUiState,
    onAction: (SmartRescheduleAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = SmartRescheduleSpacing.ExtraLarge,
                top = SmartRescheduleSpacing.Small,
                end = SmartRescheduleSpacing.ExtraLarge,
                bottom = SmartRescheduleSpacing.Large,
            ),
        verticalArrangement = Arrangement.spacedBy(SmartRescheduleSpacing.Small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = { onAction(SmartRescheduleAction.ApplySchedule) },
            enabled = uiState is SmartRescheduleUiState.Success && uiState.canApply,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = SmartRescheduleDimensions.ContentMaxWidth)
                .height(SmartRescheduleDimensions.BottomButtonHeight),
            contentPadding = ButtonDefaults.ContentPadding,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_app_check_small),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Text(
                text = if (uiState is SmartRescheduleUiState.Success && uiState.isApplying) {
                    stringResource(R.string.smart_reschedule_applying)
                } else {
                    stringResource(R.string.smart_reschedule_apply)
                },
                modifier = Modifier.padding(start = SmartRescheduleSpacing.Small),
            )
        }

        OutlinedButton(
            onClick = { onAction(SmartRescheduleAction.Cancel) },
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = SmartRescheduleDimensions.ContentMaxWidth)
                .height(SmartRescheduleDimensions.BottomButtonHeight),
            contentPadding = ButtonDefaults.ContentPadding,
            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_app_close),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Text(
                text = stringResource(R.string.common_cancel),
                modifier = Modifier.padding(start = SmartRescheduleSpacing.Small),
            )
        }
    }
}

@Composable
private fun SmartRescheduleLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        SmartLoadingCircularIndicator(isLoading = true)
    }
}

@Composable
private fun SmartRescheduleMessageContent(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(SmartRescheduleSpacing.ExtraLarge),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SmartRescheduleSpacing.Medium),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (onRetry != null) {
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.common_retry))
                }
            }
        }
    }
}

@Composable
private fun SmartRescheduleChangeType.label(): String {
    return when (this) {
        SmartRescheduleChangeType.ADDED -> stringResource(R.string.smart_reschedule_status_added)
        SmartRescheduleChangeType.MOVED -> stringResource(R.string.smart_reschedule_status_moved)
        SmartRescheduleChangeType.DEFERRED -> stringResource(R.string.smart_reschedule_status_deferred)
        SmartRescheduleChangeType.UNCHANGED -> stringResource(R.string.smart_reschedule_status_unchanged)
    }
}

@Composable
private fun SmartRescheduleChangeUiModel.containerColor(): Color {
    return when (type) {
        SmartRescheduleChangeType.ADDED -> MaterialTheme.colorScheme.tertiaryContainer
        SmartRescheduleChangeType.MOVED -> MaterialTheme.colorScheme.secondaryContainer
        SmartRescheduleChangeType.DEFERRED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
        SmartRescheduleChangeType.UNCHANGED -> MaterialTheme.colorScheme.surfaceContainer
    }
}

private fun SmartRescheduleChangeUiModel.leadingIconResId(): Int {
    return when {
        type == SmartRescheduleChangeType.ADDED && priority == Priority.HIGH -> R.drawable.ic_app_warning_triangle
        type == SmartRescheduleChangeType.ADDED -> R.drawable.ic_app_lightning
        else -> R.drawable.ic_app_lightning
    }
}

@Composable
private fun SmartRescheduleChangeUiModel.leadingIconTint(): Color {
    return if (type == SmartRescheduleChangeType.ADDED && priority == Priority.HIGH) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.tertiary
    }
}
