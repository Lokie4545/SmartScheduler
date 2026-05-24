package com.example.smartscheduler.domain.model


import java.time.LocalDateTime
import java.time.Duration


interface TimeSlot {
    val startTime: LocalDateTime
    val endTime: LocalDateTime

    val duration: Duration
        get() = Duration.between(startTime, endTime)
}


enum class Priority {
    LOW, MEDIUM, HIGH
}


enum class Status {
    PENDING, SCHEDULED, COMPLETED, OVERDUE
}



sealed interface Task {
    val id: String
    val name: String
    val description: String?
    val status: Status
    val priority: Priority
    val isLocked: Boolean
    val deadline: LocalDateTime?

    val preferredPlaceTime: LocalDateTime?
}

data class UnscheduledTask(
    override val id: String,
    override val name: String,
    override val description: String?,
    override val status: Status,
    override val priority: Priority,
    override val isLocked: Boolean,
    override val deadline: LocalDateTime?,
    override val preferredPlaceTime: LocalDateTime?,

    val duration: Duration?,
) : Task


fun UnscheduledTask.schedule(timeSlot: TimeSlot) = ScheduledTask(
    id = id,
    name = name,
    description = description,
    status = Status.SCHEDULED,
    priority = priority,
    isLocked = isLocked,
    deadline = deadline,
    preferredPlaceTime = preferredPlaceTime,
    startTime = timeSlot.startTime,
    endTime = timeSlot.endTime,
)


data class ScheduledTask(
    override val id: String,
    override val name: String,
    override val description: String?,
    override val status: Status,
    override val priority: Priority,
    override val isLocked: Boolean,
    override val deadline: LocalDateTime?,
    override val preferredPlaceTime: LocalDateTime?,

    override val startTime: LocalDateTime,
    override val endTime: LocalDateTime,
) : Task, TimeSlot


fun ScheduledTask.unSchedule(): UnscheduledTask {
    return UnscheduledTask(
        id = this.id,
        name = this.name,
        description = this.description,
        status = Status.PENDING,
        priority = this.priority,
        isLocked = this.isLocked,
        deadline = this.deadline,
        preferredPlaceTime = this.preferredPlaceTime,

        duration = this.duration
    )
}