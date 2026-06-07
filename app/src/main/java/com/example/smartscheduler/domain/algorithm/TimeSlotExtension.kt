package com.example.smartscheduler.domain.algorithm

import android.icu.util.TimeZone
import com.example.smartscheduler.domain.model.TimeSlot
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

fun TimeSlot.overlapsWith(other: TimeSlot): Boolean {
    return (this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime))
}

fun LocalDateTime.roundToNextHalfHour(): LocalDateTime {
    val truncatedTime = this.truncatedTo(ChronoUnit.HOURS)
    return if (minute < 30) {
        truncatedTime.plusMinutes(30)
    }
    else {
        truncatedTime.plusHours(1)
    }
}

fun LocalDateTime.toDefaultEventSlot(durationMinutes: Long = 60): TimeSlot {
    val start = this.roundToNextHalfHour()
    val end = start.plusMinutes(durationMinutes)

    return object : TimeSlot {
        override val startTime: LocalDateTime = start
        override val endTime: LocalDateTime = end
    }
}
