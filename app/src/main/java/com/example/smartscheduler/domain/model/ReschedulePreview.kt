package com.example.smartscheduler.domain.model

import java.time.LocalDate

data class ReschedulePreview(
    val date: LocalDate,
    val oldPlan: List<ScheduledTask>,
    val backlog: List<UnscheduledTask>,
    val proposedSchedule: List<TimeSlot>,
    val proposedUnallocated: List<UnscheduledTask>,
    val diff: List<DiffItem>,
)
