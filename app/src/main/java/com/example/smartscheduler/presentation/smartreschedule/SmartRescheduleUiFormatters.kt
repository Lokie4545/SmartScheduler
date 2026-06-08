package com.example.smartscheduler.presentation.smartreschedule

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import com.example.smartscheduler.R
import com.example.smartscheduler.domain.model.Priority
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
internal fun SmartRescheduleSummaryUiModel.formatSummary(): String {
    return stringResource(
        R.string.smart_reschedule_summary,
        totalChanges,
        movedCount,
        addedCount,
        deferredCount,
    )
}

@Composable
internal fun Priority.formatLabel(): String {
    return when (this) {
        Priority.HIGH -> stringResource(R.string.priority_high)
        Priority.MEDIUM -> stringResource(R.string.priority_medium)
        Priority.LOW -> stringResource(R.string.priority_low)
    }
}

@Composable
internal fun Duration.formatSmartDuration(): String {
    val hours = toHours()
    val minutes = minusHours(hours).toMinutes()

    return when {
        hours > 0 && minutes > 0 -> stringResource(R.string.common_hour_minute, hours, minutes)
        hours > 0 -> stringResource(R.string.common_hour, hours)
        else -> stringResource(R.string.common_minute, minutes.coerceAtLeast(0))
    }
}

private fun LocalDateTime.formatSmartTime(pattern: String): String {
    return format(DateTimeFormatter.ofPattern(pattern))
}

@Composable
internal fun LocalDate.formatDiffSectionTitle(today: LocalDate): String {
    if (this == today) return stringResource(R.string.common_today)
    return format(
        DateTimeFormatter.ofPattern(
            stringResource(R.string.date_format_diff_section),
            LocalLocale.current.platformLocale,
        )
    )
}

@Composable
internal fun SmartRescheduleChangeUiModel.formatMeta(): String {
    val timePattern = stringResource(R.string.time_format_24h)
    val priorityLabel = priority?.formatLabel()
    val timeLabel = when (type) {
        SmartRescheduleChangeType.ADDED -> newStartTime?.let { start ->
            val end = newEndTime?.formatSmartTime(timePattern)
            if (end != null) {
                stringResource(R.string.common_time_range, start.formatSmartTime(timePattern), end)
            } else {
                start.formatSmartTime(timePattern)
            }
        }

        SmartRescheduleChangeType.MOVED -> {
            val oldLabel = oldStartTime?.formatSmartTime(timePattern)
            val newLabel = newStartTime?.formatSmartTime(timePattern)
            when {
                oldLabel != null && newLabel != null -> stringResource(
                    R.string.common_time_transition,
                    oldLabel,
                    newLabel,
                )
                newLabel != null -> newLabel
                else -> null
            }
        }

        SmartRescheduleChangeType.DEFERRED -> (newStartTime ?: oldStartTime)?.let { start ->
            val end = (newEndTime ?: oldEndTime)?.formatSmartTime(timePattern)
            if (end != null) {
                stringResource(R.string.common_time_range, start.formatSmartTime(timePattern), end)
            } else {
                start.formatSmartTime(timePattern)
            }
        }

        SmartRescheduleChangeType.UNCHANGED -> oldStartTime?.let { start ->
            val end = oldEndTime?.formatSmartTime(timePattern)
            if (end != null) {
                stringResource(R.string.common_time_range, start.formatSmartTime(timePattern), end)
            } else {
                start.formatSmartTime(timePattern)
            }
        }
    }
    val durationLabel = duration?.formatSmartDuration()

    return listOfNotNull(priorityLabel, timeLabel, durationLabel)
        .joinToString(stringResource(R.string.common_meta_separator))
}

@Composable
internal fun SmartRescheduleChangeUiModel.formatPreviewSubtitle(today: LocalDate): String {
    val deadlineDate = deadline?.toLocalDate()
    return when {
        deadlineDate != null && deadlineDate.isBefore(today) -> stringResource(
            R.string.smart_reschedule_expired_date,
            deadlineDate.formatRelativeDate(today),
        )
        deadlineDate == today -> stringResource(R.string.smart_reschedule_deadline_today)
        else -> reason
    }
}

@Composable
internal fun SmartRescheduleBacklogCandidateUiModel.formatPreviewSubtitle(today: LocalDate): String {
    val deadlineDate = deadline?.toLocalDate()
    return when {
        deadlineDate != null && deadlineDate.isBefore(today) -> stringResource(
            R.string.smart_reschedule_expired_date,
            deadlineDate.formatRelativeDate(today),
        )
        deadlineDate == today -> stringResource(R.string.smart_reschedule_deadline_today)
        deadlineDate != null -> stringResource(
            R.string.smart_reschedule_deadline_date,
            deadlineDate.formatRelativeDate(today),
        )
        else -> priority.formatLabel()
    }
}

@Composable
private fun LocalDate.formatRelativeDate(today: LocalDate): String {
    return when (this) {
        today.minusDays(1) -> stringResource(R.string.common_yesterday)
        today -> stringResource(R.string.common_today)
        today.plusDays(1) -> stringResource(R.string.common_tomorrow)
        else -> format(
            DateTimeFormatter.ofPattern(
                stringResource(R.string.date_format_short),
                LocalLocale.current.platformLocale,
            )
        )
    }
}
