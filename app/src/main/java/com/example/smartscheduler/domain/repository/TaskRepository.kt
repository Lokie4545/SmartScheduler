package com.example.smartscheduler.domain.repository

import com.example.smartscheduler.domain.model.Task
import com.example.smartscheduler.domain.model.UnscheduledTask
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface TaskRepository {

    suspend fun getUnallocatedTasks(): List<UnscheduledTask>

    suspend fun getTask(taskId: String): Task?

    suspend fun getTasks(startTime: LocalDateTime, endTime: LocalDateTime): List<Task>

    suspend fun applyReschedule(tasks: List<Task>)

    suspend fun createTask(task: Task): String

    suspend fun deleteTask(taskId: String)

    suspend fun updateTask(task: Task)

    fun getDayTasksStream(date: LocalDate): Flow<List<Task>>

    fun observeTasks(startTime: LocalDateTime, endTime: LocalDateTime): Flow<List<Task>>

    fun getUnallocatedTasksStream(): Flow<List<UnscheduledTask>>


}
