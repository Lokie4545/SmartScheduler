package com.example.smartscheduler.presentation.today

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smartscheduler.R
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.TimeSlot
import com.example.smartscheduler.presentation.components.SmartLoadingCircularIndicator
import com.example.smartscheduler.presentation.components.SmartPriorityChip
import com.example.smartscheduler.presentation.components.SmartTaskCard
import com.example.smartscheduler.presentation.today.components.SmartFabMenu

@Composable
fun TodayRoute(viewModel: TodayViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TodayScreen(uiState = uiState, onTaskAdd = {}, onTaskClick = {})

}

sealed interface TodayBottomSheetConfig {
    data object None : TodayBottomSheetConfig
    data object AddTask : TodayBottomSheetConfig
    data class AddEvent(val defaultTimeSlot: TimeSlot) : TodayBottomSheetConfig
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    uiState: TodayUiState,
    onTaskClick: (String) -> Unit,
    onTaskAdd: () -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    var bottomSheetConfig by remember {
        mutableStateOf<TodayBottomSheetConfig>(TodayBottomSheetConfig.None)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    val title = uiState.currentDateTime.toLocalDate().toString()
                    Text(title,
                        modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                        style = MaterialTheme.typography.headlineMedium)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            SmartFabMenu(onAddTaskClick = {
                bottomSheetConfig = TodayBottomSheetConfig.AddTask
                showBottomSheet = true
            }, onAddEventClick = {
                bottomSheetConfig = TodayBottomSheetConfig.AddEvent(

                )
                showBottomSheet = true
            })
        }
    ) { innerPadding ->
        when (uiState) {
            is TodayUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SmartLoadingCircularIndicator(true)
                }
            }

            is TodayUiState.Error -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Something wrong!", style = MaterialTheme.typography.labelLarge)
                    Log.d("TodayScreen", uiState.message)
                }
            }

            is TodayUiState.Success -> {
                TodaySuccess(tasks = uiState.tasks, ) {}
            }
        }

        if (showBottomSheet) {

        }
    }
}


@Composable
private fun TodaySuccess(
    tasks: List<ScheduledTask>,
    modifier: Modifier = Modifier,
    onTaskClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(tasks, key = { it.id }) { task ->
            SmartTaskCard(
                modifier = Modifier.fillMaxWidth(),
                leadingContent = {
                    Checkbox(
                        checked = (task.status == Status.COMPLETED),
                        onCheckedChange = {
                            onTaskClick(task.id)
                        }
                    )
                },
                trailingContent = { SmartPriorityChip(task.priority) }
            ) {
                Text(task.name, style = MaterialTheme.typography.titleSmallEmphasized)
                Text(
                    "${task.startTime} - ${task.endTime}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}