package com.example.smartscheduler.di

import android.content.Context
import androidx.room.Room
import com.example.smartscheduler.data.local.SmartSchedulerDatabase
import com.example.smartscheduler.data.local.dao.EventDao
import com.example.smartscheduler.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): SmartSchedulerDatabase{
        return Room.databaseBuilder(
            context.applicationContext,
            SmartSchedulerDatabase::class.java,
            "smart_scheduler.db"
        ).build()
    }

    @Provides
    fun provideTaskDao(database: SmartSchedulerDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideEventDao(database: SmartSchedulerDatabase): EventDao = database.eventDao()
}