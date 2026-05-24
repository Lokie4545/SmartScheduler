package com.example.smartscheduler.domain.usecase

import com.example.smartscheduler.domain.model.Task
import com.example.smartscheduler.domain.repository.TaskRepository
import javax.inject.Inject

class ApplyRescheduleUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(newSchedule: List<Task>) {
        taskRepository.applyReschedule(newSchedule)
    }
}