package com.example.smartscheduler.domain.usecase

import com.example.smartscheduler.domain.algorithm.ScheduleResult
import com.example.smartscheduler.domain.algorithm.SmartSchedulerAlgorithm
import com.example.smartscheduler.domain.model.Event
import com.example.smartscheduler.domain.model.ReschedulePreview
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Task
import com.example.smartscheduler.domain.model.UnscheduledTask
import com.example.smartscheduler.domain.repository.EventRepository
import com.example.smartscheduler.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

sealed interface ReschedulePreviewResult {
    data class Success(val preview: ReschedulePreview) : ReschedulePreviewResult
    data class Failure(val message: String) : ReschedulePreviewResult
}

class PreviewRescheduleDayUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val eventRepository: EventRepository,
    private val smartSchedulerAlgorithm: SmartSchedulerAlgorithm,
    private val calculateDiffUseCase: CalculateDiffUseCase,
) {
    suspend operator fun invoke(
        date: LocalDate,
        workDayStart: LocalTime,
        workDayEnd: LocalTime,
    ): ReschedulePreviewResult = coroutineScope {
        val startDayTime = LocalDateTime.of(date, workDayStart)
        val endDayTime = LocalDateTime.of(date, workDayEnd)

        val deferredTasksOnToday = async {
            taskRepository.getTasks(startDayTime, endDayTime)
        }
        val deferredEventsOnToday = async {
            eventRepository.getEvents(startDayTime, endDayTime)
        }
        val deferredBacklog = async {
            taskRepository.getUnallocatedTasks()
        }

        val tasksOnToday = deferredTasksOnToday.await()
        val eventsOnToday = deferredEventsOnToday.await()
        val backlog = deferredBacklog.await()

        fromSnapshot(
            date = date,
            workDayStart = workDayStart,
            workDayEnd = workDayEnd,
            tasksOnToday = tasksOnToday,
            eventsOnToday = eventsOnToday,
            backlog = backlog,
        )
    }

    suspend fun fromSnapshot(
        date: LocalDate,
        workDayStart: LocalTime,
        workDayEnd: LocalTime,
        tasksOnToday: List<Task>,
        eventsOnToday: List<Event>,
        backlog: List<UnscheduledTask>,
    ): ReschedulePreviewResult {
        val oldPlan = tasksOnToday.filterIsInstance<ScheduledTask>()
        val currentPlan = oldPlan + eventsOnToday

        return when (val scheduleResult = withContext(Dispatchers.Default) {
            smartSchedulerAlgorithm.calculateSchedule(
                currentPlan = currentPlan,
                backLog = backlog,
                targetDay = date,
                workDayStart = workDayStart,
                workDayEnd = workDayEnd,
            )
        }) {
            is ScheduleResult.Failure -> ReschedulePreviewResult.Failure(scheduleResult.error)
            is ScheduleResult.Success -> {
                val diff = withContext(Dispatchers.Default) {
                    calculateDiffUseCase(
                        oldPlan = oldPlan,
                        backlog = backlog,
                        proposedPlan = scheduleResult.newSchedule,
                        newUnallocated = scheduleResult.unallocatedTasks,
                    )
                }

                ReschedulePreviewResult.Success(
                    ReschedulePreview(
                        date = date,
                        oldPlan = oldPlan,
                        backlog = backlog,
                        proposedSchedule = scheduleResult.newSchedule,
                        proposedUnallocated = scheduleResult.unallocatedTasks,
                        diff = diff,
                    )
                )
            }
        }
    }
}
