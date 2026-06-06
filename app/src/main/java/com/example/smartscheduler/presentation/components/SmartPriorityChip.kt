package com.example.smartscheduler.presentation.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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
fun SmartPriorityChip(priority: Priority, modifier: Modifier = Modifier) {
    val (text, containerColor, contentColor, iconId) = when (priority) {
        Priority.HIGH -> PriorityVisuals(
            text = "Hight",
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            iconId = R.drawable.ic_app_task_chip_priority_warning
        )

        Priority.MEDIUM -> PriorityVisuals(
            text = "Med",
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

    AssistChip(
        modifier = modifier,
        onClick = {},
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = contentColor,
            leadingIconContentColor = contentColor
        ),
        border = null,
        label = {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        },
        leadingIcon = { Icon(painterResource(iconId), contentDescription = null) }
    )
}

@Preview
@Composable
fun SmartPriorityChipPreview() {
    SmartSchedulerTheme() {
        SmartPriorityChip(Priority.HIGH)
    }
}