package com.example.smartscheduler.presentation.smartreschedule

import com.example.smartscheduler.domain.model.UnscheduledTask

internal fun List<UnscheduledTask>.filterSelectedForSmartReschedule(
    deselectedTaskIds: Set<String>,
): List<UnscheduledTask> {
    return filter { task -> task.id !in deselectedTaskIds }
}
