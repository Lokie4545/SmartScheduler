package com.example.smartscheduler.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.example.smartscheduler.presentation.scheduleitemdetail.ScheduleItemDetailMode
import com.example.smartscheduler.presentation.scheduleitemdetail.ScheduleItemKind
import kotlinx.serialization.Serializable

@Serializable
sealed class Route : NavKey {
    @Serializable
    data object Today: Route()

    @Serializable
    data object Calendar: Route()

    @Serializable
    data object Me: Route()

    @Serializable
    data object SmartRescheduleDiff: Route()

    @Serializable
    data class ScheduleItemDetail(
        val mode: ScheduleItemDetailMode,
        val kind: ScheduleItemKind,
        val itemId: String? = null,
        val draftTitle: String = "",
        val draftDescription: String = "",
        val draftStartTimeMillis: Long? = null,
        val draftEndTimeMillis: Long? = null,
    ) : Route()
}
