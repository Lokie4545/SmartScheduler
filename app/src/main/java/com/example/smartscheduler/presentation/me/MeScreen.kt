@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.smartscheduler.presentation.me

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smartscheduler.R
import com.example.smartscheduler.domain.model.AppSettings
import com.example.smartscheduler.domain.model.ThemeMode
import com.example.smartscheduler.presentation.components.SmartLoadingCircularIndicator
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private object MeSpacing {
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val ExtraLarge = 24.dp
    val ContentBottom = 32.dp
}

private object MeDimensions {
    val ContentMaxWidth = 640.dp
    val HeaderIcon = 48.dp
    val SettingIcon = 28.dp
}

private enum class WorkdayTimeTarget {
    START,
    END,
}

private val SettingsTimeFormatter = DateTimeFormatter.ofPattern("H:mm")

@Composable
fun MeRoute(
    viewModel: MeViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MeScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
    )
}

@Composable
fun MeScreen(
    uiState: MeUiState,
    onAction: (MeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var timePickerTarget by remember { mutableStateOf<WorkdayTimeTarget?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Top
        ),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Me",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (uiState) {
                MeUiState.Loading -> SmartLoadingCircularIndicator(isLoading = true)
                is MeUiState.Error -> MeErrorContent(message = uiState.message)
                is MeUiState.Success -> MeSuccessContent(
                    uiState = uiState,
                    onAction = onAction,
                    onPickWorkdayStart = { timePickerTarget = WorkdayTimeTarget.START },
                    onPickWorkdayEnd = { timePickerTarget = WorkdayTimeTarget.END },
                )
            }
        }
    }

    val successState = uiState as? MeUiState.Success
    val target = timePickerTarget
    if (successState != null && target != null) {
        val initialTime = when (target) {
            WorkdayTimeTarget.START -> successState.settings.workDayStart
            WorkdayTimeTarget.END -> successState.settings.workDayEnd
        }

        SettingsTimePickerDialog(
            title = if (target == WorkdayTimeTarget.START) "Workday start" else "Workday end",
            initialTime = initialTime,
            onConfirm = { selectedTime ->
                when (target) {
                    WorkdayTimeTarget.START -> onAction(MeAction.ChangeWorkDayStart(selectedTime))
                    WorkdayTimeTarget.END -> onAction(MeAction.ChangeWorkDayEnd(selectedTime))
                }
                timePickerTarget = null
            },
            onDismiss = { timePickerTarget = null },
        )
    }
}

@Composable
private fun MeSuccessContent(
    uiState: MeUiState.Success,
    onAction: (MeAction) -> Unit,
    onPickWorkdayStart: () -> Unit,
    onPickWorkdayEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = MeDimensions.ContentMaxWidth),
        contentPadding = PaddingValues(
            start = MeSpacing.Large,
            end = MeSpacing.Large,
            bottom = MeSpacing.ContentBottom,
        ),
        verticalArrangement = Arrangement.spacedBy(MeSpacing.Medium),
    ) {
        item(key = "header") {
            MeHeaderCard(settings = uiState.settings)
        }

        item(key = "appearance") {
            SettingsCard(title = "Appearance") {
                ThemeModeSelector(
                    selectedThemeMode = uiState.settings.themeMode,
                    onThemeModeSelected = { onAction(MeAction.ChangeThemeMode(it)) },
                )
                SettingsSwitchRow(
                    iconResId = R.drawable.ic_app_sun,
                    title = "Dynamic color",
                    supportingText = "Use Android 12+ wallpaper colors when available",
                    checked = uiState.settings.dynamicColor,
                    onCheckedChange = { onAction(MeAction.ChangeDynamicColor(it)) },
                )
            }
        }

        item(key = "planning") {
            SettingsCard(title = "Planning") {
                SettingsActionRow(
                    iconResId = R.drawable.ic_app_chip_clock,
                    title = "Workday start",
                    supportingText = "Smart Reschedule starts placing work here",
                    value = uiState.settings.workDayStart.format(SettingsTimeFormatter),
                    onClick = onPickWorkdayStart,
                )
                SettingsActionRow(
                    iconResId = R.drawable.ic_app_chip_clock,
                    title = "Workday end",
                    supportingText = "Tasks after this time stay out of auto-planning",
                    value = uiState.settings.workDayEnd.format(SettingsTimeFormatter),
                    onClick = onPickWorkdayEnd,
                )
                WorkdaySummary(settings = uiState.settings)
            }
        }

        item(key = "quick_add") {
            SettingsCard(title = "Quick add defaults") {
                DurationChipsRow(
                    title = "Task duration",
                    selectedDuration = uiState.settings.defaultTaskDuration,
                    options = uiState.taskDurationOptions,
                    onDurationSelected = { onAction(MeAction.ChangeDefaultTaskDuration(it)) },
                )
                DurationChipsRow(
                    title = "Event duration",
                    selectedDuration = uiState.settings.defaultEventDuration,
                    options = uiState.eventDurationOptions,
                    onDurationSelected = { onAction(MeAction.ChangeDefaultEventDuration(it)) },
                )
            }
        }

        item(key = "calendar") {
            SettingsCard(title = "Calendar") {
                SettingsSwitchRow(
                    iconResId = R.drawable.ic_app_bottom_nav_calendar,
                    title = "Week starts on Monday",
                    supportingText = "Calendar grid follows your planning week",
                    checked = uiState.settings.weekStartsOnMonday,
                    onCheckedChange = { onAction(MeAction.ChangeWeekStartsOnMonday(it)) },
                )
            }
        }

        item(key = "mvp_status") {
            SettingsCard(title = "MVP status") {
                ReadOnlyInfoRow(
                    iconResId = R.drawable.ic_app_reschedule_magic,
                    title = "Repository source",
                    value = "Fake repositories active",
                    supportingText = "Room is wired as @RealRepository but not default yet",
                )
                ReadOnlyInfoRow(
                    iconResId = R.drawable.ic_app_smile,
                    title = "Build track",
                    value = "Pre-prod cleanup",
                    supportingText = "Settings are persisted with DataStore",
                )
                OutlinedButton(
                    onClick = { onAction(MeAction.ResetDefaults) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MeSpacing.Large, vertical = MeSpacing.Small),
                ) {
                    Text("Reset settings")
                }
            }
        }
    }
}

@Composable
private fun MeHeaderCard(settings: AppSettings) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = MeSpacing.Small),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MeSpacing.Large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MeSpacing.Large),
        ) {
            Surface(
                modifier = Modifier.size(MeDimensions.HeaderIcon),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_app_bottom_nav_me),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(MeSpacing.Small)) {
                Text(
                    text = "Smart Scheduler",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = if (settings.isValidWorkday) {
                        "Planning ${settings.workDayStart.format(SettingsTimeFormatter)}-${settings.workDayEnd.format(SettingsTimeFormatter)}"
                    } else {
                        "Workday needs attention"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(MeSpacing.Small),
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(
                    start = MeSpacing.Large,
                    top = MeSpacing.Large,
                    end = MeSpacing.Large,
                ),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            content()
        }
    }
}

@Composable
private fun ThemeModeSelector(
    selectedThemeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MeSpacing.Large, vertical = MeSpacing.Small),
    ) {
        ThemeMode.entries.forEachIndexed { index, themeMode ->
            SegmentedButton(
                selected = selectedThemeMode == themeMode,
                onClick = { onThemeModeSelected(themeMode) },
                shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                label = {
                    Text(
                        text = themeMode.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    iconResId: Int,
    title: String,
    supportingText: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        leadingContent = {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = null,
                modifier = Modifier.size(MeDimensions.SettingIcon),
            )
        },
        headlineContent = { Text(title) },
        supportingContent = { Text(supportingText) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
    )
}

@Composable
private fun SettingsActionRow(
    iconResId: Int,
    title: String,
    supportingText: String,
    value: String,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        leadingContent = {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = null,
                modifier = Modifier.size(MeDimensions.SettingIcon),
            )
        },
        headlineContent = { Text(title) },
        supportingContent = { Text(supportingText) },
        trailingContent = {
            TextButton(onClick = onClick) {
                Text(value)
            }
        },
    )
}

@Composable
private fun WorkdaySummary(settings: AppSettings) {
    val text = if (settings.isValidWorkday) {
        "${formatDuration(settings.workdayDuration)} available for Smart Reschedule"
    } else {
        "Start must be before end"
    }
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = MeSpacing.Large, vertical = MeSpacing.Small),
        style = MaterialTheme.typography.bodyMedium,
        color = if (settings.isValidWorkday) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.error
        },
    )
}

@Composable
private fun DurationChipsRow(
    title: String,
    selectedDuration: Duration,
    options: List<Duration>,
    onDurationSelected: (Duration) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MeSpacing.Large, vertical = MeSpacing.Small),
        verticalArrangement = Arrangement.spacedBy(MeSpacing.Small),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(MeSpacing.Small),
        ) {
            options.forEach { duration ->
                FilterChip(
                    selected = selectedDuration == duration,
                    onClick = { onDurationSelected(duration) },
                    label = { Text(formatDuration(duration)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ReadOnlyInfoRow(
    iconResId: Int,
    title: String,
    value: String,
    supportingText: String,
) {
    ListItem(
        leadingContent = {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = null,
                modifier = Modifier.size(MeDimensions.SettingIcon),
            )
        },
        headlineContent = { Text(title) },
        supportingContent = { Text(supportingText) },
        trailingContent = {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

@Composable
private fun SettingsTimePickerDialog(
    title: String,
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true,
    )

    TimePickerDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    ) {
        if (LocalConfiguration.current.screenHeightDp > 480) {
            TimePicker(state = state)
        } else {
            TimeInput(state = state)
        }
    }
}

@Composable
private fun MeErrorContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(MeSpacing.ExtraLarge),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

private val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.SYSTEM -> "System"
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
    }

private fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes.coerceAtLeast(0)}m"
    }
}

@Preview(name = "Me settings", widthDp = 412, heightDp = 892, apiLevel = 35, showBackground = true)
@Composable
private fun MeScreenPreview() {
    SmartSchedulerTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            MeScreen(
                uiState = MeUiState.Success(settings = AppSettings()),
                onAction = {},
            )
        }
    }
}

@Preview(name = "Me invalid workday", widthDp = 412, heightDp = 892, apiLevel = 35, showBackground = true)
@Composable
private fun MeScreenInvalidWorkdayPreview() {
    SmartSchedulerTheme(dynamicColor = false) {
        MeScreen(
            uiState = MeUiState.Success(
                settings = AppSettings(
                    workDayStart = LocalTime.of(18, 0),
                    workDayEnd = LocalTime.of(9, 0),
                )
            ),
            onAction = {},
        )
    }
}
