package com.example.smartscheduler.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity(tableName = "event")
data class EventEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val startTime: Long,
    val endTime: Long
)