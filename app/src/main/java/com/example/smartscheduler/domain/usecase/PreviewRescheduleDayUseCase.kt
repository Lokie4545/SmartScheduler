package com.example.smartscheduler.domain.usecase

import com.example.smartscheduler.domain.algorithm.ScheduleResult
import com.example.smartscheduler.domain.algorithm.SmartSchedulerAlgorithm
import com.example.smartscheduler.domain.model.DiffItem
import com.example.smartscheduler.domain.model.Event
import com.example.smartscheduler.domain.model.ReschedulePreview
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Task
import com.example.smartscheduler.domain.model.TimeSlot
import com.example.smartscheduler.domain.model.UnscheduledTask
import com.example.smartscheduler.domain.repository.EventRepository
import com.example.smartscheduler.domain.repository.TaskRepository
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.schedule
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
                val futurePlacement = placeUnallocatedTasksOnFutureDays(
                    startDate = date,
                    workDayStart = workDayStart,
                    workDayEnd = workDayEnd,
                    unallocatedTasks = scheduleResult.unallocatedTasks,
                    oldPlan = oldPlan,
                )
                val proposedSchedule = scheduleResult.newSchedule + futurePlacement.scheduledTasks
                val proposedUnallocated = futurePlacement.unallocatedTasks
                val diff = withContext(Dispatchers.Default) {
                    val todayDiff = calculateDiffUseCase(
                        oldPlan = oldPlan,
                        backlog = backlog,
                        proposedPlan = scheduleResult.newSchedule,
                        newUnallocated = proposedUnallocated,
                    )
                    todayDiff + futurePlacement.scheduledTasks.map { task ->
                        DiffItem.Deferred(
                            taskId = task.id,
                            taskName = task.name,
                            oldStartTime = oldPlan.firstOrNull { it.id == task.id }?.startTime,
                            newStartTime = task.startTime,
                            newEndTime = task.endTime,
                        )
                    }
                }

                ReschedulePreviewResult.Success(
                    ReschedulePreview(
                        date = date,
                        oldPlan = oldPlan,
                        backlog = backlog,
                        proposedSchedule = proposedSchedule,
                        proposedUnallocated = proposedUnallocated,
                        diff = diff,
                    )
                )
            }
        }
    }

    private suspend fun placeUnallocatedTasksOnFutureDays(
        startDate: LocalDate,
        workDayStart: LocalTime,
        workDayEnd: LocalTime,
        unallocatedTasks: List<UnscheduledTask>,
        oldPlan: List<ScheduledTask>,
    ): FuturePlacement {
        val scheduledTasks = mutableListOf<ScheduledTask>()
        val remainingTasks = unallocatedTasks
            .filter { it.duration != null && it.status != Status.COMPLETED }
            .toMutableList()

        for (dayOffset in 1..FutureSearchDayLimit) {
            if (remainingTasks.isEmpty()) break

            val date = startDate.plusDays(dayOffset.toLong())
            val dayStart = LocalDateTime.of(date, workDayStart)
            val dayEnd = LocalDateTime.of(date, workDayEnd)
            val tasksOnDay = taskRepository.getTasks(dayStart, dayEnd)
                .filterIsInstance<ScheduledTask>()
                .filterNot { existingTask ->
                    existingTask.id in remainingTasks.map { it.id } || existingTask.id in oldPlan.map { it.id }
                }
            val eventsOnDay = eventRepository.getEvents(dayStart, dayEnd)
            val scheduledOnDay = scheduledTasks.filter { it.overlaps(dayStart, dayEnd) }
            val freeWindows = findFreeWindows(
                plan = tasksOnDay + eventsOnDay + scheduledOnDay,
                workDayStart = dayStart,
                workDayEnd = dayEnd,
            ).toMutableList()

            val iterator = remainingTasks.listIterator()
            while (iterator.hasNext()) {
                val task = iterator.next()
                val duration = task.duration ?: continue
                val windowIndex = freeWindows.indexOfFirst { window -> duration <= window.duration }
                if (windowIndex == -1) continue

                val window = freeWindows[windowIndex]
                val scheduledTask = task.schedule(
                    FutureTimeWindow(
                        startTime = window.startTime,
                        endTime = window.startTime.plus(duration),
                    )
                )
                scheduledTasks.add(scheduledTask)
                iterator.remove()

                val remainingWindow = window.copy(startTime = scheduledTask.endTime)
                if (remainingWindow.duration.isPositive && !remainingWindow.duration.isZero) {
                    freeWindows[windowIndex] = remainingWindow
                } else {
                    freeWindows.removeAt(windowIndex)
                }
            }
        }

        val unplaceableTaskIds = remainingTasks.map { it.id }.toSet()
        return FuturePlacement(
            scheduledTasks = scheduledTasks,
            unallocatedTasks = unallocatedTasks.filter { it.id in unplaceableTaskIds },
        )
    }

    private fun findFreeWindows(
        plan: List<TimeSlot>,
        workDayStart: LocalDateTime,
        workDayEnd: LocalDateTime,
    ): List<FutureTimeWindow> {
        val windows = mutableListOf<FutureTimeWindow>()
        var nextStart = workDayStart

        for (block in plan.sortedBy { it.startTime }) {
            if (!block.endTime.isAfter(workDayStart) || !block.startTime.isBefore(workDayEnd)) {
                continue
            }

            val blockStart = if (block.startTime.isBefore(workDayStart)) workDayStart else block.startTime
            val blockEnd = if (block.endTime.isAfter(workDayEnd)) workDayEnd else block.endTime

            if (nextStart.isBefore(blockStart)) {
                windows.add(FutureTimeWindow(nextStart, blockStart))
            }
            if (nextStart.isBefore(blockEnd)) {
                nextStart = blockEnd
            }
        }

        if (nextStart.isBefore(workDayEnd)) {
            windows.add(FutureTimeWindow(nextStart, workDayEnd))
        }

        return windows
    }

    private fun TimeSlot.overlaps(startTime: LocalDateTime, endTime: LocalDateTime): Boolean {
        return this.startTime < endTime && this.endTime > startTime
    }

    private data class FuturePlacement(
        val scheduledTasks: List<ScheduledTask>,
        val unallocatedTasks: List<UnscheduledTask>,
    )

    private data class FutureTimeWindow(
        override val startTime: LocalDateTime,
        override val endTime: LocalDateTime,
    ) : TimeSlot

    private companion object {
        const val FutureSearchDayLimit = 30
    }
}
