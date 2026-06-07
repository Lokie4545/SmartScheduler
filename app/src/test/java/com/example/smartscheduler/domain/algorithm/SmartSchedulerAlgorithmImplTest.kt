package com.example.smartscheduler.domain.algorithm

import com.example.smartscheduler.domain.model.Event
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.UnscheduledTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate

class SmartSchedulerAlgorithmImplTest {

    private val date = LocalDate.of(2026, 6, 16)
    private val algorithm = SmartSchedulerAlgorithmImpl()

    @Test
    fun `schedules backlog after event that starts at workday start`() {
        val fixedEvent = Event(
            id = "event",
            name = "Standup",
            description = null,
            startTime = date.atTime(9, 0),
            endTime = date.atTime(10, 0),
        )
        val backlogTask = backlogTask(id = "task", duration = Duration.ofMinutes(30))

        val result = algorithm.calculateSchedule(
            currentPlan = listOf(fixedEvent),
            backLog = listOf(backlogTask),
            targetDay = date,
            workDayStart = date.atTime(9, 0).toLocalTime(),
            workDayEnd = date.atTime(12, 0).toLocalTime(),
        ) as ScheduleResult.Success

        val scheduledTask = result.newSchedule.filterIsInstance<ScheduledTask>().single()
        assertEquals(date.atTime(10, 0), scheduledTask.startTime)
        assertEquals(date.atTime(10, 30), scheduledTask.endTime)
    }

    @Test
    fun `does not evict completed scheduled task`() {
        val completedTask = scheduledTask(
            id = "completed",
            status = Status.COMPLETED,
            priority = Priority.LOW,
            startHour = 9,
            endHour = 10,
        )
        val urgentBacklogTask = backlogTask(
            id = "urgent",
            priority = Priority.HIGH,
            duration = Duration.ofMinutes(30),
        )

        val result = algorithm.calculateSchedule(
            currentPlan = listOf(completedTask),
            backLog = listOf(urgentBacklogTask),
            targetDay = date,
            workDayStart = date.atTime(9, 0).toLocalTime(),
            workDayEnd = date.atTime(10, 0).toLocalTime(),
        ) as ScheduleResult.Success

        assertTrue(result.newSchedule.contains(completedTask))
        assertEquals(listOf(urgentBacklogTask), result.unallocatedTasks)
    }

    private fun backlogTask(
        id: String,
        priority: Priority = Priority.MEDIUM,
        duration: Duration = Duration.ofHours(1),
    ) = UnscheduledTask(
        id = id,
        name = id,
        description = null,
        status = Status.PENDING,
        priority = priority,
        isLocked = false,
        deadline = null,
        preferredPlaceTime = null,
        duration = duration,
    )

    private fun scheduledTask(
        id: String,
        status: Status = Status.SCHEDULED,
        priority: Priority = Priority.MEDIUM,
        startHour: Int,
        endHour: Int,
    ) = ScheduledTask(
        id = id,
        name = id,
        description = null,
        status = status,
        priority = priority,
        isLocked = false,
        deadline = null,
        preferredPlaceTime = null,
        startTime = date.atTime(startHour, 0),
        endTime = date.atTime(endHour, 0),
    )
}
