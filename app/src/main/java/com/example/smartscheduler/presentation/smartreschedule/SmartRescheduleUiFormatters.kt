package com.example.smartscheduler.presentation.smartreschedule

import com.example.smartscheduler.domain.model.Priority
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun SmartRescheduleSummaryUiModel.formatSummary(): String {
    return "You have $totalChanges changes: $movedCount moved · $addedCount added · $deferredCount deferred"
}

internal fun Priority.formatLabel(): String {
    return name.lowercase().replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
    }
}

internal fun Duration.formatSmartDuration(): String {
    val hours = toHours()
    val minutes = minusHours(hours).toMinutes()

    return when {
        hours > 0 && minutes > 0 -> "${hours} h ${minutes} min"
        hours > 0 -> "${hours} h"
        else -> "${minutes.coerceAtLeast(0)} min"
    }
}

internal fun LocalDateTime.formatSmartTime(): String {
    return format(DateTimeFormatter.ofPattern("H:mm"))
}

internal fun LocalDate.formatDiffSectionTitle(today: LocalDate): String {
    if (this == today) return "Today"
    return format(DateTimeFormatter.ofPattern("EEE d", Locale.getDefault()))
}

internal fun SmartRescheduleChangeUiModel.formatMeta(): String {
    val priorityLabel = priority?.formatLabel()
    val timeLabel = when (type) {
        SmartRescheduleChangeType.ADDED -> newStartTime?.let { start ->
            val end = newEndTime?.formatSmartTime()
            if (end != null) "${start.formatSmartTime()} - $end" else start.formatSmartTime()
        }

        SmartRescheduleChangeType.MOVED -> {
            val oldLabel = oldStartTime?.formatSmartTime()
            val newLabel = newStartTime?.formatSmartTime()
            when {
                oldLabel != null && newLabel != null -> "$oldLabel → $newLabel"
                newLabel != null -> newLabel
                else -> null
            }
        }

        SmartRescheduleChangeType.DEFERRED,
        SmartRescheduleChangeType.UNCHANGED -> oldStartTime?.let { start ->
            val end = oldEndTime?.formatSmartTime()
            if (end != null) "${start.formatSmartTime()} - $end" else start.formatSmartTime()
        }
    }
    val durationLabel = duration?.formatSmartDuration()

    return listOfNotNull(priorityLabel, timeLabel, durationLabel).joinToString(" · ")
}

internal fun SmartRescheduleChangeUiModel.formatPreviewSubtitle(today: LocalDate): String {
    val deadlineDate = deadline?.toLocalDate()
    return when {
        deadlineDate != null && deadlineDate.isBefore(today) -> "Expired: ${deadlineDate.formatRelativeDate(today)}"
        deadlineDate == today -> "Deadline today"
        else -> reason
    }
}

internal fun SmartRescheduleBacklogCandidateUiModel.formatPreviewSubtitle(today: LocalDate): String {
    val deadlineDate = deadline?.toLocalDate()
    return when {
        deadlineDate != null && deadlineDate.isBefore(today) -> "Expired: ${deadlineDate.formatRelativeDate(today)}"
        deadlineDate == today -> "Deadline today"
        deadlineDate != null -> "Deadline ${deadlineDate.formatRelativeDate(today)}"
        else -> priority.formatLabel()
    }
}

private fun LocalDate.formatRelativeDate(today: LocalDate): String {
    return when (this) {
        today.minusDays(1) -> "Yesterday"
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        else -> format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
    }
}
