package com.example.smartscheduler.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Route : NavKey {
    @Serializable
    data object Today: Route()

    @Serializable
    data object Calendar: Route()

    @Serializable
    data object Me: Route()
}
