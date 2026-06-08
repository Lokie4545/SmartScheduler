package com.example.smartscheduler.data.mapper

import com.example.smartscheduler.data.local.TaskEntity
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.UnscheduledTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class TaskMapperTest {

    @Test
    fun `maps unscheduled task duration from entity`() {
        val entity = TaskEntity(
            id = "backlog-1",
            name = "Backlog task",
            description = null,
            status = Status.PENDING.name,
            duration = Duration.ofMinutes(45).toMillis(),
            priority = Priority.MEDIUM.name,
            deadline = null,
            preferredPlaceTime = null,
            isLocked = false,
            startTime = null,
            endTime = null,
        )

        val task = entity.toDomain()

        assertTrue(task is UnscheduledTask)
        assertEquals(Duration.ofMinutes(45), (task as UnscheduledTask).duration)
    }

    @Test
    fun `preserves nullable deadline for unscheduled task entity`() {
        val task = UnscheduledTask(
            id = "backlog-2",
            name = "No deadline task",
            description = null,
            status = Status.PENDING,
            priority = Priority.LOW,
            isLocked = false,
            deadline = null,
            preferredPlaceTime = null,
            duration = Duration.ofMinutes(30),
        )

        val entity = task.toEntity()

        assertNull(entity.deadline)
        assertEquals(Duration.ofMinutes(30).toMillis(), entity.duration)
        assertNull(entity.startTime)
        assertNull(entity.endTime)
    }
}
