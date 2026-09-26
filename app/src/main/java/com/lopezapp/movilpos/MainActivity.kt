package com.lopezapp.movilpos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.InventoryScreen
import com.lopezapp.movilpos.ui.POSScreen
import com.lopezapp.movilpos.ui.SettingsScreen
import com.lopezapp.movilpos.ui.navigation.HomeRoute
import com.lopezapp.movilpos.ui.navigation.InventoryRoute
import com.lopezapp.movilpos.ui.navigation.POSRoute
import com.lopezapp.movilpos.ui.navigation.SettingsRoute
import com.lopezapp.movilpos.ui.navigation.buildNavTransition
import com.lopezapp.movilpos.ui.theme.MovilPOSTheme
import com.lopezapp.movilpos.ui.viewmodel.InventoryViewModel
import com.lopezapp.movilpos.ui.viewmodel.POSViewModel
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    private val appRepository = AppRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovilPOSTheme {
                val settingsViewModel: SettingsViewModel = viewModel()
                val settingsState by settingsViewModel.uiState.collectAsState()
                val backStack = rememberNavBackStack(HomeRoute)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        modifier = Modifier.padding(innerPadding),
                        transitionSpec = {
                            buildNavTransition(
                                animationType = settingsState.animationType,
                                durationMs = settingsState.animationDurationMs,
                                isPop = false
                            )
                        },
                        popTransitionSpec = {
                            buildNavTransition(
                                animationType = settingsState.animationType,
                                durationMs = settingsState.animationDurationMs,
                                isPop = true
                            )
                        },
                        entryProvider = { key ->
                            when (key) {
                                is HomeRoute -> NavEntry(key) {
                                    HomeScreen(
                                        onNavigateToInventory = { backStack.add(InventoryRoute) },
                                        onNavigateToPOS = { backStack.add(POSRoute) },
                                        onNavigateToSettings = { backStack.add(SettingsRoute) }
                                    )
                                }
                                is InventoryRoute -> NavEntry(key) {
                                    val inventoryViewModel: InventoryViewModel = viewModel(
                                        factory = InventoryViewModel.Factory(appRepository)
                                    )
                                    InventoryScreen(
                                        viewModel = inventoryViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is POSRoute -> NavEntry(key) {
                                    val posViewModel: POSViewModel = viewModel(
                                        factory = POSViewModel.Factory(appRepository)
                                    )
                                    POSScreen(viewModel = posViewModel)
                                }
                                is SettingsRoute -> NavEntry(key) {
                                    SettingsScreen(
                                        viewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                else -> error("Unknown route: $key")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToInventory: () -> Unit,
    onNavigateToPOS: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            MenuCard(
                title = "Inventory",
                icon = Icons.Rounded.Inventory,
                onClick = onNavigateToInventory
            )
        }
        item {
            MenuCard(
                title = "POS",
                icon = Icons.Rounded.PointOfSale,
                onClick = onNavigateToPOS
            )
        }
        item {
            MenuCard(
                title = "Configuración",
                icon = Icons.Rounded.Settings,
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
