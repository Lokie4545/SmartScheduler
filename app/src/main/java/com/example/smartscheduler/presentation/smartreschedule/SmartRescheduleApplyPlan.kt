package com.example.smartscheduler.presentation.smartreschedule

import com.example.smartscheduler.domain.model.DiffItem
import com.example.smartscheduler.domain.model.ReschedulePreview
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Task
import com.example.smartscheduler.domain.model.UnscheduledTask
import com.example.smartscheduler.domain.model.unSchedule

internal fun buildSmartRescheduleTasksToApply(
    preview: ReschedulePreview,
    rejectedTaskIds: Set<String>,
): List<Task> {
    val proposedTasks = preview.proposedSchedule.filterIsInstance<ScheduledTask>()
    val selectedScheduledTasks = proposedTasks.filterNot { it.id in rejectedTaskIds }
    val rejectedTasks = rejectedTaskIds.mapNotNull { taskId ->
        preview.oldPlan.firstOrNull { it.id == taskId }
            ?: preview.backlog.firstOrNull { it.id == taskId }
            ?: proposedTasks.firstOrNull { it.id == taskId }?.unSchedule()
            ?: preview.proposedUnallocated.firstOrNull { it.id == taskId }
    }
    val selectedUnallocatedTasks = preview.proposedUnallocated.filterNot { it.id in rejectedTaskIds }

    return (selectedScheduledTasks + rejectedTasks + selectedUnallocatedTasks)
        .distinctBy { it.id }
}

internal fun ReschedulePreview.canRejectChange(taskId: String): Boolean {
    return diff.any { it.taskId == taskId && it !is DiffItem.Unchanged }
}
