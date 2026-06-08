package com.example.smartscheduler.domain.repository

import com.example.smartscheduler.domain.model.Event
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime


interface EventRepository {

    fun observeEvents(
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): Flow<List<Event>>

    suspend fun getEvents(startTime: LocalDateTime, endTime: LocalDateTime): List<Event>

    suspend fun getEvent(eventId: String): Event?

    suspend fun createEvent(event: Event): String

    suspend fun updateEvent(event: Event)

    suspend fun deleteEvent(eventId: String)

}
