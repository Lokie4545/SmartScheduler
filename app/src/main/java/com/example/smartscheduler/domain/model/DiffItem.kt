package com.example.smartscheduler.domain.model

import java.time.LocalDateTime

sealed interface DiffItem {
    val taskId: String
    val taskName: String

    data class Added(
        override val taskName: String,
        override val taskId: String,
        val updatedStartTime: LocalDateTime
    ): DiffItem

    data class Moved(
        override val taskId: String,
        override val taskName: String,
        val oldStartTime: LocalDateTime,
        val newStartTime: LocalDateTime
    ): DiffItem

    data class Evicted(
        override val taskId: String,
        override val taskName: String,
    ) : DiffItem

    data class Deferred(
        override val taskId: String,
        override val taskName: String,
        val oldStartTime: LocalDateTime?,
        val newStartTime: LocalDateTime,
        val newEndTime: LocalDateTime,
    ) : DiffItem

    data class Unchanged(
        override val taskId: String,
        override val taskName: String
    ) : DiffItem
}
