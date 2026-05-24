package com.example.smartscheduler.domain.algorithm

import com.example.smartscheduler.domain.model.TimeSlot
import com.example.smartscheduler.domain.model.UnscheduledTask
import java.time.LocalDate
import java.time.LocalTime

sealed interface ScheduleResult {
    data class Success(
        val newSchedule: List<TimeSlot>,
        val unallocatedTasks: List<UnscheduledTask>
    ): ScheduleResult

    data class Failure(val error: String): ScheduleResult
}

interface SmartSchedulerAlgorithm {
    fun calculateSchedule(
        currentPlan: List<TimeSlot>,
        backLog: List<UnscheduledTask>,
        targetDay: LocalDate,
        workDayStart: LocalTime,
        workDayEnd: LocalTime
    ): ScheduleResult
}