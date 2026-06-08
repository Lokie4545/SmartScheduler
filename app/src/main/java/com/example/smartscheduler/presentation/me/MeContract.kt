package com.example.smartscheduler.presentation.me

import com.example.smartscheduler.domain.model.ThemeMode
import java.time.Duration
import java.time.LocalTime

sealed interface MeAction {
    data class ChangeThemeMode(val themeMode: ThemeMode) : MeAction
    data class ChangeDynamicColor(val enabled: Boolean) : MeAction
    data class ChangeWorkDayStart(val time: LocalTime) : MeAction
    data class ChangeWorkDayEnd(val time: LocalTime) : MeAction
    data class ChangeDefaultTaskDuration(val duration: Duration) : MeAction
    data class ChangeDefaultEventDuration(val duration: Duration) : MeAction
    data class ChangeWeekStartsOnMonday(val enabled: Boolean) : MeAction
    data object ResetDefaults : MeAction
}
