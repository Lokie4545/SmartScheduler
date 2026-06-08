package com.example.smartscheduler.presentation.smartreschedule

import com.example.smartscheduler.domain.model.Priority
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

sealed interface SmartRescheduleUiState {
    val currentDate: LocalDate

    data class Loading(override val currentDate: LocalDate) : SmartRescheduleUiState

    data class Empty(override val currentDate: LocalDate) : SmartRescheduleUiState

    data class Error(
        override val currentDate: LocalDate,
        val message: String,
    ) : SmartRescheduleUiState

    data class Success(
        override val currentDate: LocalDate,
        val backlogCandidates: List<SmartRescheduleBacklogCandidateUiModel>,
        val summary: SmartRescheduleSummaryUiModel,
        val changes: List<SmartRescheduleChangeUiModel>,
        val isApplying: Boolean = false,
    ) : SmartRescheduleUiState {
        val activeChanges: List<SmartRescheduleChangeUiModel> = changes.filterNot { it.isRejected }
        val selectedBacklogCount: Int = backlogCandidates.count { it.isSelected }
        val selectedBacklogDuration: Duration = backlogCandidates
            .filter { it.isSelected }
            .mapNotNull { it.duration }
            .fold(Duration.ZERO, Duration::plus)
        val canApply: Boolean = activeChanges.isNotEmpty() && !isApplying
        val canViewChanges: Boolean = selectedBacklogCount > 0 && activeChanges.isNotEmpty() && !isApplying
    }
}

data class SmartRescheduleBacklogCandidateUiModel(
    val taskId: String,
    val taskName: String,
    val priority: Priority,
    val duration: Duration?,
    val deadline: LocalDateTime?,
    val isSelected: Boolean,
) {
    val isExpired: Boolean
        get() = deadline?.toLocalDate()?.isBefore(LocalDate.now()) == true
}

data class SmartRescheduleSummaryUiModel(
    val movedCount: Int,
    val addedCount: Int,
    val deferredCount: Int,
) {
    val totalChanges: Int = movedCount + addedCount + deferredCount
}

enum class SmartRescheduleChangeType {
    ADDED,
    MOVED,
    DEFERRED,
    UNCHANGED,
}

data class SmartRescheduleChangeUiModel(
    val taskId: String,
    val taskName: String,
    val type: SmartRescheduleChangeType,
    val priority: Priority?,
    val duration: Duration?,
    val deadline: LocalDateTime?,
    val oldStartTime: LocalDateTime?,
    val oldEndTime: LocalDateTime?,
    val newStartTime: LocalDateTime?,
    val newEndTime: LocalDateTime?,
    val reason: String,
    val isRejected: Boolean,
) {
    val returnsToBacklog: Boolean = isRejected || (type == SmartRescheduleChangeType.DEFERRED && newStartTime == null)
}
