package com.example.smartscheduler.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.smartscheduler.R
import com.example.smartscheduler.presentation.smartreschedule.SmartRescheduleAction
import com.example.smartscheduler.presentation.smartreschedule.SmartRescheduleRoute
import com.example.smartscheduler.presentation.smartreschedule.SmartRescheduleViewModel
import com.example.smartscheduler.presentation.today.TodayRoute
import kotlinx.serialization.serializer

@Composable
fun SmartSchedulerApplication() {
    val backStack = rememberRouteNavBackStack(Route.Calendar)
    val currentRoute = backStack.lastOrNull()
    val showBottomBar = currentRoute !is Route.SmartRescheduleDiff
    val smartRescheduleViewModel: SmartRescheduleViewModel = hiltViewModel()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute is Route.Today,
                        onClick = {
                            if (currentRoute !is Route.Today) {
                                backStack.clear()
                                backStack.add(Route.Today)
                            }
                        },
                        icon = {
                            Icon(
                                painterResource(R.drawable.ic_app_bottom_nav_today),
                                contentDescription = null
                            )
                        },
                        label = {
                            Text("Today")
                        }
                    )

                    NavigationBarItem(
                        selected = currentRoute is Route.Calendar,
                        onClick = {
                            if (currentRoute !is Route.Calendar) {
                                backStack.clear()
                                backStack.add(Route.Calendar)
                            }
                        },
                        icon = {
                            Icon(
                                painterResource(R.drawable.ic_app_bottom_nav_calendar),
                                contentDescription = null
                            )
                        },
                        label = {
                            Text("Calendar")
                        }
                    )

                    NavigationBarItem(
                        selected = currentRoute is Route.Me,
                        onClick = {
                            if (currentRoute !is Route.Me) {
                                backStack.clear()
                                backStack.add(Route.Me)
                            }
                        },
                        icon = {
                            Icon(
                                painterResource(R.drawable.ic_app_bottom_nav_me),
                                contentDescription = null
                            )
                        },
                        label = {
                            Text("Me")
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            backStack = backStack,
            onBack = {
                if (backStack.lastOrNull() is Route.SmartRescheduleDiff) {
                    smartRescheduleViewModel.handleAction(SmartRescheduleAction.ResetSession)
                }
                backStack.removeLastOrNull()
            },
            entryProvider = { key ->
                when (key) {
                    is Route.Calendar -> NavEntry(key) {
                        Button(onClick = { backStack.add(Route.Me) }) { Text("Calendar go to ME") }
                    }

                    is Route.Me -> NavEntry(key) { Text("ME") }
                    is Route.Today -> NavEntry(key) {
                        TodayRoute(
                            viewModel = hiltViewModel(),
                            smartRescheduleViewModel = smartRescheduleViewModel,
                            onNavigateToTaskDetail = { _, _ -> },
                            onNavigateToSmartRescheduleDiff = {
                                backStack.add(Route.SmartRescheduleDiff)
                            }
                        )
                    }

                    is Route.SmartRescheduleDiff -> NavEntry(key) {
                        SmartRescheduleRoute(
                            viewModel = smartRescheduleViewModel,
                            onNavigateBack = { backStack.removeLastOrNull() },
                        )
                    }

                }
            }
        )
    }


}

@Composable
fun rememberRouteNavBackStack(vararg elements: Route): NavBackStack<Route> {
    return rememberSerializable(serializer = serializer()) {
        NavBackStack(*elements)
    }
}
