package com.lopezapp.movilpos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import com.lopezapp.movilpos.data.model.BluetoothPrinterConfig
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.CustomerPayment
import com.lopezapp.movilpos.data.model.DocumentType
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.TicketConfig
import com.lopezapp.movilpos.ui.components.CompactSearchBar
import com.lopezapp.movilpos.ui.theme.MovilPOSTheme
import com.lopezapp.movilpos.ui.viewmodel.AccountsReceivableViewModel
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import com.lopezapp.movilpos.util.EscPosPrinter
import com.lopezapp.movilpos.util.PdfReportGenerator
import com.lopezapp.movilpos.util.formatCurrency
import com.lopezapp.movilpos.util.sanitizeDecimalTextFieldValue

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Custom Amber/Orange palette for Accounts Receivable
private val AmberContainer = Color(0xFFFFF3E0)
private val AmberOnContainer = Color(0xFFE65100)
private val AmberDarkText = Color(0xFFBF360C)
private val AmberAccent = Color(0xFFFB8C00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsReceivableScreen(
    viewModel: AccountsReceivableViewModel,
    settingsViewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialCustomerId: String? = null
) {
    val totalAccountsReceivable by viewModel.totalAccountsReceivable.collectAsState()
    val debtorCustomers by viewModel.debtorCustomers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val allSales by viewModel.sales.collectAsState()
    val customerPayments by viewModel.customerPayments.collectAsState()
    val settingsUiState by settingsViewModel.uiState.collectAsState()

    var selectedCustomer by remember(initialCustomerId, debtorCustomers) {
        mutableStateOf(
            if (!initialCustomerId.isNullOrBlank()) {
                viewModel.getCustomerById(initialCustomerId)
            } else {
                null
            }
        )
    }

    var showRegisterPaymentDialog by remember { mutableStateOf(false) }
    var dialogCustomer by remember { mutableStateOf<Customer?>(null) }
    var dialogPreselectedSaleId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedCustomer != null) {
                            "Detalle de Cuenta"
                        } else {
                            "Cuentas por Cobrar"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedCustomer != null) {
                                selectedCustomer = null
                            } else {
                                onBackClick()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val currentSelectedCustomer = selectedCustomer
            if (currentSelectedCustomer != null) {
                // Customer Debt Details View
                CustomerDebtDetailsView(
                    customer = currentSelectedCustomer,
                    allSales = allSales,
                    customerPayments = customerPayments,
                    businessInfo = settingsUiState.businessInfo,
                    ticketConfig = settingsUiState.ticketConfig,
                    bluetoothPrinterConfig = settingsUiState.bluetoothPrinterConfig,
                    currencySymbol = settingsUiState.currencySymbol,
                    defaultDecimalPlaces = settingsUiState.defaultDecimalPlaces,
                    allowExtraDecimals = settingsUiState.allowExtraDecimals,
                    onBackToList = { selectedCustomer = null },
                    onRegisterAbonoClick = { saleId ->
                        dialogCustomer = currentSelectedCustomer
                        dialogPreselectedSaleId = saleId
                        showRegisterPaymentDialog = true
                    }
                )
            } else {
                // Main Debtor List View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(2.dp))

                    // Summary Card: Cartera Deudora Total ($)
                    AccountsReceivableSummaryCard(
                        totalAmount = totalAccountsReceivable,
                        debtorCount = debtorCustomers.size,
                        currencySymbol = settingsUiState.currencySymbol,
                        defaultDecimalPlaces = settingsUiState.defaultDecimalPlaces,
                        allowExtraDecimals = settingsUiState.allowExtraDecimals
                    )

                    // Search Bar
                    CompactSearchBar(
                        query = searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        placeholder = "Buscar deudor por nombre o documento..."
                    )

                    // List of Debtor Customers
                    if (debtorCustomers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircleOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (searchQuery.isBlank()) {
                                            "No hay clientes con saldo deudor pendiente."
                                        } else {
                                            "No se encontraron clientes deudores para \"$searchQuery\"."
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(debtorCustomers, key = { it.id }) { customer ->
                                val pendingSalesCount = allSales.count {
                                    it.customerId == customer.id && it.isCredit && !it.isVoided && it.remainingBalance > 0.0
                                }
                                DebtorCustomerCard(
                                    customer = customer,
                                    pendingSalesCount = pendingSalesCount,
                                    currencySymbol = settingsUiState.currencySymbol,
                                    defaultDecimalPlaces = settingsUiState.defaultDecimalPlaces,
                                    allowExtraDecimals = settingsUiState.allowExtraDecimals,
                                    onSelectCustomer = { selectedCustomer = customer },
                                    onRegisterAbono = {
                                        dialogCustomer = customer
                                        dialogPreselectedSaleId = null
                                        showRegisterPaymentDialog = true
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Register Payment Dialog
        if (showRegisterPaymentDialog && dialogCustomer != null) {
            val targetCustomer = dialogCustomer!!
            val customerPendingSales = remember(targetCustomer.id, allSales) {
                allSales.filter {
                    it.customerId == targetCustomer.id && it.isCredit && !it.isVoided && it.remainingBalance > 0.0
                }.sortedByDescending { it.dateMillis }
            }

            RegisterPaymentDialog(
                customer = targetCustomer,
                pendingSales = customerPendingSales,
                preselectedSaleId = dialogPreselectedSaleId,
                currencySymbol = settingsUiState.currencySymbol,
                defaultDecimalPlaces = settingsUiState.defaultDecimalPlaces,
                allowExtraDecimals = settingsUiState.allowExtraDecimals,
                onDismissRequest = {
                    showRegisterPaymentDialog = false
                    dialogCustomer = null
                    dialogPreselectedSaleId = null
                },
                onConfirmPayment = { saleId, amount, paymentMethodName, notes ->
                    viewModel.registerPayment(
                        customerId = targetCustomer.id,
                        saleId = saleId,
                        amount = amount,
                        paymentMethodName = paymentMethodName,
                        notes = notes
                    )
                    showRegisterPaymentDialog = false
                    dialogCustomer = null
                    dialogPreselectedSaleId = null
                }
            )
        }
    }
}

/**
 * Prominent Amber/Orange Summary Card for Cartera Deudora Total ($)
 */
@Composable
fun AccountsReceivableSummaryCard(
    totalAmount: Double,
    debtorCount: Int,
    currencySymbol: String,
    defaultDecimalPlaces: Int,
    allowExtraDecimals: Boolean,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = AmberContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cartera Deudora Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AmberOnContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatCurrency(
                        totalAmount,
                        currencySymbol,
                        defaultDecimalPlaces,
                        allowExtraDecimals
                    ),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = AmberDarkText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (debtorCount == 1) "1 cliente deudor registrado" else "$debtorCount clientes deudores registrados",
                    style = MaterialTheme.typography.bodySmall,
                    color = AmberOnContainer.copy(alpha = 0.8f)
                )
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AmberAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = AmberOnContainer
                )
            }
        }
    }
}

/**
 * Item Card for a Debtor Customer in the Main List
 */
@Composable
fun DebtorCustomerCard(
    customer: Customer,
    pendingSalesCount: Int,
    currencySymbol: String,
    defaultDecimalPlaces: Int,
    allowExtraDecimals: Boolean,
    onSelectCustomer: () -> Unit,
    onRegisterAbono: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelectCustomer() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${customer.documentType}: ${customer.documentNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!customer.phone.isNullOrBlank()) {
                        Text(
                            text = "Tel: ${customer.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Saldo Deudor Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Saldo Deudor",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberOnContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatCurrency(
                            customer.currentDebt,
                            currencySymbol,
                            defaultDecimalPlaces,
                            allowExtraDecimals
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberDarkText
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = if (pendingSalesCount == 1) "1 venta pendiente" else "$pendingSalesCount ventas pendientes",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onRegisterAbono,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = ButtonDefaults.ContentPadding
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Registrar Abono",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onSelectCustomer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ver Detalle",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Customer Debt Details View (shown when a debtor customer is selected)
 */
@Composable
fun CustomerDebtDetailsView(
    customer: Customer,
    allSales: List<Sale>,
    customerPayments: List<CustomerPayment>,
    businessInfo: BusinessInfo,
    ticketConfig: TicketConfig = TicketConfig(),
    bluetoothPrinterConfig: BluetoothPrinterConfig = BluetoothPrinterConfig(),
    currencySymbol: String,
    defaultDecimalPlaces: Int,
    allowExtraDecimals: Boolean,
    onBackToList: () -> Unit,
    onRegisterAbonoClick: (saleId: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val pendingCreditSales = remember(customer.id, allSales) {
        allSales.filter {
            it.customerId == customer.id && it.isCredit && !it.isVoided && it.remainingBalance > 0.0
        }.sortedByDescending { it.dateMillis }
    }

    val paymentsHistory = remember(customer.id, customerPayments) {
        customerPayments.filter {
            it.customerId == customer.id
        }.sortedByDescending { it.dateMillis }
    }

    val dateTimeFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            TextButton(
                onClick = onBackToList,
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Volver a la lista de deudores")
            }

            // Customer Summary Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = customer.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${customer.documentType}: ${customer.documentNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!customer.phone.isNullOrBlank()) {
                                Text(
                                    text = "Teléfono: ${customer.phone}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(AmberContainer)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Deuda Total",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberOnContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatCurrency(
                                        customer.currentDebt,
                                        currencySymbol,
                                        defaultDecimalPlaces,
                                        allowExtraDecimals
                                    ),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AmberDarkText
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { onRegisterAbonoClick(null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberAccent,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Payments,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Registrar Abono a Deuda",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section 1: Pending Credit Sales
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ventas a Crédito Pendientes (${pendingCreditSales.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (pendingCreditSales.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No se encontraron ventas a crédito pendientes para este cliente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(pendingCreditSales, key = { it.id }) { sale ->
                OutlinedCard(
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Venta #${sale.id.take(8).uppercase()}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = dateTimeFormat.format(Date(sale.dateMillis)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Venta: ${formatCurrency(sale.totalAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (sale.creditDueDateMillis != null) {
                                    Text(
                                        text = "Vence: ${dateFormat.format(Date(sale.creditDueDateMillis))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Saldo Pendiente",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberOnContainer
                                )
                                Text(
                                    text = formatCurrency(
                                        sale.remainingBalance,
                                        currencySymbol,
                                        defaultDecimalPlaces,
                                        allowExtraDecimals
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AmberDarkText
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { onRegisterAbonoClick(sale.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Payments,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Abonar a esta venta")
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Payment History
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Historial de Abonos (${paymentsHistory.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (paymentsHistory.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No hay registro de abonos o pagos previos para este cliente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(paymentsHistory, key = { it.id }) { payment ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = dateTimeFormat.format(Date(payment.dateMillis)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Método: ${payment.paymentMethodName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (payment.saleId != null) {
                                Text(
                                    text = "Aplicado a Venta #${payment.saleId.take(8).uppercase()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "Abono a Saldo General",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            if (!payment.notes.isNullOrBlank()) {
                                Text(
                                    text = "Nota: ${payment.notes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "+${formatCurrency(payment.amount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            IconButton(
                                onClick = {
                                    val macAddress = bluetoothPrinterConfig.macAddress
                                    if (macAddress.isNullOrBlank()) {
                                        Toast.makeText(context, "No hay impresora Bluetooth configurada.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Imprimiendo ticket de pago en impresora Bluetooth...", Toast.LENGTH_SHORT).show()
                                        val bytes = EscPosPrinter.formatPaymentTicket(
                                            payment = payment,
                                            customer = customer,
                                            businessInfo = businessInfo,
                                            ticketConfig = ticketConfig
                                        )
                                        val result = EscPosPrinter.printBytesViaBluetooth(macAddress, bytes)
                                        result.onFailure { error ->
                                            Toast.makeText(context, "Error al imprimir: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Bluetooth,
                                    contentDescription = "Imprimir por Bluetooth",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = {
                                    try {
                                        val pdfFile = PdfReportGenerator.generatePaymentReceiptTicketPdf(
                                            context = context,
                                            payment = payment,
                                            customer = customer,
                                            businessInfo = businessInfo,
                                            ticketConfig = ticketConfig
                                        )
                                        val uri: Uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            pdfFile
                                        )
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "application/pdf")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(
                                            Intent.createChooser(intent, "Ver / Imprimir Ticket PDF")
                                        )
                                        Toast.makeText(
                                            context,
                                            "Ticket de abono generado exitosamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(
                                            context,
                                            "Error al generar ticket: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                                    contentDescription = "Ver / Imprimir Ticket PDF",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = {
                                    try {
                                        val pdfFile = PdfReportGenerator.generatePaymentReceiptPdf(
                                            context = context,
                                            payment = payment,
                                            customer = customer,
                                            businessInfo = businessInfo
                                        )
                                        val uri: Uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            pdfFile
                                        )
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "application/pdf")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(
                                            Intent.createChooser(intent, "Ver / Imprimir Recibo PDF")
                                        )
                                        Toast.makeText(
                                            context,
                                            "Recibo de abono generado exitosamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(
                                            context,
                                            "Error al generar recibo: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "Ver / Imprimir Recibo PDF",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Register Payment Dialog ("Registrar Abono / Pago de Deuda")
 */
@Composable
fun RegisterPaymentDialog(
    customer: Customer,
    pendingSales: List<Sale>,
    preselectedSaleId: String?,
    currencySymbol: String,
    defaultDecimalPlaces: Int,
    allowExtraDecimals: Boolean,
    onDismissRequest: () -> Unit,
    onConfirmPayment: (saleId: String?, amount: Double, paymentMethodName: String, notes: String?) -> Unit
) {
    var selectedSaleId by remember { mutableStateOf(preselectedSaleId) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Selected sale balance or customer debt
    val maxAvailableDebt = remember(selectedSaleId, customer, pendingSales) {
        if (selectedSaleId != null) {
            pendingSales.find { it.id == selectedSaleId }?.remainingBalance ?: customer.currentDebt
        } else {
            customer.currentDebt
        }
    }

    val initialAmountString = remember(maxAvailableDebt) {
        if (maxAvailableDebt > 0.0) {
            String.format(Locale.US, "%.2f", maxAvailableDebt)
        } else {
            ""
        }
    }

    var amountTextFieldValue by remember {
        mutableStateOf(TextFieldValue(text = initialAmountString))
    }

    var selectedPaymentMethodName by remember { mutableStateOf("Efectivo") }
    var notesText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val paymentMethods = listOf("Efectivo", "Tarjeta", "Transferencia")

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column {
                Text(
                    text = "Registrar Abono / Pago de Deuda",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cliente: ${customer.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Deuda Actual: ${formatCurrency(customer.currentDebt, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AmberDarkText
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dropdown to select pending sale or general customer debt
                Column {
                    Text(
                        text = "Abonar a:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        val currentSelectionText = if (selectedSaleId == null) {
                            "Saldo General (Todos) - $${String.format(Locale.US, "%.2f", customer.currentDebt)}"
                        } else {
                            val sale = pendingSales.find { it.id == selectedSaleId }
                            if (sale != null) {
                                "Venta #${sale.id.take(8).uppercase()} - Saldo: $${String.format(Locale.US, "%.2f", sale.remainingBalance)}"
                            } else {
                                "Saldo General (Todos)"
                            }
                        }

                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { dropdownExpanded = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentSelectionText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Expandir opciones"
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text("Saldo General (Aplicar a deuda global)")
                                },
                                onClick = {
                                    selectedSaleId = null
                                    dropdownExpanded = false
                                    val newMax = customer.currentDebt
                                    amountTextFieldValue = TextFieldValue(text = String.format(Locale.US, "%.2f", newMax))
                                    validationError = null
                                }
                            )

                            if (pendingSales.isNotEmpty()) {
                                HorizontalDivider()
                                pendingSales.forEach { sale ->
                                    DropdownMenuItem(
                                        text = {
                                            Text("Venta #${sale.id.take(8).uppercase()} (Saldo: ${formatCurrency(sale.remainingBalance, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)})")
                                        },
                                        onClick = {
                                            selectedSaleId = sale.id
                                            dropdownExpanded = false
                                            val newMax = sale.remainingBalance
                                            amountTextFieldValue = TextFieldValue(text = String.format(Locale.US, "%.2f", newMax))
                                            validationError = null
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Monto a Abonar ($)
                Column {
                    OutlinedTextField(
                        value = amountTextFieldValue,
                        onValueChange = { newValue ->
                            validationError = null
                            amountTextFieldValue = sanitizeDecimalTextFieldValue(newValue, amountTextFieldValue)
                        },
                        label = { Text("Monto a Abonar ($) *") },
                        placeholder = { Text("0.00") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        isError = validationError != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (validationError != null) {
                        Text(
                            text = validationError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }

                // Método de Pago FilterChips
                Column {
                    Text(
                        text = "Método de Pago *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        paymentMethods.forEach { method ->
                            val isSelected = selectedPaymentMethodName.equals(method, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPaymentMethodName = method },
                                label = { Text(method) },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Payment,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // Notas / Observaciones
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notas / Observaciones (Opcional)") },
                    placeholder = { Text("Ej. Cheque #1234 o Recibo de pago") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Notes,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountDouble = amountTextFieldValue.text.toDoubleOrNull()
                    if (amountDouble == null || amountDouble <= 0.0) {
                        validationError = "Ingrese un monto válido mayor a 0"
                        return@Button
                    }
                    onConfirmPayment(selectedSaleId, amountDouble, selectedPaymentMethodName, notesText)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Registrar Abono")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismissRequest,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun AccountsReceivableSummaryCardPreview() {
    MovilPOSTheme {
        AccountsReceivableSummaryCard(
            totalAmount = 1250.75,
            debtorCount = 5,
            currencySymbol = "$",
            defaultDecimalPlaces = 2,
            allowExtraDecimals = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}
