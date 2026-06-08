package com.example.smartscheduler.domain.repository

import com.example.smartscheduler.domain.model.AppSettings
import com.example.smartscheduler.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.LocalTime

interface SettingsRepository {
    val settingsStream: Flow<AppSettings>

    suspend fun setThemeMode(themeMode: ThemeMode)

    suspend fun setDynamicColor(enabled: Boolean)

    suspend fun setWorkDayStart(time: LocalTime)

    suspend fun setWorkDayEnd(time: LocalTime)

    suspend fun setDefaultTaskDuration(duration: Duration)

    suspend fun setDefaultEventDuration(duration: Duration)

    suspend fun setWeekStartsOnMonday(enabled: Boolean)

    suspend fun reset()
}
