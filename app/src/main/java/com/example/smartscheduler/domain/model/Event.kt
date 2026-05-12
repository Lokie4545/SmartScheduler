package com.example.smartscheduler.domain.model

import java.time.LocalDateTime


data class Event(
    val id: String,
    val name: String,
    val description: String?,
    override val startTime: LocalDateTime,
    override val endTime: LocalDateTime,
): TimeSlot
