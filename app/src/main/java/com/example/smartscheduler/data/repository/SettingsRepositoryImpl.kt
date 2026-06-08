package com.example.smartscheduler.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.smartscheduler.domain.model.AppSettings
import com.example.smartscheduler.domain.model.ThemeMode
import com.example.smartscheduler.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.LocalTime
import javax.inject.Inject

private val Context.settingsDataStore by preferencesDataStore(name = "smart_scheduler_settings")

class SettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : SettingsRepository {

    override val settingsStream: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        val defaultSettings = AppSettings()
        AppSettings(
            themeMode = preferences[ThemeModeKey]?.let { value ->
                runCatching { ThemeMode.valueOf(value) }.getOrDefault(defaultSettings.themeMode)
            } ?: defaultSettings.themeMode,
            dynamicColor = preferences[DynamicColorKey] ?: defaultSettings.dynamicColor,
            workDayStart = (preferences[WorkDayStartMinutesKey] ?: defaultSettings.workDayStart.toMinutesOfDay())
                .toLocalTime(),
            workDayEnd = (preferences[WorkDayEndMinutesKey] ?: defaultSettings.workDayEnd.toMinutesOfDay())
                .toLocalTime(),
            defaultTaskDuration = Duration.ofMinutes(
                (preferences[DefaultTaskDurationMinutesKey] ?: defaultSettings.defaultTaskDuration.toMinutes().toInt()).toLong()
            ),
            defaultEventDuration = Duration.ofMinutes(
                (preferences[DefaultEventDurationMinutesKey] ?: defaultSettings.defaultEventDuration.toMinutes().toInt()).toLong()
            ),
            weekStartsOnMonday = preferences[WeekStartsOnMondayKey] ?: defaultSettings.weekStartsOnMonday,
        )
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        context.settingsDataStore.edit { preferences ->
            preferences[ThemeModeKey] = themeMode.name
        }
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[DynamicColorKey] = enabled
        }
    }

    override suspend fun setWorkDayStart(time: LocalTime) {
        context.settingsDataStore.edit { preferences ->
            preferences[WorkDayStartMinutesKey] = time.toMinutesOfDay()
        }
    }

    override suspend fun setWorkDayEnd(time: LocalTime) {
        context.settingsDataStore.edit { preferences ->
            preferences[WorkDayEndMinutesKey] = time.toMinutesOfDay()
        }
    }

    override suspend fun setDefaultTaskDuration(duration: Duration) {
        context.settingsDataStore.edit { preferences ->
            preferences[DefaultTaskDurationMinutesKey] = duration.toMinutes().toInt()
        }
    }

    override suspend fun setDefaultEventDuration(duration: Duration) {
        context.settingsDataStore.edit { preferences ->
            preferences[DefaultEventDurationMinutesKey] = duration.toMinutes().toInt()
        }
    }

    override suspend fun setWeekStartsOnMonday(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[WeekStartsOnMondayKey] = enabled
        }
    }

    override suspend fun reset() {
        context.settingsDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private fun LocalTime.toMinutesOfDay(): Int = hour * 60 + minute

    private fun Int.toLocalTime(): LocalTime {
        val clampedMinutes = coerceIn(0, MinutesPerDay - 1)
        return LocalTime.of(clampedMinutes / 60, clampedMinutes % 60)
    }

    private companion object {
        const val MinutesPerDay = 24 * 60
        val ThemeModeKey = stringPreferencesKey("theme_mode")
        val DynamicColorKey = booleanPreferencesKey("dynamic_color")
        val WorkDayStartMinutesKey = intPreferencesKey("work_day_start_minutes")
        val WorkDayEndMinutesKey = intPreferencesKey("work_day_end_minutes")
        val DefaultTaskDurationMinutesKey = intPreferencesKey("default_task_duration_minutes")
        val DefaultEventDurationMinutesKey = intPreferencesKey("default_event_duration_minutes")
        val WeekStartsOnMondayKey = booleanPreferencesKey("week_starts_on_monday")
    }
}
