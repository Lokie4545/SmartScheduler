package com.example.smartscheduler.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.smartscheduler.data.local.dao.EventDao
import com.example.smartscheduler.data.local.dao.TaskDao


@Database(
    version = 1,
    entities = [
        TaskEntity::class,
        EventEntity::class
    ],
    exportSchema = true
)
abstract class SmartSchedulerDatabase: RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun eventDao(): EventDao
}