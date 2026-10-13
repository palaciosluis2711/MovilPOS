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
import androidx.compose.material.icons.rounded.CreditScore
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.automirrored.rounded.AssignmentReturn
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.MoneyOff
import androidx.compose.material.icons.rounded.Outbox
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.RequestQuote
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.graphics.Color
import com.lopezapp.movilpos.util.FirebaseSyncService
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import android.widget.Toast
import com.lopezapp.movilpos.data.model.AuthSession
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.Role
import com.lopezapp.movilpos.data.model.toSpanishLabel
import com.lopezapp.movilpos.ui.LoginScreen
import com.lopezapp.movilpos.ui.navigation.LoginRoute
import com.lopezapp.movilpos.ui.viewmodel.AuthViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.lopezapp.movilpos.ui.QuotationEditForm
import com.lopezapp.movilpos.ui.QuotationListScreen
import com.lopezapp.movilpos.ui.QuotationReadOnlyView
import com.lopezapp.movilpos.ui.SaleReadOnlyView
import com.lopezapp.movilpos.ui.SalesListScreen
import com.lopezapp.movilpos.ui.DteReportsListScreen
import com.lopezapp.movilpos.ui.DteDetailScreen
import com.lopezapp.movilpos.ui.SettingsAnimationScreen
import com.lopezapp.movilpos.ui.SettingsBrandsScreen
import com.lopezapp.movilpos.ui.SettingsBusinessInfoScreen
import com.lopezapp.movilpos.ui.SettingsCategoriesScreen
import com.lopezapp.movilpos.ui.SettingsCurrencyScreen
import com.lopezapp.movilpos.ui.SettingsElectronicBillingScreen
import com.lopezapp.movilpos.ui.SettingsFirebaseScreen
import com.lopezapp.movilpos.ui.SettingsPaymentMethodsScreen
import com.lopezapp.movilpos.ui.SettingsPriceRulesScreen
import com.lopezapp.movilpos.ui.SettingsScreen
import com.lopezapp.movilpos.ui.SettingsTaxesScreen
import com.lopezapp.movilpos.ui.SettingsTicketScreen
import com.lopezapp.movilpos.ui.SettingsBluetoothPrinterScreen
import com.lopezapp.movilpos.ui.SettingsUnitsScreen
import com.lopezapp.movilpos.ui.SettingsUsersScreen
import com.lopezapp.movilpos.ui.UserEditScreen
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
import com.lopezapp.movilpos.ui.navigation.QuotationDetailRoute
import com.lopezapp.movilpos.ui.navigation.QuotationEditRoute
import com.lopezapp.movilpos.ui.navigation.QuotationsRoute
import com.lopezapp.movilpos.ui.navigation.SaleDetailRoute
import com.lopezapp.movilpos.ui.navigation.SalesRoute
import com.lopezapp.movilpos.ui.navigation.DteReportsRoute
import com.lopezapp.movilpos.ui.navigation.DteDetailRoute
import com.lopezapp.movilpos.ui.navigation.SettingsAnimationKey
import com.lopezapp.movilpos.ui.navigation.SettingsBrandsKey
import com.lopezapp.movilpos.ui.navigation.SettingsBusinessInfoKey
import com.lopezapp.movilpos.ui.navigation.SettingsCategoriesKey
import com.lopezapp.movilpos.ui.navigation.SettingsCurrencyKey
import com.lopezapp.movilpos.ui.navigation.SettingsElectronicBillingKey
import com.lopezapp.movilpos.ui.navigation.SettingsFirebaseKey
import com.lopezapp.movilpos.ui.navigation.SettingsPaymentMethodsKey
import com.lopezapp.movilpos.ui.navigation.SettingsPriceRulesKey
import com.lopezapp.movilpos.ui.navigation.SettingsRoute
import com.lopezapp.movilpos.ui.navigation.SettingsTaxesKey
import com.lopezapp.movilpos.ui.navigation.SettingsTicketKey
import com.lopezapp.movilpos.ui.navigation.SettingsBluetoothPrinterKey
import com.lopezapp.movilpos.ui.navigation.SettingsUnitsKey
import com.lopezapp.movilpos.ui.navigation.SettingsUsersKey
import com.lopezapp.movilpos.ui.navigation.UserEditKey
import com.lopezapp.movilpos.ui.navigation.SupplierDetailRoute
import com.lopezapp.movilpos.ui.navigation.SupplierEditRoute
import com.lopezapp.movilpos.ui.navigation.SuppliersRoute
import com.lopezapp.movilpos.ui.theme.MovilPOSTheme
import com.lopezapp.movilpos.ui.AnalyticsScreen
import com.lopezapp.movilpos.ui.AccountsReceivableScreen
import com.lopezapp.movilpos.ui.AccountsReceivableDetailScreen
import com.lopezapp.movilpos.ui.ExpensesScreen
import com.lopezapp.movilpos.ui.BarcodeLabelScreen
import com.lopezapp.movilpos.ui.InventoryOutflowScreen
import com.lopezapp.movilpos.ui.InventoryOutflowEditForm
import com.lopezapp.movilpos.ui.InventoryOutflowReadOnlyView
import com.lopezapp.movilpos.ui.ProductReturnScreen
import com.lopezapp.movilpos.ui.ProductReturnEditForm
import com.lopezapp.movilpos.ui.ProductReturnReadOnlyView
import com.lopezapp.movilpos.ui.navigation.AnalyticsRoute
import com.lopezapp.movilpos.ui.navigation.AccountsReceivableRoute
import com.lopezapp.movilpos.ui.navigation.AccountsReceivableDetailRoute
import com.lopezapp.movilpos.ui.navigation.ExpensesRoute
import com.lopezapp.movilpos.ui.navigation.BarcodeLabelsRoute
import com.lopezapp.movilpos.ui.navigation.InventoryOutflowsRoute
import com.lopezapp.movilpos.ui.navigation.InventoryOutflowDetailRoute
import com.lopezapp.movilpos.ui.navigation.InventoryOutflowEditRoute
import com.lopezapp.movilpos.ui.navigation.ProductReturnsRoute
import com.lopezapp.movilpos.ui.navigation.ProductReturnDetailRoute
import com.lopezapp.movilpos.ui.navigation.ProductReturnEditRoute
import com.lopezapp.movilpos.ui.viewmodel.AnalyticsViewModel
import com.lopezapp.movilpos.ui.viewmodel.AccountsReceivableViewModel
import com.lopezapp.movilpos.ui.viewmodel.BarcodeLabelViewModel
import com.lopezapp.movilpos.ui.viewmodel.CustomerViewModel
import com.lopezapp.movilpos.ui.viewmodel.ExpensesViewModel
import com.lopezapp.movilpos.ui.viewmodel.InventoryOutflowViewModel
import com.lopezapp.movilpos.ui.viewmodel.ProductReturnViewModel
import com.lopezapp.movilpos.ui.viewmodel.InventoryViewModel
import com.lopezapp.movilpos.ui.viewmodel.POSViewModel
import com.lopezapp.movilpos.ui.viewmodel.PurchaseViewModel
import com.lopezapp.movilpos.ui.viewmodel.QuotationViewModel
import com.lopezapp.movilpos.ui.viewmodel.SalesViewModel
import com.lopezapp.movilpos.ui.viewmodel.DteReportsViewModel
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import com.lopezapp.movilpos.ui.viewmodel.SupplierViewModel

class MainActivity : ComponentActivity() {
    private val appRepository = AppRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appRepository.initializeLocalDiskPersistence(applicationContext)
        enableEdgeToEdge()
        setContent {
            MovilPOSTheme {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(appRepository)
                )
                val posViewModel: POSViewModel = viewModel(
                    factory = POSViewModel.Factory(appRepository)
                )
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.Factory(appRepository)
                )

                val currentAuthSession by authViewModel.authSession.collectAsState()
                val isSyncing by FirebaseSyncService.isSyncing.collectAsState()
                val context = LocalContext.current

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        val screenModifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                            .imePadding()

                        val isLoggedIn = currentAuthSession?.isLoggedIn == true
                        val initialRoute = if (isLoggedIn) HomeRoute else LoginRoute
                        val backStack = rememberNavBackStack(initialRoute)

                        LaunchedEffect(isLoggedIn) {
                            if (isLoggedIn) {
                                if (backStack.lastOrNull() != HomeRoute) {
                                    backStack.clear()
                                    backStack.add(HomeRoute)
                                }
                            } else {
                                if (backStack.lastOrNull() != LoginRoute) {
                                    backStack.clear()
                                    backStack.add(LoginRoute)
                                }
                            }
                        }

                        AppNavDisplay(
                            backStack = backStack,
                            onBack = { backStack.removeLastOrNull() },
                            modifier = screenModifier,
                            settingsViewModel = settingsViewModel,
                            entryProvider = { key ->
                                when (key) {
                                    is LoginRoute -> NavEntry(key) {
                                        LoginScreen(
                                            authViewModel = authViewModel,
                                            settingsViewModel = settingsViewModel
                                        )
                                    }
                                    is HomeRoute -> NavEntry(key) {
                                        HomeScreen(
                                            authViewModel = authViewModel,
                                            settingsViewModel = settingsViewModel,
                                            onLogoutClick = {
                                                authViewModel.logout()
                                            },
                                        onNavigateToInventory = { backStack.add(InventoryRoute) },
                                        onNavigateToPOS = { backStack.add(POSRoute) },
                                        onNavigateToSales = { backStack.add(SalesRoute) },
                                        onNavigateToQuotations = { backStack.add(QuotationsRoute) },
                                        onNavigateToPurchases = { backStack.add(PurchasesRoute) },
                                        onNavigateToExpenses = { backStack.add(ExpensesRoute) },
                                        onNavigateToInventoryOutflows = { backStack.add(InventoryOutflowsRoute) },
                                        onNavigateToProductReturns = { backStack.add(ProductReturnsRoute) },
                                        onNavigateToSuppliers = { backStack.add(SuppliersRoute) },
                                        onNavigateToCustomers = { backStack.add(CustomersRoute) },
                                        onNavigateToAccountsReceivable = { backStack.add(AccountsReceivableRoute) },
                                        onNavigateToBarcodeLabels = { backStack.add(BarcodeLabelsRoute) },
                                        onNavigateToSettings = { backStack.add(SettingsRoute) },
                                        onNavigateToDteReports = { backStack.add(DteReportsRoute) },
                                        onNavigateToAnalytics = { backStack.add(AnalyticsRoute) }
                                    )
                                }
                                is BarcodeLabelsRoute -> NavEntry(key) {
                                    val barcodeLabelViewModel: BarcodeLabelViewModel = viewModel(
                                        factory = BarcodeLabelViewModel.Factory(appRepository)
                                    )
                                    BarcodeLabelScreen(
                                        viewModel = barcodeLabelViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is ExpensesRoute -> NavEntry(key) {
                                    val expensesViewModel: ExpensesViewModel = viewModel(
                                        factory = ExpensesViewModel.Factory(appRepository)
                                    )
                                    ExpensesScreen(
                                        viewModel = expensesViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is InventoryRoute -> NavEntry(key) {
                                    val inventoryViewModel: InventoryViewModel = viewModel(
                                        factory = InventoryViewModel.Factory(appRepository)
                                    )
                                    InventoryScreen(
                                        viewModel = inventoryViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() },
                                        onNavigateToBarcodeLabels = { backStack.add(BarcodeLabelsRoute) }
                                    )
                                }
                                is AnalyticsRoute -> NavEntry(key) {
                                    val analyticsViewModel: AnalyticsViewModel = viewModel(
                                        factory = AnalyticsViewModel.Factory(appRepository)
                                    )
                                    AnalyticsScreen(
                                        viewModel = analyticsViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is POSRoute -> NavEntry(key) {
                                    POSScreen(
                                        viewModel = posViewModel,
                                        settingsViewModel = settingsViewModel,
                                        authViewModel = authViewModel,
                                        onLogoutClick = {
                                            authViewModel.logout()
                                        },
                                        onNavigateToCheckout = { backStack.add(POSCheckoutKey) },
                                        onBackClick = { backStack.removeLastOrNull() }
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
                                        },
                                        onNavigateToSettings = {
                                            backStack.removeLastOrNull()
                                            backStack.add(SettingsElectronicBillingKey)
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
                                is SalesRoute -> NavEntry(key) {
                                    val salesViewModel: SalesViewModel = viewModel(
                                        factory = SalesViewModel.Factory(appRepository)
                                    )
                                    SalesListScreen(
                                        viewModel = salesViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateToDetail = { saleId -> backStack.add(SaleDetailRoute(saleId)) },
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is DteReportsRoute -> NavEntry(key) {
                                    val dteReportsViewModel: DteReportsViewModel = viewModel(
                                        factory = DteReportsViewModel.Factory(appRepository)
                                    )
                                    DteReportsListScreen(
                                        viewModel = dteReportsViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateToDetail = { saleId -> backStack.add(DteDetailRoute(saleId)) },
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is DteDetailRoute -> NavEntry(key) {
                                    val dteReportsViewModel: DteReportsViewModel = viewModel(
                                        factory = DteReportsViewModel.Factory(appRepository)
                                    )
                                    DteDetailScreen(
                                        saleId = key.saleId,
                                        viewModel = dteReportsViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SaleDetailRoute -> NavEntry(key) {
                                    val salesViewModel: SalesViewModel = viewModel(
                                        factory = SalesViewModel.Factory(appRepository)
                                    )
                                    SaleReadOnlyView(
                                        saleId = key.saleId,
                                        salesViewModel = salesViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateUp = { backStack.removeLastOrNull() },
                                        onNavigateToDteDetail = { saleId -> backStack.add(DteDetailRoute(saleId)) },
                                        onNavigateToElectronicBilling = { backStack.add(SettingsElectronicBillingKey) }
                                    )
                                }
                                is QuotationsRoute -> NavEntry(key) {
                                    val quotationViewModel: QuotationViewModel = viewModel(
                                        factory = QuotationViewModel.Factory(appRepository)
                                    )
                                    QuotationListScreen(
                                        viewModel = quotationViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateToDetail = { quotationId -> backStack.add(QuotationDetailRoute(quotationId)) },
                                        onNavigateToCreate = { backStack.add(QuotationEditRoute()) },
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is QuotationDetailRoute -> NavEntry(key) {
                                    val quotationViewModel: QuotationViewModel = viewModel(
                                        factory = QuotationViewModel.Factory(appRepository)
                                    )
                                    QuotationReadOnlyView(
                                        quotationId = key.quotationId,
                                        viewModel = quotationViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateToEdit = { quotationId -> backStack.add(QuotationEditRoute(quotationId)) },
                                        onNavigateUp = { backStack.removeLastOrNull() }
                                    )
                                }
                                is QuotationEditRoute -> NavEntry(key) {
                                    val quotationViewModel: QuotationViewModel = viewModel(
                                        factory = QuotationViewModel.Factory(appRepository)
                                    )
                                    LaunchedEffect(key.quotationId) {
                                        key.quotationId?.let { id ->
                                            quotationViewModel.getQuotationById(id)?.let { q ->
                                                quotationViewModel.loadQuotationForEdit(q)
                                            }
                                        }
                                    }
                                    QuotationEditForm(
                                        viewModel = quotationViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateUp = { backStack.removeLastOrNull() }
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
                                is InventoryOutflowsRoute -> NavEntry(key) {
                                    val outflowViewModel: InventoryOutflowViewModel = viewModel(
                                        factory = InventoryOutflowViewModel.Factory(appRepository)
                                    )
                                    InventoryOutflowScreen(
                                        viewModel = outflowViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is InventoryOutflowDetailRoute -> NavEntry(key) {
                                    val outflowViewModel: InventoryOutflowViewModel = viewModel(
                                        factory = InventoryOutflowViewModel.Factory(appRepository)
                                    )
                                    InventoryOutflowReadOnlyView(
                                        outflowId = key.outflowId,
                                        viewModel = outflowViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateUp = { backStack.removeLastOrNull() }
                                    )
                                }
                                is InventoryOutflowEditRoute -> NavEntry(key) {
                                    val outflowViewModel: InventoryOutflowViewModel = viewModel(
                                        factory = InventoryOutflowViewModel.Factory(appRepository)
                                    )
                                    InventoryOutflowEditForm(
                                        viewModel = outflowViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateUp = { backStack.removeLastOrNull() }
                                    )
                                }
                                is ProductReturnsRoute -> NavEntry(key) {
                                    val productReturnViewModel: ProductReturnViewModel = viewModel(
                                        factory = ProductReturnViewModel.Factory(appRepository)
                                    )
                                    ProductReturnScreen(
                                        viewModel = productReturnViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() },
                                        settingsViewModel = settingsViewModel
                                    )
                                }
                                is ProductReturnDetailRoute -> NavEntry(key) {
                                    val productReturnViewModel: ProductReturnViewModel = viewModel(
                                        factory = ProductReturnViewModel.Factory(appRepository)
                                    )
                                    ProductReturnReadOnlyView(
                                        returnId = key.returnId,
                                        viewModel = productReturnViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateUp = { backStack.removeLastOrNull() }
                                    )
                                }
                                is ProductReturnEditRoute -> NavEntry(key) {
                                    val productReturnViewModel: ProductReturnViewModel = viewModel(
                                        factory = ProductReturnViewModel.Factory(appRepository)
                                    )
                                    ProductReturnEditForm(
                                        viewModel = productReturnViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onNavigateUp = { backStack.removeLastOrNull() }
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
                                is AccountsReceivableRoute -> NavEntry(key) {
                                    val accountsReceivableViewModel: AccountsReceivableViewModel = viewModel(
                                        factory = AccountsReceivableViewModel.Factory(appRepository)
                                    )
                                    AccountsReceivableScreen(
                                        viewModel = accountsReceivableViewModel,
                                        settingsViewModel = settingsViewModel,
                                        onBackClick = { backStack.removeLastOrNull() },
                                        onCustomerClick = { customerId ->
                                            backStack.add(AccountsReceivableDetailRoute(customerId))
                                        }
                                    )
                                }
                                is AccountsReceivableDetailRoute -> NavEntry(key) {
                                    val accountsReceivableViewModel: AccountsReceivableViewModel = viewModel(
                                        factory = AccountsReceivableViewModel.Factory(appRepository)
                                    )
                                    AccountsReceivableDetailScreen(
                                        customerId = key.customerId,
                                        viewModel = accountsReceivableViewModel,
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
                                        onNavigateToBluetoothPrinter = { backStack.add(SettingsBluetoothPrinterKey) },
                                        onNavigateToElectronicBilling = { backStack.add(SettingsElectronicBillingKey) },
                                        onNavigateToFirebase = { backStack.add(SettingsFirebaseKey) },
                                        onNavigateToUsers = { backStack.add(SettingsUsersKey) },
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SettingsFirebaseKey -> NavEntry(key) {
                                    SettingsFirebaseScreen(
                                        repository = appRepository,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SettingsUsersKey -> NavEntry(key) {
                                    SettingsUsersScreen(
                                        viewModel = settingsViewModel,
                                        onNavigateToUserEdit = { userId -> backStack.add(UserEditKey(userId)) },
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is UserEditKey -> NavEntry(key) {
                                    UserEditScreen(
                                        viewModel = settingsViewModel,
                                        userId = key.userId,
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
                                is SettingsBluetoothPrinterKey -> NavEntry(key) {
                                    SettingsBluetoothPrinterScreen(
                                        viewModel = settingsViewModel,
                                        onNavigateBack = { backStack.removeLastOrNull() }
                                    )
                                }
                                is SettingsElectronicBillingKey -> NavEntry(key) {
                                    SettingsElectronicBillingScreen(
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

                if (isSyncing) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding(),
                        color = Color(0xCC2E7D32), // Semi-transparent green
                        contentColor = Color.White
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.White,
                                    strokeWidth = 1.5.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sincronizando datos...",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp),
                                color = Color.White,
                                trackColor = Color.Transparent
                            )
                        }
                    }
                }
            }
        }
    }
}
}

data class HomeMenuItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val allowedRoles: Set<Role>,
    val enabled: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel? = null,
    settingsViewModel: SettingsViewModel? = null,
    electronicBillingConfig: ElectronicBillingConfig? = null,
    authSession: AuthSession? = null,
    businessInfo: BusinessInfo? = null,
    onLogoutClick: () -> Unit = {},
    onNavigateToInventory: () -> Unit,
    onNavigateToPOS: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToQuotations: () -> Unit,
    onNavigateToPurchases: () -> Unit,
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToInventoryOutflows: () -> Unit = {},
    onNavigateToProductReturns: () -> Unit = {},
    onNavigateToSuppliers: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToAccountsReceivable: () -> Unit = {},
    onNavigateToBarcodeLabels: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    onNavigateToDteReports: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentAuthSession = authSession ?: authViewModel?.authSession?.collectAsState()?.value
    val settingsUiState = settingsViewModel?.uiState?.collectAsState()?.value
    val effectiveEbConfig = electronicBillingConfig ?: settingsUiState?.electronicBillingConfig ?: ElectronicBillingConfig()
    val isDteEnabled = effectiveEbConfig.isEnabled

    val activeUser = currentAuthSession?.user
    val userRole = activeUser?.role ?: Role.ADMINISTRATOR

    
    var showLogoutDialog by remember { mutableStateOf(false) }
    val isLoggingOut = authViewModel?.isLoggingOut?.collectAsState()?.value == true

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Desea cerrar sesión ahora?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogoutClick()
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (isLoggingOut) {
        Dialog(onDismissRequest = { /* Prevent dismiss */ }) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Cerrando sesión...")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = "MovilPOS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (activeUser != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "${activeUser.name} • ${activeUser.role.toSpanishLabel()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onLogoutClick()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar Sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val menuItems = listOf(
                HomeMenuItem(
                    title = "POS",
                    icon = Icons.Rounded.PointOfSale,
                    onClick = onNavigateToPOS,
                    allowedRoles = setOf(Role.CASHIER, Role.SUPERVISOR, Role.ADMINISTRATOR)
                ),
                HomeMenuItem(
                    title = "Clientes",
                    icon = Icons.Rounded.Person,
                    onClick = onNavigateToCustomers,
                    allowedRoles = setOf(Role.CASHIER, Role.SUPERVISOR, Role.ADMINISTRATOR)
                ),
                HomeMenuItem(
                    title = "Cotizaciones",
                    icon = Icons.Rounded.RequestQuote,
                    onClick = onNavigateToQuotations,
                    allowedRoles = setOf(Role.CASHIER, Role.SUPERVISOR, Role.ADMINISTRATOR)
                ),
                HomeMenuItem(
                    title = "Inventario",
                    icon = Icons.Rounded.Inventory,
                    onClick = onNavigateToInventory,
                    allowedRoles = setOf(Role.SUPERVISOR, Role.ADMINISTRATOR)
                ),
                HomeMenuItem(
                    title = "Ventas",
                    icon = Icons.Rounded.ReceiptLong,
                    onClick = onNavigateToSales,
                    allowedRoles = setOf(Role.SUPERVISOR, Role.ADMINISTRATOR)
                ),
                HomeMenuItem(
                    title = "Etiquetas de Productos",
                    subtitle = "Códigos de barras y etiquetas térmicas o carta",
                    icon = Icons.Rounded.Print,
                    onClick = onNavigateToBarcodeLabels,
                    allowedRoles = setOf(Role.SUPERVISOR, Role.ADMINISTRATOR)
                ),
                HomeMenuItem(
                    title = "Salidas de Inventario",
                    subtitle = "Uso interno, mermas, vencimientos y bajas de productos",
                    icon = Icons.Rounded.Outbox,
                    onClick = onNavigateToInventoryOutflows,
                    allowedRoles = setOf(Role.ADMINISTRATOR)
                ),
                HomeMenuItem(
                    title = "Devoluciones",
                    subtitle = "Devoluciones de clientes, notas de crédito y reposición de stock",
                    icon = Icons.AutoMirrored.Rounded.AssignmentReturn,
                    onClick = onNavigateToProductReturns,
                    allowedRoles = setOf(Role.CASHIER, Role.SUPERVISOR, Role.ADMINISTRATOR)
                ),
                HomeMenuItem(
                    title = "Cuentas por Cobrar",
                    subtitle = "Saldos deudores, fiados y registro de abonos",
                    icon = Icons.Rounded.CreditScore,
                    onClick = onNavigateToAccountsReceivable,
                    allowedRoles = setOf(Role.ADMINISTRATOR)
                ),
                HomeMenuItem(
                    title = "Estadísticas",
                    icon = Icons.Rounded.Analytics,
                    onClick = onNavigateToAnalytics,
                    allowedRoles = setOf(Role.ADMINISTRATOR)
                ),
                HomeMenuItem(
                    title = "Reportes DTE y MH",
                    icon = Icons.Rounded.CloudDone,
                    onClick = {
                        if (isDteEnabled) {
                            onNavigateToDteReports()
                        } else {
                            Toast.makeText(context, "Facturación Electrónica deshabilitada en Ajustes", Toast.LENGTH_SHORT).show()
                        }
                    },
                    allowedRoles = setOf(Role.ADMINISTRATOR),
                    enabled = isDteEnabled
                ),
                HomeMenuItem(
                    title = "Compras",
                    icon = Icons.Rounded.ShoppingBag,
                    onClick = onNavigateToPurchases,
                    allowedRoles = setOf(Role.ADMINISTRATOR)
                ),
                HomeMenuItem(
                    title = "Gastos de Caja",
                    subtitle = "Registrar y consultar egresos o salidas de efectivo",
                    icon = Icons.Rounded.MoneyOff,
                    onClick = onNavigateToExpenses,
                    allowedRoles = setOf(Role.ADMINISTRATOR)
                ),
                HomeMenuItem(
                    title = "Proveedores",
                    icon = Icons.Rounded.LocalShipping,
                    onClick = onNavigateToSuppliers,
                    allowedRoles = setOf(Role.ADMINISTRATOR)
                ),
                HomeMenuItem(
                    title = "Configuración",
                    icon = Icons.Rounded.Settings,
                    onClick = onNavigateToSettings,
                    allowedRoles = setOf(Role.ADMINISTRATOR)
                )
            )

            val visibleItems = menuItems.filter { userRole in it.allowedRoles }

            items(visibleItems.size) { index ->
                val item = visibleItems[index]
                MenuCard(
                    title = item.title,
                    subtitle = item.subtitle,
                    icon = item.icon,
                    onClick = item.onClick,
                    enabled = item.enabled
                )
            }
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .then(if (!enabled) Modifier.alpha(0.5f) else Modifier)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
