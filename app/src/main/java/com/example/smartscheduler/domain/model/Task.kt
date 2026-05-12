package com.example.smartscheduler.domain.model


import java.time.LocalDateTime
import java.time.Duration


interface TimeSlot {
    val startTime: LocalDateTime?
    val endTime: LocalDateTime?
}


enum class Priority {
    LOW, MEDIUM, HIGH
}


enum class Status {
    PENDING, SCHEDULED, COMPLETED, OVERDUE
}


data class Task(
    val id: String,
    val name: String,
    val description: String?,
    val status: Status,
    val duration: Duration?,
    val priority: Priority,
    val deadline: LocalDateTime?,
    val preferredPlaceTime: LocalDateTime?,
    val isLocked: Boolean,
    override val startTime: LocalDateTime?,
    override val endTime: LocalDateTime?,
): TimeSlot