package com.example.smartscheduler.presentation.components

import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun SmartLoadingCircularIndicator(isLoading: Boolean, modifier: Modifier = Modifier) {
    if (!isLoading) return

    CircularProgressIndicator(
        modifier = modifier
            .width(64.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}