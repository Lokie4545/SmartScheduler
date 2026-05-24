package com.example.smartscheduler.data.mapper

import com.example.smartscheduler.data.local.TaskEntity
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.Task
import com.example.smartscheduler.domain.model.UnscheduledTask
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset


//Converting model Entity -> Domain
fun TaskEntity.toDomain(): Task {
    val parsedStartTime = startTime?.let {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(startTime),
            ZoneOffset.UTC
        )
    }

    val parsedEndTime = endTime?.let {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(endTime),
            ZoneOffset.UTC
        )
    }

    val parsedDeadline = deadline?.let {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(deadline),
            ZoneOffset.UTC
        )
    }

    val parsedPreferredPlaceTime = preferredPlaceTime?.let {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(preferredPlaceTime),
            ZoneOffset.UTC
        )
    }

    val parsedStatus = Status.valueOf(status.uppercase())

    val parsedPriority = Priority.valueOf(priority.uppercase())

    return if (parsedEndTime != null && parsedStartTime != null) {
        ScheduledTask(
            id = id,
            name = name,
            description = description,
            status = parsedStatus,
            priority = parsedPriority,
            deadline = parsedDeadline,
            preferredPlaceTime = parsedPreferredPlaceTime,
            isLocked = isLocked,
            startTime = parsedStartTime,
            endTime = parsedEndTime
        )
    }
    else {
        UnscheduledTask(
            id = id,
            name = name,
            description = description,
            status = parsedStatus,
            priority = parsedPriority,
            deadline = parsedDeadline,
            preferredPlaceTime = parsedPreferredPlaceTime,
            isLocked = isLocked,
            duration = null,
        )
    }

}

//From Domain -> Entity
fun Task.toEntity() = when(this) {
    is ScheduledTask ->  TaskEntity(
        id = id,
        name = name,
        description = description,
        status = status.name,
        duration = duration.toMillis(),
        priority = priority.name,
        deadline = deadline?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        preferredPlaceTime = preferredPlaceTime?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        isLocked = isLocked,
        startTime = startTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli(),
        endTime = endTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )
    is UnscheduledTask -> TaskEntity(
        id = id,
        name = name,
        description = description,
        status = status.name,
        duration = duration?.toMillis(),
        priority = priority.name,
        deadline = deadline?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        preferredPlaceTime = preferredPlaceTime?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        isLocked = isLocked,
        startTime = null,
        endTime = null,
    )
}

