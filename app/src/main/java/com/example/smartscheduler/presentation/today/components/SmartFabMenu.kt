package com.example.smartscheduler.presentation.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartscheduler.R
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme

private object FabMenuSpacing {
    val Gap = 16.dp
}

@Composable
fun SmartFabMenu(
    onAddTaskClick: () -> Unit,
    onAddEventClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val targetRotation = if (isExpanded) 45f else 0f

    val rotationAngle by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 300),
        label = "fab_rotation"
    )

    SmartFabMenuContent(
        isExpanded = isExpanded,
        rotationAngle = rotationAngle,
        onToggleExpanded = { isExpanded = !isExpanded },
        onAddTaskClick = {
            isExpanded = false
            onAddTaskClick()
        },
        onAddEventClick = {
            isExpanded = false
            onAddEventClick()
        },
        modifier = modifier
    )
}

@Composable
private fun SmartFabMenuContent(
    isExpanded: Boolean,
    rotationAngle: Float,
    onToggleExpanded: () -> Unit,
    onAddTaskClick: () -> Unit,
    onAddEventClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(FabMenuSpacing.Gap)
    ) {
        AnimatedVisibility(visible = isExpanded) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(FabMenuSpacing.Gap)
            ) {
                SmallFloatingActionButton(
                    onClick = onAddEventClick,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(
                        painterResource(R.drawable.ic_app_event),
                        contentDescription = stringResource(R.string.fab_add_event_content_description)
                    )
                }

                SmallFloatingActionButton(
                    onClick = onAddTaskClick,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(
                        painterResource(R.drawable.ic_app_task),
                        contentDescription = stringResource(R.string.fab_add_task_content_description)
                    )
                }
            }
        }



        FloatingActionButton(
            onClick = onToggleExpanded,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                painterResource(R.drawable.ic_app_fab_plus),
                modifier = Modifier.graphicsLayer {
                    rotationZ = rotationAngle
                },
                contentDescription = if (isExpanded) {
                    stringResource(R.string.fab_collapse_content_description)
                } else {
                    stringResource(R.string.fab_expand_content_description)
                }
            )
        }
    }
}

@Preview(apiLevel = 35, showBackground = true)
@Composable
private fun SmartFabMenuPreview() {
    SmartSchedulerTheme(dynamicColor = false) {
        SmartFabMenu(
            onAddTaskClick = {},
            onAddEventClick = {}
        )
    }
}

@Preview(name = "Expanded FAB menu", apiLevel = 35, showBackground = true)
@Composable
private fun SmartFabMenuExpandedPreview() {
    SmartSchedulerTheme(dynamicColor = false) {
        SmartFabMenuContent(
            isExpanded = true,
            rotationAngle = 45f,
            onToggleExpanded = {},
            onAddTaskClick = {},
            onAddEventClick = {}
        )
    }
}
