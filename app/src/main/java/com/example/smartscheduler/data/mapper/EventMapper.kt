package com.example.smartscheduler.data.mapper

import com.example.smartscheduler.data.local.EventEntity
import com.example.smartscheduler.domain.model.Event
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

//Converting model Entity -> Domain
fun EventEntity.toDomain(): Event = Event(
    id = this.id,
    name = this.name,
    description = this.description,
    startTime = this.startTime.let { time ->
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(time),
            ZoneOffset.UTC
        )
    },
    endTime = this.endTime.let { time ->
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(time),
            ZoneOffset.UTC
        )
    }
)


//Converting model Domain -> Entity
fun Event.toEntity(): EventEntity = EventEntity(
    id = this.id,
    name = this.name,
    description = this.description,
    startTime = this.startTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
    endTime = this.endTime.toInstant(ZoneOffset.UTC).toEpochMilli()
)
