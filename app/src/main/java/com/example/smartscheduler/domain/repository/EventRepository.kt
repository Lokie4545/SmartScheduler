package com.example.smartscheduler.domain.repository

import com.example.smartscheduler.domain.model.Event
import java.time.LocalDateTime


interface EventRepository {
    suspend fun getEvents(startTime: LocalDateTime, endTime: LocalDateTime): List<Event>

    suspend fun createEvent(event: Event): String

    suspend fun deleteEvent(eventId: String)

}