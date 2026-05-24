package com.example.smartscheduler.domain.usecase

import com.example.smartscheduler.domain.model.DiffItem
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.TimeSlot
import com.example.smartscheduler.domain.model.UnscheduledTask
import javax.inject.Inject

class CalculateDiffUseCase @Inject constructor() {
    operator fun invoke(
        oldPlan: List<ScheduledTask>,
        backlog: List<UnscheduledTask>,
        proposedPlan: List<TimeSlot>,
        newUnallocated: List<UnscheduledTask>,
    ): List<DiffItem> {
        val insertedTasks = proposedPlan.filterIsInstance<ScheduledTask>()
        val diffItems = mutableListOf<DiffItem>()

        for (task in insertedTasks) {
            val taskInOldPlan = oldPlan.firstOrNull { it.id == task.id }

            if (taskInOldPlan != null) {
                if (taskInOldPlan.startTime == task.startTime) {
                    diffItems.add(DiffItem.Unchanged(task.id, task.name))
                } else {
                    diffItems.add(
                        DiffItem.Moved(
                            task.id,
                            task.name,
                            taskInOldPlan.startTime,
                            task.startTime
                        )
                    )
                }
            } else {
                val taskInBacklog = backlog.firstOrNull { it.id == task.id }
                if (taskInBacklog != null) {
                    diffItems.add(
                        DiffItem.Added(
                            task.name,
                            task.id,
                            task.startTime
                        )
                    )
                }
            }

        }

        for(task in oldPlan) {
            val taskInUnallocated = newUnallocated.firstOrNull { it.id == task.id }

            if (taskInUnallocated != null) {
                diffItems.add(DiffItem.Evicted(task.id, task.name))
            }
        }

        return diffItems.toList()
    }
}