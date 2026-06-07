package com.example.smartscheduler.domain.usecase

import com.example.smartscheduler.domain.model.DiffItem
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.UnscheduledTask
import com.example.smartscheduler.domain.model.schedule
import com.example.smartscheduler.domain.model.unSchedule
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate

class CalculateDiffUseCaseTest {

    private val date = LocalDate.of(2026, 6, 16)
    private val useCase = CalculateDiffUseCase()

    @Test
    fun `emits added moved and evicted diff items`() {
        val movedOldTask = scheduledTask("moved", startHour = 9, endHour = 10)
        val evictedOldTask = scheduledTask("evicted", startHour = 10, endHour = 11)
        val addedBacklogTask = backlogTask("added")
        val addedScheduledTask = addedBacklogTask.schedule(
            SimpleSlot(
                startTime = date.atTime(11, 0),
                endTime = date.atTime(12, 0),
            )
        )
        val movedNewTask = movedOldTask.copy(
            startTime = date.atTime(12, 0),
            endTime = date.atTime(13, 0),
        )

        val diff = useCase(
            oldPlan = listOf(movedOldTask, evictedOldTask),
            backlog = listOf(addedBacklogTask),
            proposedPlan = listOf(addedScheduledTask, movedNewTask),
            newUnallocated = listOf(evictedOldTask.unSchedule()),
        )

        assertTrue(diff.any { it is DiffItem.Added && it.taskId == "added" })
        assertTrue(diff.any { it is DiffItem.Moved && it.taskId == "moved" })
        assertTrue(diff.any { it is DiffItem.Evicted && it.taskId == "evicted" })
    }

    private data class SimpleSlot(
        override val startTime: java.time.LocalDateTime,
        override val endTime: java.time.LocalDateTime,
    ) : com.example.smartscheduler.domain.model.TimeSlot

    private fun backlogTask(id: String) = UnscheduledTask(
        id = id,
        name = id,
        description = null,
        status = Status.PENDING,
        priority = Priority.MEDIUM,
        isLocked = false,
        deadline = null,
        preferredPlaceTime = null,
        duration = Duration.ofHours(1),
    )

    private fun scheduledTask(
        id: String,
        startHour: Int,
        endHour: Int,
    ) = ScheduledTask(
        id = id,
        name = id,
        description = null,
        status = Status.SCHEDULED,
        priority = Priority.MEDIUM,
        isLocked = false,
        deadline = null,
        preferredPlaceTime = null,
        startTime = date.atTime(startHour, 0),
        endTime = date.atTime(endHour, 0),
    )
}
