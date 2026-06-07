package com.example.smartscheduler.domain.algorithm

import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.TimeSlot
import com.example.smartscheduler.domain.model.UnscheduledTask
import com.example.smartscheduler.domain.model.schedule
import com.example.smartscheduler.domain.model.unSchedule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

class SmartSchedulerAlgorithmImpl @Inject constructor(

) : SmartSchedulerAlgorithm {

    data class WorkTime(
        override val startTime: LocalDateTime,
        override val endTime: LocalDateTime

    ) : TimeSlot

    data class TimeWindow(
        override val startTime: LocalDateTime,
        override val endTime: LocalDateTime

    ) : TimeSlot


    override fun calculateSchedule(
        currentPlan: List<TimeSlot>,
        backLog: List<UnscheduledTask>,
        targetDay: LocalDate,
        workDayStart: LocalTime,
        workDayEnd: LocalTime
    ): ScheduleResult {

        val workTime = WorkTime(
            startTime = LocalDateTime.of(targetDay, workDayStart),
            endTime = LocalDateTime.of(targetDay, workDayEnd)
        )

        val backlogAllocated = backLog
            .filter { it.duration != null && it.status != Status.COMPLETED }
            .sortedWith(
                compareByDescending<UnscheduledTask> { it.priority }
                    .thenBy { it.deadline ?: LocalDateTime.MAX }
                    .thenBy { it.duration }
                    .thenBy { it.id }
            )
            .toMutableList()


        val remainingPlan = backlogAllocated.maxOfOrNull { it.priority }?.let { maxPriority ->
            val (evictedTasks, remainingPlan) = currentPlan.partition {
                it is ScheduledTask &&
                    !it.isLocked &&
                    it.status != Status.COMPLETED &&
                    it.priority < maxPriority
            }
            backlogAllocated.addAll(
                evictedTasks.filterIsInstance<ScheduledTask>().map { it.unSchedule() })
            backlogAllocated.sortWith(
                compareByDescending<UnscheduledTask> { it.priority }
                    .thenBy { it.deadline ?: LocalDateTime.MAX }
                    .thenBy { it.duration }
                    .thenBy { it.id }
            )
            remainingPlan
        } ?: currentPlan


        val freeTimeWindows = findFreeWindows(
            plan = remainingPlan,
            workTime = workTime
        ).toMutableList()

        val allocatedTasks = mutableListOf<TimeSlot>()
        val unallocatedTasks = mutableListOf<UnscheduledTask>()

        for (task in backlogAllocated) {
            val freeWindowIndex =
                freeTimeWindows.indexOfFirst { window -> task.duration!! <= window.duration }
            if (freeWindowIndex > -1) {

                val window = freeTimeWindows[freeWindowIndex]
                val timeRemainder = window.duration.minus(task.duration)

                val assignedTask = task.schedule(
                    TimeWindow(
                        startTime = window.startTime,
                        endTime = window.startTime.plus(task.duration)
                    ),


                    )

                allocatedTasks.add(assignedTask)

                if (!timeRemainder.isZero && timeRemainder.isPositive) {
                    freeTimeWindows[freeWindowIndex] = window.copy(
                        startTime = assignedTask.endTime
                    )
                } else {
                    freeTimeWindows.removeAt(freeWindowIndex)
                }

            } else {
                unallocatedTasks.add(task)
            }
        }

        val finalSchedule: List<TimeSlot> = (remainingPlan + allocatedTasks).sortedBy { it.startTime }

        return ScheduleResult.Success(finalSchedule, unallocatedTasks)
    }


    private fun findFreeWindows(plan: List<TimeSlot>, workTime: WorkTime): List<TimeWindow> {
        val freeWindows = mutableListOf<TimeWindow>()
        var startTime = workTime.startTime
        for (timeBlock in plan.sortedBy { it.startTime }) {
            if (!timeBlock.endTime.isAfter(workTime.startTime) || !timeBlock.startTime.isBefore(workTime.endTime)) {
                continue
            }

            val blockStart = if (timeBlock.startTime.isBefore(workTime.startTime)) {
                workTime.startTime
            } else {
                timeBlock.startTime
            }
            val blockEnd = if (timeBlock.endTime.isAfter(workTime.endTime)) {
                workTime.endTime
            } else {
                timeBlock.endTime
            }

            if (startTime.isBefore(blockStart)) {
                freeWindows.add(
                    TimeWindow(
                        startTime = startTime,
                        endTime = blockStart
                    )
                )
            }

            if (startTime.isBefore(blockEnd)) {
                startTime = blockEnd
            }
        }
        if (startTime.isBefore(workTime.endTime)) {
            freeWindows.add(
                TimeWindow(startTime = startTime, endTime = workTime.endTime)
            )
        }
        return freeWindows
    }
}
