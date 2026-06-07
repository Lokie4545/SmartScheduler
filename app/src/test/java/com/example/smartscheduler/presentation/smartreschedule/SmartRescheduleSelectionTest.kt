package com.example.smartscheduler.presentation.smartreschedule

import com.example.smartscheduler.data.repository.fake.FakeEventRepository
import com.example.smartscheduler.data.repository.fake.FakeTaskRepository
import com.example.smartscheduler.domain.algorithm.SmartSchedulerAlgorithmImpl
import com.example.smartscheduler.domain.model.DiffItem
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.UnscheduledTask
import com.example.smartscheduler.domain.usecase.CalculateDiffUseCase
import com.example.smartscheduler.domain.usecase.PreviewRescheduleDayUseCase
import com.example.smartscheduler.domain.usecase.ReschedulePreviewResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class SmartRescheduleSelectionTest {

    @Test
    fun `unchecked backlog task is excluded from preview diff`() = runBlocking {
        val selectedTask = backlogTask("selected")
        val uncheckedTask = backlogTask("unchecked")
        val selectedBacklog = listOf(selectedTask, uncheckedTask)
            .filterSelectedForSmartReschedule(deselectedTaskIds = setOf(uncheckedTask.id))
        val useCase = PreviewRescheduleDayUseCase(
            taskRepository = FakeTaskRepository(),
            eventRepository = FakeEventRepository(),
            smartSchedulerAlgorithm = SmartSchedulerAlgorithmImpl(),
            calculateDiffUseCase = CalculateDiffUseCase(),
        )

        val result = useCase.fromSnapshot(
            date = LocalDate.of(2026, 6, 16),
            workDayStart = LocalTime.of(9, 0),
            workDayEnd = LocalTime.of(12, 0),
            tasksOnToday = emptyList(),
            eventsOnToday = emptyList(),
            backlog = selectedBacklog,
        ) as ReschedulePreviewResult.Success

        assertTrue(result.preview.diff.any { it is DiffItem.Added && it.taskId == selectedTask.id })
        assertFalse(result.preview.diff.any { it.taskId == uncheckedTask.id })
    }

    private fun backlogTask(id: String) = UnscheduledTask(
        id = id,
        name = id,
        description = null,
        status = Status.PENDING,
        priority = Priority.MEDIUM,
        isLocked = false,
        deadline = null,
        preferredPlaceTime = null,
        duration = Duration.ofMinutes(30),
    )
}
