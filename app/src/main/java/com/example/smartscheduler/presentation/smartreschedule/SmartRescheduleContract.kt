package com.example.smartscheduler.presentation.smartreschedule

sealed interface SmartRescheduleAction {
    data object Retry : SmartRescheduleAction
    data object ApplySchedule : SmartRescheduleAction
    data object Cancel : SmartRescheduleAction
    data object ResetSession : SmartRescheduleAction
    data class ToggleBacklogCandidate(val taskId: String, val selected: Boolean) : SmartRescheduleAction
    data class RejectChange(val taskId: String) : SmartRescheduleAction
    data class RestoreChange(val taskId: String) : SmartRescheduleAction
}

sealed interface SmartRescheduleEffect {
    data object NavigateBack : SmartRescheduleEffect
}
