package com.example.smartscheduler.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.smartscheduler.R
import com.example.smartscheduler.presentation.calendar.CalendarRoute
import com.example.smartscheduler.presentation.me.MeRoute
import com.example.smartscheduler.presentation.scheduleitemdetail.ScheduleItemDetailArgs
import com.example.smartscheduler.presentation.scheduleitemdetail.ScheduleItemDetailMode
import com.example.smartscheduler.presentation.scheduleitemdetail.ScheduleItemDetailRoute
import com.example.smartscheduler.presentation.scheduleitemdetail.ScheduleItemKind
import com.example.smartscheduler.presentation.smartreschedule.SmartRescheduleAction
import com.example.smartscheduler.presentation.smartreschedule.SmartRescheduleRoute
import com.example.smartscheduler.presentation.smartreschedule.SmartRescheduleViewModel
import com.example.smartscheduler.presentation.today.TodayRoute
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.serialization.serializer

@Composable
fun SmartSchedulerApplication() {
    val topLevelBackStacks = rememberTopLevelBackStacks()
    var selectedTopLevelRoute by remember { mutableStateOf<Route>(Route.Today) }
    val activeBackStack = topLevelBackStacks.getValue(selectedTopLevelRoute)
    val currentRoute = activeBackStack.lastOrNull()
    val showBottomBar = currentRoute !is Route.SmartRescheduleDiff && currentRoute !is Route.ScheduleItemDetail
    val smartRescheduleViewModel: SmartRescheduleViewModel = hiltViewModel()
    val entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator<Route>(),
        rememberViewModelStoreNavEntryDecorator<Route>(),
    )

    fun navigateToTopLevel(route: Route) {
        selectedTopLevelRoute = route
    }

    fun addToActiveBackStack(route: Route) {
        topLevelBackStacks.getValue(selectedTopLevelRoute).add(route)
    }

    fun navigateBack() {
        val backStack = topLevelBackStacks.getValue(selectedTopLevelRoute)
        if (backStack.lastOrNull() is Route.SmartRescheduleDiff) {
            smartRescheduleViewModel.handleAction(SmartRescheduleAction.ResetSession)
        }

        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        } else if (selectedTopLevelRoute != Route.Today) {
            selectedTopLevelRoute = Route.Today
        }
    }

    val entryProvider: (Route) -> NavEntry<Route> = { key ->
        when (key) {
            is Route.Calendar -> NavEntry(key) {
                CalendarRoute(
                    viewModel = hiltViewModel(),
                    onNavigateToScheduleItemCreate = { kind, draftTitle, draftDescription, timeSlot ->
                        addToActiveBackStack(
                            Route.ScheduleItemDetail(
                                mode = ScheduleItemDetailMode.CREATE,
                                kind = kind,
                                draftTitle = draftTitle,
                                draftDescription = draftDescription,
                                draftStartTimeMillis = timeSlot?.startTime?.toUtcMillis(),
                                draftEndTimeMillis = timeSlot?.endTime?.toUtcMillis(),
                            )
                        )
                    },
                    onNavigateToScheduleItemEdit = { kind, itemId ->
                        addToActiveBackStack(
                            Route.ScheduleItemDetail(
                                mode = ScheduleItemDetailMode.EDIT,
                                kind = kind,
                                itemId = itemId,
                            )
                        )
                    },
                )
            }

            is Route.Me -> NavEntry(key) { MeRoute(viewModel = hiltViewModel()) }
            is Route.Today -> NavEntry(key) {
                TodayRoute(
                    viewModel = hiltViewModel(),
                    smartRescheduleViewModel = smartRescheduleViewModel,
                    onNavigateToScheduleItemCreate = { kind, draftTitle, draftDescription, timeSlot ->
                        addToActiveBackStack(
                            Route.ScheduleItemDetail(
                                mode = ScheduleItemDetailMode.CREATE,
                                kind = kind,
                                draftTitle = draftTitle,
                                draftDescription = draftDescription,
                                draftStartTimeMillis = timeSlot?.startTime?.toUtcMillis(),
                                draftEndTimeMillis = timeSlot?.endTime?.toUtcMillis(),
                            )
                        )
                    },
                    onNavigateToScheduleItemEdit = { kind, itemId ->
                        addToActiveBackStack(
                            Route.ScheduleItemDetail(
                                mode = ScheduleItemDetailMode.EDIT,
                                kind = kind,
                                itemId = itemId,
                            )
                        )
                    },
                    onNavigateToSmartRescheduleDiff = {
                        addToActiveBackStack(Route.SmartRescheduleDiff)
                    }
                )
            }

            is Route.SmartRescheduleDiff -> NavEntry(key) {
                SmartRescheduleRoute(
                    viewModel = smartRescheduleViewModel,
                    onNavigateBack = { navigateBack() },
                )
            }

            is Route.ScheduleItemDetail -> NavEntry(key) {
                ScheduleItemDetailRoute(
                    viewModel = hiltViewModel(),
                    args = key.toArgs(),
                    onNavigateBack = { navigateBack() },
                )
            }
        }
    }

    val todayEntries = rememberDecoratedNavEntries(
        backStack = topLevelBackStacks.getValue(Route.Today),
        entryDecorators = entryDecorators,
        entryProvider = entryProvider,
    )
    val calendarEntries = rememberDecoratedNavEntries(
        backStack = topLevelBackStacks.getValue(Route.Calendar),
        entryDecorators = entryDecorators,
        entryProvider = entryProvider,
    )
    val meEntries = rememberDecoratedNavEntries(
        backStack = topLevelBackStacks.getValue(Route.Me),
        entryDecorators = entryDecorators,
        entryProvider = entryProvider,
    )
    val entries = when (selectedTopLevelRoute) {
        Route.Today -> todayEntries
        Route.Calendar -> todayEntries + calendarEntries
        Route.Me -> todayEntries + meEntries
        else -> todayEntries
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute is Route.Today,
                        onClick = {
                            if (currentRoute !is Route.Today) {
                                navigateToTopLevel(Route.Today)
                            }
                        },
                        icon = {
                            Icon(
                                painterResource(R.drawable.ic_app_bottom_nav_today),
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(stringResource(R.string.nav_today))
                        }
                    )

                    NavigationBarItem(
                        selected = currentRoute is Route.Calendar,
                        onClick = {
                            if (currentRoute !is Route.Calendar) {
                                navigateToTopLevel(Route.Calendar)
                            }
                        },
                        icon = {
                            Icon(
                                painterResource(R.drawable.ic_app_bottom_nav_calendar),
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(stringResource(R.string.nav_calendar))
                        }
                    )

                    NavigationBarItem(
                        selected = currentRoute is Route.Me,
                        onClick = {
                            if (currentRoute !is Route.Me) {
                                navigateToTopLevel(Route.Me)
                            }
                        },
                        icon = {
                            Icon(
                                painterResource(R.drawable.ic_app_bottom_nav_me),
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(stringResource(R.string.nav_me))
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            entries = entries,
            onBack = { navigateBack() },
        )
    }


}

@Composable
private fun rememberTopLevelBackStacks(): Map<Route, NavBackStack<Route>> {
    val todayBackStack = rememberRouteNavBackStack(Route.Today)
    val calendarBackStack = rememberRouteNavBackStack(Route.Calendar)
    val meBackStack = rememberRouteNavBackStack(Route.Me)

    return remember(todayBackStack, calendarBackStack, meBackStack) {
        mapOf(
            Route.Today to todayBackStack,
            Route.Calendar to calendarBackStack,
            Route.Me to meBackStack,
        )
    }
}

@Composable
fun rememberRouteNavBackStack(vararg elements: Route): NavBackStack<Route> {
    return rememberSerializable(serializer = serializer()) {
        NavBackStack(*elements)
    }
}

private fun Route.ScheduleItemDetail.toArgs(): ScheduleItemDetailArgs {
    return ScheduleItemDetailArgs(
        mode = mode,
        kind = kind,
        itemId = itemId,
        draftTitle = draftTitle,
        draftDescription = draftDescription,
        draftStartTime = draftStartTimeMillis?.toLocalDateTimeUtc(),
        draftEndTime = draftEndTimeMillis?.toLocalDateTimeUtc(),
    )
}

private fun Long.toLocalDateTimeUtc(): LocalDateTime {
    return Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDateTime()
}

private fun LocalDateTime.toUtcMillis(): Long {
    return atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
}
