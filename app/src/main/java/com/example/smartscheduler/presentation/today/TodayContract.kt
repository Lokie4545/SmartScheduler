package com.example.smartscheduler.presentation.today

import com.example.smartscheduler.domain.model.TimeSlot

sealed interface TodayAction {
    data class MarkTaskCompleted(val taskId: String) : TodayAction
    data class AddQuickTask(val title: String, val description: String) : TodayAction
    data class AddQuickEvent(val title: String, val description: String, val timeSlot: TimeSlot) : TodayAction
    data object RequestSmartReschedule : TodayAction
    data object DismissFastAddRequest: TodayAction

    data class NavigateToTaskDetail(val draftTitle: String, val draftDescription: String) : TodayAction
}
