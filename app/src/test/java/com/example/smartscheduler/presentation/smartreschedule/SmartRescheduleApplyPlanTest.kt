package com.example.smartscheduler.presentation.smartreschedule

import com.example.smartscheduler.domain.model.DiffItem
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.ReschedulePreview
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.TimeSlot
import com.example.smartscheduler.domain.model.UnscheduledTask
import com.example.smartscheduler.domain.model.schedule
import com.example.smartscheduler.domain.model.unSchedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class SmartRescheduleApplyPlanTest {

    private val date = LocalDate.of(2026, 6, 16)

    @Test
    fun `rejected moved task restores old scheduled slot`() {
        val oldTask = scheduledTask("moved", startHour = 9, endHour = 10)
        val proposedTask = oldTask.copy(
            startTime = date.atTime(11, 0),
            endTime = date.atTime(12, 0),
        )
        val preview = ReschedulePreview(
            date = date,
            oldPlan = listOf(oldTask),
            backlog = emptyList(),
            proposedSchedule = listOf(proposedTask),
            proposedUnallocated = emptyList(),
            diff = listOf(
                DiffItem.Moved(
                    taskId = oldTask.id,
                    taskName = oldTask.name,
                    oldStartTime = oldTask.startTime,
                    newStartTime = proposedTask.startTime,
                )
            ),
        )

        val tasksToApply = buildSmartRescheduleTasksToApply(
            preview = preview,
            rejectedTaskIds = setOf(oldTask.id),
        )

        val restoredTask = tasksToApply.filterIsInstance<ScheduledTask>().single()
        assertEquals(oldTask.startTime, restoredTask.startTime)
        assertEquals(oldTask.endTime, restoredTask.endTime)
    }

    @Test
    fun `rejected added task returns to backlog`() {
        val backlogTask = backlogTask("added")
        val proposedTask = backlogTask.schedule(
            SimpleSlot(
                startTime = date.atTime(10, 0),
                endTime = date.atTime(11, 0),
            )
        )
        val preview = ReschedulePreview(
            date = date,
            oldPlan = emptyList(),
            backlog = listOf(backlogTask),
            proposedSchedule = listOf(proposedTask),
            proposedUnallocated = emptyList(),
            diff = listOf(
                DiffItem.Added(
                    taskName = backlogTask.name,
                    taskId = backlogTask.id,
                    updatedStartTime = proposedTask.startTime,
                )
            ),
        )

        val tasksToApply = buildSmartRescheduleTasksToApply(
            preview = preview,
            rejectedTaskIds = setOf(backlogTask.id),
        )

        assertTrue(tasksToApply.single() is UnscheduledTask)
    }

    @Test
    fun `rejected evicted task restores old scheduled slot`() {
        val oldTask = scheduledTask("evicted", startHour = 13, endHour = 14)
        val preview = ReschedulePreview(
            date = date,
            oldPlan = listOf(oldTask),
            backlog = emptyList(),
            proposedSchedule = emptyList(),
            proposedUnallocated = listOf(oldTask.unSchedule()),
            diff = listOf(
                DiffItem.Evicted(
                    taskId = oldTask.id,
                    taskName = oldTask.name,
                )
            ),
        )

        val tasksToApply = buildSmartRescheduleTasksToApply(
            preview = preview,
            rejectedTaskIds = setOf(oldTask.id),
        )

        val restoredTask = tasksToApply.filterIsInstance<ScheduledTask>().single()
        assertEquals(oldTask.startTime, restoredTask.startTime)
        assertEquals(oldTask.endTime, restoredTask.endTime)
    }

    private data class SimpleSlot(
        override val startTime: LocalDateTime,
        override val endTime: LocalDateTime,
    ) : TimeSlot

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
