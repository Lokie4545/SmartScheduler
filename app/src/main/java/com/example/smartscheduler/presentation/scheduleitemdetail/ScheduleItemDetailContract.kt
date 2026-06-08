package com.example.smartscheduler.presentation.scheduleitemdetail

import com.example.smartscheduler.domain.model.Priority
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ScheduleItemDetailArgs(
    val mode: ScheduleItemDetailMode,
    val kind: ScheduleItemKind,
    val itemId: String? = null,
    val draftTitle: String = "",
    val draftDescription: String = "",
    val draftStartTime: LocalDateTime? = null,
    val draftEndTime: LocalDateTime? = null,
)

sealed interface ScheduleItemDetailAction {
    data class TitleChanged(val title: String) : ScheduleItemDetailAction
    data class DescriptionChanged(val description: String) : ScheduleItemDetailAction
    data class KindChanged(val kind: ScheduleItemKind) : ScheduleItemDetailAction
    data class DateChanged(val date: LocalDate) : ScheduleItemDetailAction
    data class DeadlineChanged(val date: LocalDate?) : ScheduleItemDetailAction
    data class TaskDurationChanged(val duration: Duration) : ScheduleItemDetailAction
    data class TaskStartTimeChanged(val time: LocalTime) : ScheduleItemDetailAction
    data class EventStartTimeChanged(val time: LocalTime) : ScheduleItemDetailAction
    data class EventEndTimeChanged(val time: LocalTime) : ScheduleItemDetailAction
    data class LockChanged(val locked: Boolean) : ScheduleItemDetailAction
    data class PriorityChanged(val priority: Priority) : ScheduleItemDetailAction
    data object Save : ScheduleItemDetailAction
    data object Delete : ScheduleItemDetailAction
    data object MarkCompleted : ScheduleItemDetailAction
    data object Close : ScheduleItemDetailAction
    data object Retry : ScheduleItemDetailAction
}

sealed interface ScheduleItemDetailEffect {
    data object NavigateBack : ScheduleItemDetailEffect
}

enum class ScheduleItemTimeTarget {
    EVENT_START,
    EVENT_END,
}
