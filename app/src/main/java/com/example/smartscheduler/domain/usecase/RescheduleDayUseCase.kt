package com.example.smartscheduler.domain.usecase

import com.example.smartscheduler.domain.algorithm.ScheduleResult
import com.example.smartscheduler.domain.algorithm.SmartSchedulerAlgorithm
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.TimeSlot
import com.example.smartscheduler.domain.repository.EventRepository
import com.example.smartscheduler.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

class RescheduleDayUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val eventRepository: EventRepository,
    private val smartSchedulerAlgorithm: SmartSchedulerAlgorithm
) {
    suspend operator fun invoke(
        date: LocalDate,
        workDayStart: LocalTime,
        workDayEnd: LocalTime
    ): ScheduleResult {

        val startDayTime = LocalDateTime.of(date, workDayStart)
        val endDayTime = LocalDateTime.of(date, workDayEnd)

        return coroutineScope {
            val deferredTasksOnToday = async {
                taskRepository.getTasks(startDayTime, endDayTime)
            }

            val deferredEventsOnToday = async {
                eventRepository.getEvents(startDayTime, endDayTime)
            }

            val deferredBacklog = async {
                taskRepository.getUnallocatedTasks()
            }

            val taskOnToday = deferredTasksOnToday.await()
            val eventsOnToday = deferredEventsOnToday.await()
            val backLog = deferredBacklog.await()

            val scheduledTasks = taskOnToday.filterIsInstance<ScheduledTask>()

            val todayPlan = scheduledTasks + eventsOnToday

            smartSchedulerAlgorithm.calculateSchedule(
                currentPlan = todayPlan.toList(),
                backLog = backLog,
                targetDay = date,
                workDayStart = workDayStart,
                workDayEnd = workDayEnd
            )
        }
    }
}