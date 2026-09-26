package com.lopezapp.movilpos.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Serializable
data object POSRoute : NavKey

@Serializable
data object InventoryRoute : NavKey

@Serializable
data object SettingsRoute : NavKey
