package com.example.smartscheduler.data.mapper

import com.example.smartscheduler.data.local.TaskEntity
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.Task
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset


//Converting model Entity -> Domain
fun TaskEntity.toDomain(): Task = Task(
    id = id,
    name = name,
    description = description,
    status = Status.valueOf(status.uppercase()),
    duration = duration?.let { Duration.ofMillis(duration) },
    priority = Priority.valueOf(priority.uppercase()),
    deadline = deadline?.let {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(deadline),
            ZoneOffset.UTC
        )
    },
    preferredPlaceTime = preferredPlaceTime?.let {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(preferredPlaceTime),
            ZoneOffset.UTC
        )
    },
    isLocked = isLocked,
    startTime = startTime?.let {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(startTime),
            ZoneOffset.UTC
        )
    },
    endTime = endTime?.let {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(endTime),
            ZoneOffset.UTC
        )
    },
)

//From Domain -> Entity
fun Task.toEntity() = TaskEntity(
    id = id,
    name = name,
    description = description,
    status = status.name,
    duration = duration?.toMillis(),
    priority = priority.name,
    deadline = deadline?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
    preferredPlaceTime = preferredPlaceTime?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
    isLocked = isLocked,
    startTime = startTime?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
    endTime = endTime?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
)

