package com.example.smartscheduler.data.repository

import com.example.smartscheduler.data.local.dao.TaskDao
import com.example.smartscheduler.data.mapper.toDomain
import com.example.smartscheduler.data.mapper.toEntity
import com.example.smartscheduler.domain.model.Task
import com.example.smartscheduler.domain.model.UnscheduledTask
import com.example.smartscheduler.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {
    override suspend fun getUnallocatedTasks(): List<UnscheduledTask> {
        return taskDao.getUnallocatedTasks().map { it.toDomain() }
            .filterIsInstance<UnscheduledTask>()
    }

    override suspend fun getTask(taskId: String): Task? {
        return taskDao.getTaskById(taskId)?.toDomain()
    }

    override suspend fun getTasks(
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): List<Task> {

        return taskDao.getTasks(
            startTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
            endTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
        ).map { it.toDomain() }
    }

    override suspend fun applyReschedule(tasks: List<Task>) {
        taskDao.upsertTasks(tasks.map { it.toEntity() })
    }

    override suspend fun createTask(task: Task): String {
        taskDao.upsertTask(task.toEntity())
        return task.id
    }

    override suspend fun deleteTask(taskId: String) {
        taskDao.deleteTaskById(taskId)
    }

    override suspend fun updateTask(task: Task) {
        taskDao.upsertTask(task = task.toEntity())
    }


    override fun getDayTasksStream(date: LocalDate): Flow<List<Task>> {
        val startTime = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val endTime =
            date
                .plusDays(1)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli()
        return taskDao.observeTasks(
            startTime,
            endTime
        ).map { it.map { entity -> entity.toDomain() } }
    }

    override fun observeTasks(startTime: LocalDateTime, endTime: LocalDateTime): Flow<List<Task>> {
        return taskDao.observeTasks(
            startTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
            endTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
        ).map { it.map { entity -> entity.toDomain() } }
    }

    override fun getUnallocatedTasksStream(): Flow<List<UnscheduledTask>> {
        return taskDao.observeUnallocatedTasks()
            .map { it.map { entity -> entity.toDomain() }.filterIsInstance<UnscheduledTask>() }
    }
}
