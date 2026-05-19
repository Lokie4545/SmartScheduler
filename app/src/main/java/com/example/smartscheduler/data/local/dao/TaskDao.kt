package com.example.smartscheduler.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.smartscheduler.data.local.EventEntity
import com.example.smartscheduler.data.local.TaskEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface TaskDao {

    @Query("""
        SELECT * FROM task
        WHERE startTime >= :startPeriod AND endTime <= :endPeriod
    """)
    fun observeTasks(
        startPeriod: Long,
        endPeriod: Long
    ): Flow<List<TaskEntity>>


    @Query("""
        SELECT * FROM task
        WHERE startTime IS NULL AND endTime IS NULL
    """
    )
    fun observeUnallocatedTasks() : Flow<List<TaskEntity>>


    @Query("""
        SELECT * FROM task
        WHERE startTime >= :startPeriod AND endTime <= :endPeriod
    """)
    suspend fun getTasks(startPeriod: Long, endPeriod: Long): List<TaskEntity>


    @Query("""
        SELECT * FROM task
        WHERE startTime IS NULL AND endTime IS NULL
    """
    )
    suspend fun getUnallocatedTasks(): List<TaskEntity>


    @Query("""
        SELECT * FROM task
        WHERE id = :taskId
    """)
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Query("DELETE FROM task WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String): Int

    @Upsert
    suspend fun upsertTask(task: TaskEntity)
}