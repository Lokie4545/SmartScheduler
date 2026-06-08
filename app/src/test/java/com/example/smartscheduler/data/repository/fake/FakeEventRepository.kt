package com.example.smartscheduler.data.repository.fake

import com.example.smartscheduler.domain.model.Event
import com.example.smartscheduler.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

class FakeEventRepository(
    initialEvents: List<Event> = emptyList(),
) : EventRepository {

    private val eventsFlow = MutableStateFlow(initialEvents)

    override fun observeEvents(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Flow<List<Event>> {
        return eventsFlow.map { events -> events.filter { it.overlaps(startTime, endTime) } }
    }

    override suspend fun getEvents(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<Event> {
        return eventsFlow.value.filter { it.overlaps(startTime, endTime) }
    }

    override suspend fun getEvent(eventId: String): Event? {
        return eventsFlow.value.firstOrNull { it.id == eventId }
    }

    override suspend fun createEvent(event: Event): String {
        eventsFlow.update { currentEvents ->
            val index = currentEvents.indexOfFirst { it.id == event.id }
            if (index >= 0) currentEvents.toMutableList().apply { set(index, event) } else currentEvents + event
        }
        return event.id
    }

    override suspend fun updateEvent(event: Event) {
        createEvent(event)
    }

    override suspend fun deleteEvent(eventId: String) {
        eventsFlow.update { currentEvents -> currentEvents.filterNot { it.id == eventId } }
    }

    private fun Event.overlaps(startTime: LocalDateTime, endTime: LocalDateTime): Boolean {
        return this.startTime < endTime && this.endTime > startTime
    }
}
