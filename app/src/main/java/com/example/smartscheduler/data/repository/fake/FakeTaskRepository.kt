package com.example.smartscheduler.data.repository.fake

import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Task
import com.example.smartscheduler.domain.model.UnscheduledTask
import com.example.smartscheduler.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalDateTime

class FakeTaskRepository(
    initialTasks: List<Task> = emptyList()
) : TaskRepository {

    private val tasksFlow = MutableStateFlow(initialTasks)

    override suspend fun getUnallocatedTasks(): List<UnscheduledTask> {
        return tasksFlow.value.filterIsInstance<UnscheduledTask>()
    }

    override suspend fun getTask(taskId: String): Task? {
        return tasksFlow.value.firstOrNull { it.id == taskId }
    }

    override suspend fun getTasks(
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): List<Task> {
        return tasksFlow.value.filterIsInstance<ScheduledTask>()
            .filter { it.overlaps(startTime, endTime) }
    }

    override suspend fun applyReschedule(tasks: List<Task>) {
        tasksFlow.update { currentTasks ->
            val taskMap = currentTasks.associateBy { it.id }.toMutableMap()
            tasks.forEach { taskMap[it.id] = it }
            taskMap.values.toList()
        }
    }

    override suspend fun createTask(task: Task): String {
        tasksFlow.update { currentTasks ->
            val index = currentTasks.indexOfFirst { it.id == task.id }
            if (index >= 0) currentTasks.toMutableList().apply { set(index, task) }
            else currentTasks + task
        }
        return task.id
    }

    override suspend fun deleteTask(taskId: String) {
        tasksFlow.update { currentTasks ->
            currentTasks.filterNot { it.id == taskId }
        }
    }

    override suspend fun updateTask(task: Task) {
        createTask(task)
    }

    override fun getDayTasksStream(date: LocalDate): Flow<List<Task>> {
        val startTime = date.atStartOfDay()
        val endTime = date.plusDays(1).atStartOfDay()
        return tasksFlow.map { tasks ->
            tasks.filterIsInstance<ScheduledTask>()
                .filter { it.overlaps(startTime, endTime) }
        }
    }

    override fun observeTasks(startTime: LocalDateTime, endTime: LocalDateTime): Flow<List<Task>> {
        return tasksFlow.map { tasks ->
            tasks.filterIsInstance<ScheduledTask>()
                .filter { it.overlaps(startTime, endTime) }
        }
    }

    override fun getUnallocatedTasksStream(): Flow<List<UnscheduledTask>> {
        return tasksFlow.map { tasks ->
            tasks.filterIsInstance<UnscheduledTask>()
        }
    }

    private fun ScheduledTask.overlaps(startTime: LocalDateTime, endTime: LocalDateTime): Boolean {
        return this.startTime < endTime && this.endTime > startTime
    }
}
