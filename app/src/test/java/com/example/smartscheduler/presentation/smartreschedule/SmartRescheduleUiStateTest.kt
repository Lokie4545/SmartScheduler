package com.example.smartscheduler.presentation.smartreschedule

import com.example.smartscheduler.domain.model.Priority
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDate

class SmartRescheduleUiStateTest {

    @Test
    fun `rejected changes do not keep apply enabled`() {
        val today = LocalDate.of(2026, 6, 16)
        val candidate = SmartRescheduleBacklogCandidateUiModel(
            taskId = "task-1",
            taskName = "Task 1",
            priority = Priority.MEDIUM,
            duration = Duration.ofHours(1),
            deadline = null,
            isSelected = true,
        )
        val rejectedChange = SmartRescheduleChangeUiModel(
            taskId = "task-1",
            taskName = "Task 1",
            type = SmartRescheduleChangeType.MOVED,
            priority = Priority.MEDIUM,
            duration = Duration.ofHours(1),
            deadline = null,
            oldStartTime = null,
            oldEndTime = null,
            newStartTime = null,
            newEndTime = null,
            reason = "reason",
            isRejected = true,
        )

        val state = SmartRescheduleUiState.Success(
            currentDate = today,
            backlogCandidates = listOf(candidate),
            summary = SmartRescheduleSummaryUiModel(
                movedCount = 0,
                addedCount = 0,
                deferredCount = 0,
            ),
            changes = listOf(rejectedChange),
        )

        assertFalse(state.canApply)
        assertFalse(state.canViewChanges)
        assertEquals(0, state.summary.totalChanges)
    }
}
