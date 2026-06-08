package com.example.smartscheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.example.smartscheduler.domain.model.ThemeMode
import com.example.smartscheduler.presentation.me.AppSettingsViewModel
import com.example.smartscheduler.presentation.navigation.SmartSchedulerApplication
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartSchedulerRoot()
        }
    }
}

@Composable
private fun SmartSchedulerRoot(
    viewModel: AppSettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val systemDarkTheme = isSystemInDarkTheme()

    SmartSchedulerTheme(
        darkTheme = settings.themeMode.resolveDarkTheme(systemDarkTheme),
        dynamicColor = settings.dynamicColor,
    ) {
        SmartSchedulerApplication()
    }
}

private fun ThemeMode.resolveDarkTheme(systemDarkTheme: Boolean): Boolean {
    return when (this) {
        ThemeMode.SYSTEM -> systemDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}
