package com.example.smartscheduler.presentation.today.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.smartscheduler.R
import com.example.smartscheduler.domain.model.TimeSlot

@Composable
fun FastAddEventBottomSheet(
    modifier: Modifier = Modifier,
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
        FastAddEventChipsRow(defaultTimeSlot = currentTimeSlot) {
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
    defaultTimeSlot: TimeSlot,
    onChipClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .wrapContentSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
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
                Text("Today", style = MaterialTheme.typography.labelLarge)
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
                    "${defaultTimeSlot.startTime} - ${defaultTimeSlot.endTime}",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        )

    }
}