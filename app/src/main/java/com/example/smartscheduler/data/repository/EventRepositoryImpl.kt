package com.example.smartscheduler.data.repository

import com.example.smartscheduler.data.local.EventEntity
import com.example.smartscheduler.data.local.dao.EventDao
import com.example.smartscheduler.data.mapper.toDomain
import com.example.smartscheduler.data.mapper.toEntity
import com.example.smartscheduler.domain.model.Event
import com.example.smartscheduler.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject


class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao
) : EventRepository {

    override fun observeEvents(
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): Flow<List<Event>> {
        return eventDao.observeEvents(
            startTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
            endTime.toInstant(ZoneOffset.UTC).toEpochMilli()
        ).map { it.map(EventEntity::toDomain) }
    }

    override suspend fun getEvents(
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): List<Event> {
        return eventDao.getEvents(
            startTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
            endTime.toInstant(ZoneOffset.UTC).toEpochMilli()
        ).map (EventEntity::toDomain)
    }

    override suspend fun getEvent(eventId: String): Event? {
        return eventDao.getEventById(eventId)?.toDomain()
    }

    override suspend fun createEvent(event: Event): String {
        eventDao.upsertEvent(event.toEntity())
        return event.id
    }

    override suspend fun updateEvent(event: Event) {
        eventDao.upsertEvent(event.toEntity())
    }

    override suspend fun deleteEvent(eventId: String) {
        eventDao.deleteEventById(eventId)
    }
}
