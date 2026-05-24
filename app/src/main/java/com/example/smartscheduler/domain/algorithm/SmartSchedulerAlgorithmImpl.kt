package com.example.smartscheduler.domain.algorithm

import com.example.smartscheduler.domain.model.ScheduledTask
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

        val backlogAllocated =
            backLog.filter { it.duration != null }.sortedByDescending { it.priority }
                .toMutableList()


        val remainingPlan = backlogAllocated.maxOfOrNull { it.priority }?.let { maxPriority ->
            val (evictedTasks, remainingPlan) = currentPlan.partition {
                it is ScheduledTask && !it.isLocked && it.priority < maxPriority
            }
            backlogAllocated.addAll(
                evictedTasks.filterIsInstance<ScheduledTask>().map { it.unSchedule() })
            backlogAllocated.sortByDescending { it.priority }
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

        val finalSchedule: List<TimeSlot> = remainingPlan + allocatedTasks

        return ScheduleResult.Success(finalSchedule, unallocatedTasks)
    }


    private fun findFreeWindows(plan: List<TimeSlot>, workTime: WorkTime): List<TimeWindow> {
        val freeWindows = mutableListOf<TimeWindow>()
        var startTime = workTime.startTime
        plan.sortedBy { it.startTime }
            .forEach { timeBlock ->
                if ((startTime.isBefore(timeBlock.startTime))) {
                    freeWindows.add(
                        TimeWindow(
                            startTime = startTime,
                            endTime = timeBlock.startTime
                        )
                    )
                    startTime =
                        if (startTime.isBefore(timeBlock.endTime)) timeBlock.endTime else startTime
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