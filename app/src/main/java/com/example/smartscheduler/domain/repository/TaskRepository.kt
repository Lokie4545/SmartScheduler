package com.example.smartscheduler.domain.repository

import com.example.smartscheduler.domain.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.Period

interface TaskRepository {

    suspend fun getTasks(): List<Task>

    suspend fun getTasks(period: Period): List<Task>

    suspend fun createTask(task: Task): String

    suspend fun deleteTask(taskId: String)

    suspend fun updateTask(task: Task)

    suspend fun refreshTasks()

    fun getDayTasksStream(date: LocalDate): Flow<List<Task>>

    fun getUnallocatedTasksStream(): Flow<List<Task>>


}