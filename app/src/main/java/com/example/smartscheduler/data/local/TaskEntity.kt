package com.example.smartscheduler.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity(tableName = "task")
data class TaskEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val status: String,
    val duration: Long?,
    val priority: String,
    val deadline: Long?,
    val preferredPlaceTime: Long?,
    val isLocked: Boolean,
    val startTime: Long?,
    val endTime: Long?,
)