package com.example.smartscheduler.presentation.me

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartscheduler.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiState: StateFlow<MeUiState> = settingsRepository.settingsStream
        .map { settings -> MeUiState.Success(settings) as MeUiState }
        .catch { error -> emit(MeUiState.Error(error.message ?: "Unable to load settings")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MeUiState.Loading,
        )

    fun handleAction(action: MeAction) {
        viewModelScope.launch {
            when (action) {
                is MeAction.ChangeThemeMode -> settingsRepository.setThemeMode(action.themeMode)
                is MeAction.ChangeDynamicColor -> settingsRepository.setDynamicColor(action.enabled)
                is MeAction.ChangeWorkDayStart -> settingsRepository.setWorkDayStart(action.time)
                is MeAction.ChangeWorkDayEnd -> settingsRepository.setWorkDayEnd(action.time)
                is MeAction.ChangeDefaultTaskDuration -> settingsRepository.setDefaultTaskDuration(action.duration)
                is MeAction.ChangeDefaultEventDuration -> settingsRepository.setDefaultEventDuration(action.duration)
                is MeAction.ChangeWeekStartsOnMonday -> settingsRepository.setWeekStartsOnMonday(action.enabled)
                MeAction.ResetDefaults -> settingsRepository.reset()
            }
        }
    }
}
