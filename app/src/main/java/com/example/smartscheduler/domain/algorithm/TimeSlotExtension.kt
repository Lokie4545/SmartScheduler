package com.example.smartscheduler.domain.algorithm

import com.example.smartscheduler.domain.model.TimeSlot
import java.time.Duration

fun TimeSlot.overlapsWith(other: TimeSlot): Boolean {
    return (this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime))
}
