package com.example.smartscheduler.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartscheduler.R
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme

private data class PriorityVisuals(
    val text: String,
    val containerColor: Color,
    val contentColor: Color,
    val iconId: Int
)


@Composable
fun SmartPriorityChip(
    priority: Priority,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val (text, containerColor, contentColor, iconId) = when (priority) {
        Priority.HIGH -> PriorityVisuals(
            text = "High",
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            iconId = R.drawable.ic_app_task_chip_priority_warning
        )

        Priority.MEDIUM -> PriorityVisuals(
            text = "Medium",
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            iconId = R.drawable.ic_app_task_chip_priority_medium
        )

        Priority.LOW -> PriorityVisuals(
            text = "Low",
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            iconId = R.drawable.ic_app_task_chip_priority_low
        )
    }

    SuggestionChip(
        modifier = modifier,
        onClick = onClick ?: {},
        enabled = onClick != null,
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = containerColor,
            labelColor = contentColor,
            iconContentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledLabelColor = contentColor,
            disabledIconContentColor = contentColor
        ),
        border = null,
        label = {
            Text(text = text)
        },
        icon = { Icon(painterResource(iconId), contentDescription = null) }
    )
}

@Preview(apiLevel = 35, showBackground = true)
@Composable
fun SmartPriorityChipPreview() {
    SmartSchedulerTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmartPriorityChip(Priority.HIGH)
                SmartPriorityChip(Priority.MEDIUM)
                SmartPriorityChip(Priority.LOW)
            }
        }
    }
}
