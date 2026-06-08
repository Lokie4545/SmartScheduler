package com.example.smartscheduler.presentation.today.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartscheduler.R
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme

@Composable
fun FastAddTaskBottomSheet(
    modifier: Modifier = Modifier,
    dateLabel: String,
    durationLabel: String,
    onDismissRequest: () -> Unit,
    onSaveDefaultTask: (title: String, description: String) -> Unit,
    onNavigateToFullscreenTask: (draftTitle: String, draftDescription: String) -> Unit
) {
    val titleState = rememberTextFieldState()
    val descriptionState = rememberTextFieldState()
    FastAddBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        onSaveDefault = onSaveDefaultTask,
        titleState = titleState,
        descriptionState = descriptionState
    ) {
        FastAddTaskChipsRow(
            dateLabel = dateLabel,
            durationLabel = durationLabel,
            modifier = Modifier.weight(1f),
        ) {
            onNavigateToFullscreenTask(
                titleState.text.toString(),
                descriptionState.text.toString()
            )
        }
    }
}


@Composable
private fun FastAddTaskChipsRow(
    dateLabel: String,
    durationLabel: String,
    modifier: Modifier = Modifier,
    onChipClick: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SuggestionChip(
            onClick = onChipClick,
            icon = {
                Icon(
                    painterResource(R.drawable.ic_app_bottom_nav_calendar),
                    contentDescription = null
                )
            },
            label = {
                Text(dateLabel, style = MaterialTheme.typography.labelLarge)
            }
        )


        SuggestionChip(
            onClick = onChipClick,
            icon = {
                Icon(
                    painterResource(R.drawable.ic_app_chip_clock),
                    contentDescription = null
                )
            },
            label = {
                Text(durationLabel, style = MaterialTheme.typography.labelLarge)
            }
        )


        SuggestionChip(
            onClick = onChipClick,
            icon = {
                Icon(
                    painterResource(R.drawable.ic_app_general_priority),
                    contentDescription = null
                )
            },
            label = {
                Text("Medium", style = MaterialTheme.typography.labelLarge)
            }
        )
    }
}

@Preview(name = "Fast add mini task", widthDp = 393, apiLevel = 35, showBackground = true)
@Composable
private fun FastAddMiniTaskContentPreview() {
    SmartSchedulerTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            FastAddBottomSheetContent(
                titleState = rememberTextFieldState(),
                descriptionState = rememberTextFieldState(),
                onSaveDefault = { _, _ -> },
                chipsContent = {
                    FastAddTaskChipsRow(
                        dateLabel = "Today",
                        durationLabel = "30 min",
                        modifier = Modifier.weight(1f),
                        onChipClick = {},
                    )
                }
            )
        }
    }
}
