package com.example.smartscheduler.presentation.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartscheduler.R
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.presentation.calendar.CalendarAgendaItemUiModel
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CalendarAgendaList(
    items: List<CalendarAgendaItemUiModel>,
    onTaskCheckedChange: (String) -> Unit,
    onItemClick: (CalendarAgendaItemUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        calendarAgendaItems(
            items = items,
            onTaskCheckedChange = onTaskCheckedChange,
            onItemClick = onItemClick,
        )
    }
}

fun LazyListScope.calendarAgendaItems(
    items: List<CalendarAgendaItemUiModel>,
    onTaskCheckedChange: (String) -> Unit,
    onItemClick: (CalendarAgendaItemUiModel) -> Unit,
) {
    if (items.isEmpty()) {
        item(key = "agenda_empty") {
            Text(
                text = "No tasks or events for this day",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    itemsIndexed(
        items = items,
        key = { _, item -> "agenda_${item.id}" },
    ) { index, item ->
        Column(modifier = Modifier.fillMaxWidth()) {
            if (index > 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            CalendarAgendaRow(
                item = item,
                onTaskCheckedChange = onTaskCheckedChange,
                onItemClick = onItemClick,
            )
        }
    }
}

@Composable
private fun CalendarAgendaRow(
    item: CalendarAgendaItemUiModel,
    onTaskCheckedChange: (String) -> Unit,
    onItemClick: (CalendarAgendaItemUiModel) -> Unit,
) {
    when (item) {
        is CalendarAgendaItemUiModel.TaskItem -> CalendarAgendaTaskRow(
            item = item,
            onCheckedChange = { onTaskCheckedChange(item.id) },
            onClick = { onItemClick(item) },
        )

        is CalendarAgendaItemUiModel.EventItem -> CalendarAgendaEventRow(
            item = item,
            onClick = { onItemClick(item) },
        )
    }
}

@Composable
private fun CalendarAgendaTaskRow(
    item: CalendarAgendaItemUiModel.TaskItem,
    onCheckedChange: () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Checkbox(
            checked = item.status == Status.COMPLETED,
            onCheckedChange = { onCheckedChange() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )
        AgendaTitleAndTime(
            title = item.title,
            meta = formatAgendaTime(item.startTime, item.endTime, item.duration),
            completed = item.status == Status.COMPLETED,
            modifier = Modifier.weight(1f),
        )
        CalendarPriorityIcon(priority = item.priority)
    }
}

@Composable
private fun CalendarAgendaEventRow(
    item: CalendarAgendaItemUiModel.EventItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary),
        )
        AgendaTitleAndTime(
            title = item.title,
            meta = formatAgendaTime(item.startTime, item.endTime, item.duration),
            completed = false,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(R.drawable.ic_app_smile),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AgendaTitleAndTime(
    title: String,
    meta: String,
    completed: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (completed) TextDecoration.LineThrough else null,
        )
        Text(
            text = meta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (completed) TextDecoration.LineThrough else null,
        )
    }
}

@Composable
private fun CalendarPriorityIcon(priority: Priority) {
    val visuals = when (priority) {
        Priority.HIGH -> PriorityIconVisuals(
            iconResId = R.drawable.ic_app_task_chip_priority_warning,
            tint = MaterialTheme.colorScheme.error,
        )
        Priority.MEDIUM -> PriorityIconVisuals(
            iconResId = R.drawable.ic_app_task_chip_priority_medium,
            tint = MaterialTheme.colorScheme.tertiary,
        )
        Priority.LOW -> PriorityIconVisuals(
            iconResId = R.drawable.ic_app_task_chip_priority_low,
            tint = MaterialTheme.colorScheme.secondary,
        )
    }

    Icon(
        painter = painterResource(visuals.iconResId),
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = visuals.tint,
    )
}

private data class PriorityIconVisuals(
    val iconResId: Int,
    val tint: Color,
)

private fun formatAgendaTime(
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    duration: Duration,
): String {
    val formatter = DateTimeFormatter.ofPattern("H:mm")
    return "${startTime.format(formatter)} - ${endTime.format(formatter)} • ${formatDuration(duration)}"
}

private fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()

    return when {
        hours > 0 && minutes > 0 -> "${hours} h ${minutes} min"
        hours > 0 -> "$hours h"
        else -> "${minutes.coerceAtLeast(0)} min"
    }
}

@Preview(apiLevel = 35, showBackground = true)
@Composable
private fun CalendarAgendaListPreview() {
    val date = LocalDate.of(2026, 4, 17)
    SmartSchedulerTheme(dynamicColor = false) {
        CalendarAgendaList(
            items = listOf(
                CalendarAgendaItemUiModel.TaskItem(
                    id = "task-1",
                    title = "Card design",
                    description = null,
                    startTime = date.atTime(10, 0),
                    endTime = date.atTime(12, 0),
                    duration = Duration.ofHours(2),
                    status = Status.SCHEDULED,
                    priority = Priority.HIGH,
                ),
                CalendarAgendaItemUiModel.EventItem(
                    id = "event-1",
                    title = "Some Event",
                    description = null,
                    startTime = date.atTime(10, 0),
                    endTime = date.atTime(12, 0),
                    duration = Duration.ofHours(2),
                ),
            ),
            onTaskCheckedChange = {},
            onItemClick = {},
        )
    }
}
