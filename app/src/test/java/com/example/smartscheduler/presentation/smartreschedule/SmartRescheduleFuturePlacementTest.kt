package com.example.smartscheduler.presentation.smartreschedule

import com.example.smartscheduler.data.repository.fake.FakeEventRepository
import com.example.smartscheduler.data.repository.fake.FakeTaskRepository
import com.example.smartscheduler.domain.algorithm.SmartSchedulerAlgorithmImpl
import com.example.smartscheduler.domain.model.DiffItem
import com.example.smartscheduler.domain.model.Event
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.UnscheduledTask
import com.example.smartscheduler.domain.usecase.CalculateDiffUseCase
import com.example.smartscheduler.domain.usecase.PreviewRescheduleDayUseCase
import com.example.smartscheduler.domain.usecase.ReschedulePreviewResult
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartRescheduleFuturePlacementTest {

    private val today = LocalDate.of(2026, 6, 16)

    @Test
    fun `task that does not fit today is placed in nearest future free slot`() = runBlocking {
        val task = backlogTask("deferred", Duration.ofHours(1))
        val useCase = PreviewRescheduleDayUseCase(
            taskRepository = FakeTaskRepository(),
            eventRepository = FakeEventRepository(
                initialEvents = listOf(
                    Event(
                        id = "tomorrow-standup",
                        name = "Standup",
                        description = null,
                        startTime = today.plusDays(1).atTime(9, 0),
                        endTime = today.plusDays(1).atTime(10, 0),
                    )
                )
            ),
            smartSchedulerAlgorithm = SmartSchedulerAlgorithmImpl(),
            calculateDiffUseCase = CalculateDiffUseCase(),
        )

        val result = useCase.fromSnapshot(
            date = today,
            workDayStart = LocalTime.of(9, 0),
            workDayEnd = LocalTime.of(11, 0),
            tasksOnToday = emptyList(),
            eventsOnToday = listOf(
                Event(
                    id = "today-focus",
                    name = "Focus block",
                    description = null,
                    startTime = today.atTime(9, 0),
                    endTime = today.atTime(11, 0),
                )
            ),
            backlog = listOf(task),
        ) as ReschedulePreviewResult.Success

        val deferredDiff = result.preview.diff.filterIsInstance<DiffItem.Deferred>().single()
        val scheduledTask = result.preview.proposedSchedule.filterIsInstance<ScheduledTask>().single()

        assertEquals(today.plusDays(1).atTime(10, 0), deferredDiff.newStartTime)
        assertEquals(today.plusDays(1).atTime(11, 0), deferredDiff.newEndTime)
        assertEquals(deferredDiff.newStartTime, scheduledTask.startTime)
        assertTrue(result.preview.proposedUnallocated.isEmpty())
    }

    @Test
    fun `accepted future deferred task is applied as scheduled task`() = runBlocking {
        val task = backlogTask("deferred", Duration.ofHours(1))
        val useCase = PreviewRescheduleDayUseCase(
            taskRepository = FakeTaskRepository(),
            eventRepository = FakeEventRepository(),
            smartSchedulerAlgorithm = SmartSchedulerAlgorithmImpl(),
            calculateDiffUseCase = CalculateDiffUseCase(),
        )

        val result = useCase.fromSnapshot(
            date = today,
            workDayStart = LocalTime.of(9, 0),
            workDayEnd = LocalTime.of(10, 0),
            tasksOnToday = emptyList(),
            eventsOnToday = listOf(
                Event(
                    id = "today-focus",
                    name = "Focus block",
                    description = null,
                    startTime = today.atTime(9, 0),
                    endTime = today.atTime(10, 0),
                )
            ),
            backlog = listOf(task),
        ) as ReschedulePreviewResult.Success

        val tasksToApply = buildSmartRescheduleTasksToApply(
            preview = result.preview,
            rejectedTaskIds = emptySet(),
        )

        val scheduledTask = tasksToApply.filterIsInstance<ScheduledTask>().single()
        assertEquals(today.plusDays(1).atTime(9, 0), scheduledTask.startTime)
    }

    private fun backlogTask(id: String, duration: Duration) = UnscheduledTask(
        id = id,
        name = id,
        description = null,
        status = Status.PENDING,
        priority = Priority.MEDIUM,
        isLocked = false,
        deadline = null,
        preferredPlaceTime = null,
        duration = duration,
    )
}
