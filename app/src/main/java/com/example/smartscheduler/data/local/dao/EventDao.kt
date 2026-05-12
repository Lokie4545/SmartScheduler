package com.example.smartscheduler.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.smartscheduler.data.local.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("""
        SELECT * FROM event
        WHERE startTime >= :startPeriod AND endTime <= :endPeriod
    """)
    fun observeAllEvents(
        startPeriod: Long,
        endPeriod: Long
    ): Flow<List<EventEntity>>


    @Upsert
    suspend fun upsertEvent(event: EventEntity)


    @Query("DELETE FROM event WHERE id = :eventId")
    suspend fun deleteEventById(eventId: String)
}