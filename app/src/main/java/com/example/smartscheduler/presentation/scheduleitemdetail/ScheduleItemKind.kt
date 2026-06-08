package com.example.smartscheduler.presentation.scheduleitemdetail

import kotlinx.serialization.Serializable

@Serializable
enum class ScheduleItemKind {
    TASK,
    EVENT,
}

@Serializable
enum class ScheduleItemDetailMode {
    CREATE,
    EDIT,
}
