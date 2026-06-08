package com.example.smartscheduler.presentation.today.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartscheduler.R
import com.example.smartscheduler.domain.model.TimeSlot
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun FastAddEventBottomSheet(
    modifier: Modifier = Modifier,
    dateLabel: String,
    onDismissRequest: () -> Unit,
    currentTimeSlot: TimeSlot,
    onSaveDefaultEvent: (title: String, description: String) -> Unit,
    onNavigateToFullscreenTask: (draftTitle: String, draftDescription: String) -> Unit
) {
    val titleState = rememberTextFieldState()
    val descriptionState = rememberTextFieldState()
    FastAddBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        onSaveDefault = onSaveDefaultEvent,
        titleState = titleState,
        descriptionState = descriptionState
    ) {
        FastAddEventChipsRow(
            modifier = Modifier.weight(1f),
            dateLabel = dateLabel,
            defaultTimeSlot = currentTimeSlot,
        ) {
            onNavigateToFullscreenTask(
                titleState.text.toString(),
                descriptionState.text.toString()
            )
        }
    }
}


@Composable
private fun FastAddEventChipsRow(
    modifier: Modifier = Modifier,
    dateLabel: String,
    defaultTimeSlot: TimeSlot,
    onChipClick: () -> Unit,
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("H:mm") }

    Row(
        modifier = modifier,
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
                Text(
                    text = "${defaultTimeSlot.startTime.format(timeFormatter)} - " +
                        defaultTimeSlot.endTime.format(timeFormatter),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        )

    }
}

private data class PreviewTimeSlot(
    override val startTime: LocalDateTime,
    override val endTime: LocalDateTime
) : TimeSlot

@Preview(name = "Fast add mini event", widthDp = 393, apiLevel = 35, showBackground = true)
@Composable
private fun FastAddMiniEventContentPreview() {
    val date = LocalDate.of(2026, 9, 1)

    SmartSchedulerTheme(dynamicColor = false) {
        FastAddBottomSheetContent(
            titleState = rememberTextFieldState(),
            descriptionState = rememberTextFieldState(),
            onSaveDefault = { _, _ -> },
            chipsContent = {
                FastAddEventChipsRow(
                    modifier = Modifier.weight(1f),
                    dateLabel = "Today",
                    defaultTimeSlot = PreviewTimeSlot(
                        startTime = date.atTime(14, 0),
                        endTime = date.atTime(14, 30)
                    ),
                    onChipClick = {}
                )
            }
        )
    }
}
