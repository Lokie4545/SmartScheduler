package com.example.smartscheduler.presentation.smartreschedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smartscheduler.R
import com.example.smartscheduler.presentation.components.SmartLoadingCircularIndicator
import java.time.LocalDate

private object SmartRescheduleSheetSpacing {
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val ExtraLarge = 24.dp
}

private object SmartRescheduleSheetDimensions {
    val ContentHeightFraction = 0.78f
    val SectionIcon = 28.dp
    val TrailingIcon = 22.dp
    val PrimaryButtonHeight = 40.dp
}

@Composable
fun SmartReschedulePreviewBottomSheetRoute(
    viewModel: SmartRescheduleViewModel,
    onDismissRequest: () -> Unit,
    onViewChanges: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SmartReschedulePreviewBottomSheet(
        uiState = uiState,
        onAction = viewModel::handleAction,
        onDismissRequest = {
            viewModel.handleAction(SmartRescheduleAction.ResetSession)
            onDismissRequest()
        },
        onViewChanges = onViewChanges,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartReschedulePreviewBottomSheet(
    uiState: SmartRescheduleUiState,
    onAction: (SmartRescheduleAction) -> Unit,
    onDismissRequest: () -> Unit,
    onViewChanges: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Expanded,
        confirmValueChange = { sheetValue -> sheetValue != SheetValue.PartiallyExpanded },
    )

    ModalBottomSheet(
        modifier = modifier.fillMaxWidth(),
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
    ) {
        SmartReschedulePreviewContent(
            uiState = uiState,
            onAction = onAction,
            onViewChanges = onViewChanges,
            modifier = Modifier.fillMaxHeight(SmartRescheduleSheetDimensions.ContentHeightFraction),
        )
    }
}

@Composable
private fun SmartReschedulePreviewContent(
    uiState: SmartRescheduleUiState,
    onAction: (SmartRescheduleAction) -> Unit,
    onViewChanges: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SmartRescheduleSheetSpacing.ExtraLarge)
            .navigationBarsPadding()
            .padding(bottom = SmartRescheduleSheetSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(SmartRescheduleSheetSpacing.Large),
    ) {
        when (uiState) {
            is SmartRescheduleUiState.Loading -> SmartReschedulePreviewMessage(
                text = "Calculating your day...",
                modifier = Modifier.weight(1f),
            )

            is SmartRescheduleUiState.Error -> SmartReschedulePreviewMessage(
                text = uiState.message,
                modifier = Modifier.weight(1f),
                onRetry = { onAction(SmartRescheduleAction.Retry) },
            )

            is SmartRescheduleUiState.Empty -> SmartReschedulePreviewMessage(
                text = "No changes proposed",
                modifier = Modifier.weight(1f),
            )

            is SmartRescheduleUiState.Success -> SmartReschedulePreviewSuccess(
                uiState = uiState,
                onAction = onAction,
                modifier = Modifier.weight(1f),
            )
        }

        Button(
            onClick = onViewChanges,
            enabled = uiState is SmartRescheduleUiState.Success && uiState.canViewChanges,
            modifier = Modifier
                .fillMaxWidth()
                .height(SmartRescheduleSheetDimensions.PrimaryButtonHeight),
            contentPadding = ButtonDefaults.ContentPadding,
        ) {
            Text(
                text = "View changes",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun SmartReschedulePreviewSuccess(
    uiState: SmartRescheduleUiState.Success,
    onAction: (SmartRescheduleAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val candidates = uiState.backlogCandidates
    val expiredCandidates = remember(candidates, uiState.currentDate) {
        candidates.filter { candidate ->
            candidate.deadline?.toLocalDate()?.isBefore(uiState.currentDate) == true
        }
    }
    val waitingCandidates = remember(candidates, expiredCandidates) {
        candidates - expiredCandidates.toSet()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Your tasks on today",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "${uiState.selectedBacklogCount} tasks ~ ${uiState.selectedBacklogDuration.formatSmartDuration()}",
            modifier = Modifier.padding(top = SmartRescheduleSheetSpacing.Small),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SmartRescheduleSheetSpacing.Large),
        ) {
            if (expiredCandidates.isNotEmpty()) {
                item(key = "expired_header") {
                    SmartReschedulePreviewSectionHeader(
                        title = "Expired",
                        iconResId = R.drawable.ic_app_circle_minus,
                    )
                }
                items(
                    items = expiredCandidates,
                    key = { candidate -> "expired_${candidate.taskId}" },
                ) { candidate ->
                    SmartReschedulePreviewTaskRow(
                        candidate = candidate,
                        currentDate = uiState.currentDate,
                        isExpired = true,
                        onSelectionChange = { selected ->
                            onAction(
                                SmartRescheduleAction.ToggleBacklogCandidate(
                                    taskId = candidate.taskId,
                                    selected = selected,
                                )
                            )
                        },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }

            if (waitingCandidates.isNotEmpty()) {
                item(key = "waiting_header") {
                    SmartReschedulePreviewSectionHeader(
                        title = "Waiting",
                        iconResId = R.drawable.ic_app_smile,
                    )
                }
                items(
                    items = waitingCandidates,
                    key = { candidate -> "waiting_${candidate.taskId}" },
                ) { candidate ->
                    SmartReschedulePreviewTaskRow(
                        candidate = candidate,
                        currentDate = uiState.currentDate,
                        isExpired = false,
                        onSelectionChange = { selected ->
                            onAction(
                                SmartRescheduleAction.ToggleBacklogCandidate(
                                    taskId = candidate.taskId,
                                    selected = selected,
                                )
                            )
                        },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun SmartReschedulePreviewMessage(
    text: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SmartRescheduleSheetSpacing.Medium),
        ) {
            if (text.startsWith("Calculating")) {
                SmartLoadingCircularIndicator(isLoading = true)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (onRetry != null) {
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun SmartReschedulePreviewSectionHeader(
    title: String,
    iconResId: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SmartRescheduleSheetSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SmartRescheduleSheetSpacing.Medium),
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            modifier = Modifier.size(SmartRescheduleSheetDimensions.SectionIcon),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SmartReschedulePreviewTaskRow(
    candidate: SmartRescheduleBacklogCandidateUiModel,
    currentDate: LocalDate,
    isExpired: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .toggleable(
                value = candidate.isSelected,
                role = Role.Checkbox,
                onValueChange = onSelectionChange,
            ),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Checkbox(
                checked = candidate.isSelected,
                onCheckedChange = null,
            )
        },
        headlineContent = {
            Text(
                text = candidate.taskName,
                style = MaterialTheme.typography.titleMedium,
                color = if (candidate.isSelected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (candidate.isSelected) null else TextDecoration.LineThrough,
            )
        },
        supportingContent = {
            Text(
                text = candidate.formatPreviewSubtitle(currentDate),
                style = MaterialTheme.typography.bodySmall,
                color = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SmartRescheduleSheetSpacing.Small),
            ) {
                candidate.duration?.let { duration ->
                    Text(
                        text = duration.formatSmartDuration(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    painter = painterResource(
                        if (isExpired) R.drawable.ic_app_warning_triangle else R.drawable.ic_app_lightning
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(SmartRescheduleSheetDimensions.TrailingIcon),
                    tint = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
