package com.example.smartscheduler.presentation.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import com.example.smartscheduler.R


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

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(visible = isExpanded) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        isExpanded = false
                        onAddEventClick()
                    }
                ) {
                    Icon(painterResource(R.drawable.ic_app_event), contentDescription = null)
                }

                SmallFloatingActionButton(
                    onClick = {
                        isExpanded = false
                        onAddTaskClick()
                    }
                ) {
                    Icon(painterResource(R.drawable.ic_app_task), contentDescription = null)
                }
            }
        }



        FloatingActionButton(
            onClick = { isExpanded = !isExpanded }
        ) {
            Icon(painterResource(R.drawable.ic_app_fab_plus),
                modifier = Modifier.graphicsLayer {
                    rotationZ = rotationAngle
                },
                contentDescription = null)
        }
    }
}