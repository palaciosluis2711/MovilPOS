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
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.CustomerEditForm
import com.lopezapp.movilpos.ui.CustomerReadOnlyView
import com.lopezapp.movilpos.ui.CustomerScreen
import com.lopezapp.movilpos.ui.InventoryScreen
import com.lopezapp.movilpos.ui.POSCheckoutScreen
import com.lopezapp.movilpos.ui.POSScreen
import com.lopezapp.movilpos.ui.POSTicketReceiptScreen
import com.lopezapp.movilpos.ui.PriceRuleEditScreen
import com.lopezapp.movilpos.ui.PurchaseEditForm
import com.lopezapp.movilpos.ui.PurchaseReadOnlyView
import com.lopezapp.movilpos.ui.PurchaseScreen
import com.lopezapp.movilpos.ui.SettingsAnimationScreen
import com.lopezapp.movilpos.ui.SettingsBrandsScreen
import com.lopezapp.movilpos.ui.SettingsBusinessInfoScreen
import com.lopezapp.movilpos.ui.SettingsCategoriesScreen
import com.lopezapp.movilpos.ui.SettingsCurrencyScreen
import com.lopezapp.movilpos.ui.SettingsPaymentMethodsScreen
import com.lopezapp.movilpos.ui.SettingsPriceRulesScreen
import com.lopezapp.movilpos.ui.SettingsScreen
import com.lopezapp.movilpos.ui.SettingsTaxesScreen
import com.lopezapp.movilpos.ui.SettingsTicketScreen
import com.lopezapp.movilpos.ui.SettingsUnitsScreen
import com.lopezapp.movilpos.ui.SupplierEditForm
import com.lopezapp.movilpos.ui.SupplierReadOnlyView
import com.lopezapp.movilpos.ui.SupplierScreen
import com.lopezapp.movilpos.ui.navigation.AppNavDisplay
import com.lopezapp.movilpos.ui.navigation.CustomerDetailRoute
import com.lopezapp.movilpos.ui.navigation.CustomerEditRoute
import com.lopezapp.movilpos.ui.navigation.CustomersRoute
import com.lopezapp.movilpos.ui.navigation.HomeRoute
import com.lopezapp.movilpos.ui.navigation.InventoryRoute
import com.lopezapp.movilpos.ui.navigation.POSCheckoutKey
import com.lopezapp.movilpos.ui.navigation.POSRoute
import com.lopezapp.movilpos.ui.navigation.POSTicketReceiptKey
import com.lopezapp.movilpos.ui.navigation.PriceRuleEditKey
import com.lopezapp.movilpos.ui.navigation.PurchaseDetailRoute
import com.lopezapp.movilpos.ui.navigation.PurchaseEditRoute
import com.lopezapp.movilpos.ui.navigation.PurchasesRoute
import com.lopezapp.movilpos.ui.navigation.SettingsAnimationKey
import com.lopezapp.movilpos.ui.navigation.SettingsBrandsKey
import com.lopezapp.movilpos.ui.navigation.SettingsBusinessInfoKey
import com.lopezapp.movilpos.ui.navigation.SettingsCategoriesKey
import com.lopezapp.movilpos.ui.navigation.SettingsCurrencyKey
import com.lopezapp.movilpos.ui.navigation.SettingsPaymentMethodsKey
import com.lopezapp.movilpos.ui.navigation.SettingsPriceRulesKey
import com.lopezapp.movilpos.ui.navigation.SettingsRoute
import com.lopezapp.movilpos.ui.navigation.SettingsTaxesKey
import com.lopezapp.movilpos.ui.navigation.SettingsTicketKey
import com.lopezapp.movilpos.ui.navigation.SettingsUnitsKey
import com.lopezapp.movilpos.ui.navigation.SupplierDetailRoute
import com.lopezapp.movilpos.ui.navigation.SupplierEditRoute
import com.lopezapp.movilpos.ui.navigation.SuppliersRoute
import com.lopezapp.movilpos.ui.theme.MovilPOSTheme
import com.lopezapp.movilpos.ui.viewmodel.CustomerViewModel
import com.lopezapp.movilpos.ui.viewmodel.InventoryViewModel
import com.lopezapp.movilpos.ui.viewmodel.POSViewModel
import com.lopezapp.movilpos.ui.viewmodel.PurchaseViewModel
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import com.lopezapp.movilpos.ui.viewmodel.SupplierViewModel

class MainActivity : ComponentActivity() {
    private val appRepository = AppRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovilPOSTheme {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(appRepository)
                )
                val posViewModel: POSViewModel = viewModel(
                    factory = POSViewModel.Factory(appRepository)
                )
                val backStack = rememberNavBackStack(HomeRoute)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                            .imePadding(),
                        settingsViewModel = settingsViewModel,
                        entryProvider = { key ->
                            when (key) {
                                is HomeRoute -> NavEntry(key) {
                                    HomeScreen(
                                        onNavigateToInventory = { backStack.add(InventoryRoute) },
                                        onNavigateToPOS = { backStack.add(POSRoute) },
                                        onNavigateToPurchases = { backStack.add(PurchasesRoute) },
                                        onNavigateToSuppliers = { backStack.add(SuppliersRoute) },
                                        onNavigateToCustomers = { backStack.add(CustomersRoute) },
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
                                    POSScreen(
                                        viewModel = posViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateToCheckout = { backStack.add(POSCheckoutKey) }
                                    )
                                }
                                is POSCheckoutKey -> NavEntry(key) {
                                    POSCheckoutScreen(
                                        viewModel = posViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() },
                                        onNavigateToReceipt = { saleId ->
                                            backStack.removeLastOrNull()
                                            backStack.add(POSTicketReceiptKey(saleId))
                                        }
                                    )
                                }
                                is POSTicketReceiptKey -> NavEntry(key) {
                                    POSTicketReceiptScreen(
                                        saleId = key.saleId,
                                        posViewModel = posViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onStartNewSale = {
                                            posViewModel.clearCart()
                                            while (backStack.size > 1 && backStack.last() != POSRoute) {
                                                backStack.removeLastOrNull()
                                            }
                                            if (backStack.isEmpty() || backStack.last() != POSRoute) {
                                                backStack.clear()
                                                backStack.add(HomeRoute)
                                                backStack.add(POSRoute)
                                            }
                                        }
                                    )
                                }
                                is PurchasesRoute -> NavEntry(key) {
                                    val purchaseViewModel: PurchaseViewModel = viewModel(
                                        factory = PurchaseViewModel.Factory(appRepository)
                                    )
                                    PurchaseScreen(
                                        viewModel = purchaseViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is PurchaseDetailRoute -> NavEntry(key) {
                                    val purchaseViewModel: PurchaseViewModel = viewModel(
                                        factory = PurchaseViewModel.Factory(appRepository)
                                    )
                                    PurchaseReadOnlyView(
                                        purchaseId = key.purchaseId,
                                        viewModel = purchaseViewModel,
                                        onNavigateUp = { backStack.removeLastOrNull() }
                                    )
                                }
                                is PurchaseEditRoute -> NavEntry(key) {
                                    val purchaseViewModel: PurchaseViewModel = viewModel(
                                        factory = PurchaseViewModel.Factory(appRepository)
                                    )
                                    PurchaseEditForm(
                                        viewModel = purchaseViewModel,
                                        onNavigateUp = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SuppliersRoute -> NavEntry(key) {
                                    val supplierViewModel: SupplierViewModel = viewModel(
                                        factory = SupplierViewModel.Factory(appRepository)
                                    )
                                    SupplierScreen(
                                        viewModel = supplierViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SupplierDetailRoute -> NavEntry(key) {
                                    val supplierViewModel: SupplierViewModel = viewModel(
                                        factory = SupplierViewModel.Factory(appRepository)
                                    )
                                    SupplierReadOnlyView(
                                        supplierId = key.supplierId,
                                        viewModel = supplierViewModel,
                                        onEditClick = { backStack.add(SupplierEditRoute(key.supplierId)) },
                                        onNavigateUp = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SupplierEditRoute -> NavEntry(key) {
                                    val supplierViewModel: SupplierViewModel = viewModel(
                                        factory = SupplierViewModel.Factory(appRepository)
                                    )
                                    SupplierEditForm(
                                        supplierId = key.supplierId,
                                        viewModel = supplierViewModel,
                                        onNavigateUp = { backStack.removeLastOrNull() }
                                    )
                                }
                                is CustomersRoute -> NavEntry(key) {
                                    val customerViewModel: CustomerViewModel = viewModel(
                                        factory = CustomerViewModel.Factory(appRepository)
                                    )
                                    CustomerScreen(
                                        viewModel = customerViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is CustomerDetailRoute -> NavEntry(key) {
                                    val customerViewModel: CustomerViewModel = viewModel(
                                        factory = CustomerViewModel.Factory(appRepository)
                                    )
                                    CustomerReadOnlyView(
                                        customerId = key.customerId,
                                        viewModel = customerViewModel,
                                        onEditClick = { backStack.add(CustomerEditRoute(key.customerId)) },
                                        onNavigateUp = { backStack.removeLastOrNull() }
                                    )
                                }
                                is CustomerEditRoute -> NavEntry(key) {
                                    val customerViewModel: CustomerViewModel = viewModel(
                                        factory = CustomerViewModel.Factory(appRepository)
                                    )
                                    CustomerEditForm(
                                        customerId = key.customerId,
                                        viewModel = customerViewModel,
                                        onNavigateUp = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SettingsRoute -> NavEntry(key) {
                                    SettingsScreen(
                                        viewModel = settingsViewModel,
                                        onNavigateToAnimation = { backStack.add(SettingsAnimationKey) },
                                        onNavigateToCurrency = { backStack.add(SettingsCurrencyKey) },
                                        onNavigateToCategories = { backStack.add(SettingsCategoriesKey) },
                                        onNavigateToBrands = { backStack.add(SettingsBrandsKey) },
                                        onNavigateToUnits = { backStack.add(SettingsUnitsKey) },
                                        onNavigateToTaxes = { backStack.add(SettingsTaxesKey) },
                                        onNavigateToPriceRules = { backStack.add(SettingsPriceRulesKey) },
                                        onNavigateToPaymentMethods = { backStack.add(SettingsPaymentMethodsKey) },
                                        onNavigateToBusinessInfo = { backStack.add(SettingsBusinessInfoKey) },
                                        onNavigateToTicket = { backStack.add(SettingsTicketKey) },
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SettingsBusinessInfoKey -> NavEntry(key) {
                                    SettingsBusinessInfoScreen(
                                        viewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SettingsTicketKey -> NavEntry(key) {
                                    SettingsTicketScreen(
                                        viewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SettingsAnimationKey -> NavEntry(key) {
                                    SettingsAnimationScreen(
                                        viewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SettingsCurrencyKey -> NavEntry(key) {
                                    SettingsCurrencyScreen(
                                        viewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SettingsCategoriesKey -> NavEntry(key) {
                                    SettingsCategoriesScreen(
                                        viewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SettingsBrandsKey -> NavEntry(key) {
                                    SettingsBrandsScreen(
                                        viewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SettingsUnitsKey -> NavEntry(key) {
                                    SettingsUnitsScreen(
                                        viewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SettingsTaxesKey -> NavEntry(key) {
                                    SettingsTaxesScreen(
                                        viewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SettingsPriceRulesKey -> NavEntry(key) {
                                    SettingsPriceRulesScreen(
                                        viewModel = settingsViewModel,
                                        onNavigateToEditRule = { ruleId ->
                                            backStack.add(PriceRuleEditKey(ruleId))
                                        },
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SettingsPaymentMethodsKey -> NavEntry(key) {
                                    SettingsPaymentMethodsScreen(
                                        viewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is PriceRuleEditKey -> NavEntry(key) {
                                    PriceRuleEditScreen(
                                        ruleId = key.ruleId,
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
    onNavigateToPurchases: () -> Unit,
    onNavigateToSuppliers: () -> Unit,
    onNavigateToCustomers: () -> Unit,
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
                title = "Compras",
                icon = Icons.Rounded.ShoppingBag,
                onClick = onNavigateToPurchases
            )
        }
        item {
            MenuCard(
                title = "Proveedores",
                icon = Icons.Rounded.LocalShipping,
                onClick = onNavigateToSuppliers
            )
        }
        item {
            MenuCard(
                title = "Clientes",
                icon = Icons.Rounded.Person,
                onClick = onNavigateToCustomers
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
