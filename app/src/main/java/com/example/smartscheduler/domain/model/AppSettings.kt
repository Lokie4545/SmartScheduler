package com.example.smartscheduler.domain.model

import java.time.Duration
import java.time.LocalTime

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val workDayStart: LocalTime = LocalTime.of(9, 0),
    val workDayEnd: LocalTime = LocalTime.of(18, 0),
    val defaultTaskDuration: Duration = Duration.ofMinutes(30),
    val defaultEventDuration: Duration = Duration.ofMinutes(60),
    val weekStartsOnMonday: Boolean = true,
) {
    val isValidWorkday: Boolean = workDayStart.isBefore(workDayEnd)
    val workdayDuration: Duration = if (isValidWorkday) {
        Duration.between(workDayStart, workDayEnd)
    } else {
        Duration.ZERO
    }
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}
