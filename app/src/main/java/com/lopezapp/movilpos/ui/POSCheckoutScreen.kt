package com.lopezapp.movilpos.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lopezapp.movilpos.data.model.DteEnvironment
import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.ui.viewmodel.POSViewModel
import com.lopezapp.movilpos.ui.viewmodel.SettingsUiState
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import com.lopezapp.movilpos.util.formatCurrency
import com.lopezapp.movilpos.util.sanitizeDecimalTextFieldValue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun POSCheckoutScreen(
    viewModel: POSViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel? = null,
    onNavigateToReceipt: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDteEmitting by viewModel.isDteEmitting.collectAsState()
    val dteStatusMessage by viewModel.dteStatusMessage.collectAsState()
    val settingsUiState = settingsViewModel?.uiState?.collectAsState()?.value
        ?: SettingsUiState()
    val currencySymbol = settingsUiState.currencySymbol
    val defaultDecimalPlaces = settingsUiState.defaultDecimalPlaces
    val allowExtraDecimals = settingsUiState.allowExtraDecimals

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var showCreditDatePicker by remember { mutableStateOf(false) }

    // Synchronize local TextFieldValue with viewModel.cashReceivedStr and pre-fill with total
    val cashReceivedStr by viewModel.cashReceivedStr.collectAsState()

    LaunchedEffect(Unit) {
        val formattedTotal = String.format(Locale.US, "%.2f", uiState.total)
        viewModel.setCashReceivedStr(formattedTotal)
    }

    var cashTextFieldValue by remember {
        val initialText = cashReceivedStr.ifEmpty { String.format(Locale.US, "%.2f", uiState.total) }
        mutableStateOf(TextFieldValue(initialText, selection = TextRange(initialText.length)))
    }

    LaunchedEffect(cashReceivedStr) {
        if (cashTextFieldValue.text != cashReceivedStr) {
            cashTextFieldValue = TextFieldValue(
                text = cashReceivedStr,
                selection = TextRange(cashReceivedStr.length)
            )
        }
    }

    val selectedCustomer = uiState.customers.find { it.id == uiState.selectedCustomerId }
        ?: uiState.customers.find { it.isDefault }
        ?: uiState.customers.firstOrNull()

    var customerSearchText by remember { mutableStateOf(selectedCustomer?.name ?: "") }
    var isCustomerFocused by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCustomer?.id, selectedCustomer?.name) {
        if ((selectedCustomer != null) && (customerSearchText != selectedCustomer.name)) {
            customerSearchText = selectedCustomer.name
        }
    }

    val handleCustomerFallback = {
        val defaultCustomer = uiState.customers.find { it.isDefault } ?: uiState.customers.firstOrNull()
        val query = customerSearchText.trim()
        val matchingCustomer = if (query.isNotEmpty()) {
            uiState.customers.find { it.name.equals(query, ignoreCase = true) }
        } else null

        if (query.isEmpty() || matchingCustomer == null) {
            if (defaultCustomer != null) {
                viewModel.selectCustomer(defaultCustomer.id)
                customerSearchText = defaultCustomer.name
            }
        } else {
            viewModel.selectCustomer(matchingCustomer.id)
            customerSearchText = matchingCustomer.name
        }
    }

    val filteredCustomers = remember(uiState.customers, customerSearchText, selectedCustomer) {
        if (customerSearchText.isBlank() || customerSearchText == selectedCustomer?.name) {
            uiState.customers
        } else {
            val query = customerSearchText.trim()
            uiState.customers.filter { customer ->
                customer.name.contains(query, ignoreCase = true) ||
                        customer.documentNumber.contains(query, ignoreCase = true)
            }
        }
    }

    val selectedPaymentMethod = uiState.paymentMethods.find { it.id == uiState.selectedPaymentMethodId }
        ?: uiState.paymentMethods.find { it.isDefault }
        ?: uiState.paymentMethods.firstOrNull()

    val isCustomerValidForCredit = selectedCustomer != null &&
            !selectedCustomer.isDefault &&
            !selectedCustomer.name.trim().equals("Cliente General", ignoreCase = true)

    val isCash = !uiState.isCreditSale && ((selectedPaymentMethod == null) || selectedPaymentMethod.name.contains("Efectivo", ignoreCase = true))
    val cashReceived = cashReceivedStr.toDoubleOrNull() ?: 0.0
    val totalAmount = uiState.total
    val isCashInsufficient = isCash && cashReceivedStr.trim().isNotEmpty() && (cashReceived < totalAmount)

    if (showCreditDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.creditDueDateMillis ?: (System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L)
        )
        DatePickerDialog(
            onDismissRequest = { showCreditDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = utcMillis
                            }
                            val localCalendar = Calendar.getInstance().apply {
                                set(
                                    utcCalendar.get(Calendar.YEAR),
                                    utcCalendar.get(Calendar.MONTH),
                                    utcCalendar.get(Calendar.DAY_OF_MONTH),
                                    12, 0, 0
                                )
                            }
                            viewModel.setCreditDueDateMillis(localCalendar.timeInMillis)
                        }
                        showCreditDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreditDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cobro / Finalizar Venta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Prominent Monto Total Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Monto Total a Pagar",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatCurrency(uiState.total, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    val itemCount = uiState.cartItems.sumOf { it.quantity }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (itemCount == 1) "1 producto en el carrito" else "$itemCount productos en el carrito",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Cliente Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cliente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = customerDropdownExpanded,
                    onExpandedChange = { customerDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = customerSearchText,
                        onValueChange = { newValue ->
                            customerSearchText = newValue
                            customerDropdownExpanded = true
                        },
                        label = { Text("Cliente") },
                        placeholder = { Text("Buscar cliente por nombre o documento...") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable)
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (isCustomerFocused && !focusState.isFocused) {
                                    handleCustomerFallback()
                                }
                                isCustomerFocused = focusState.isFocused
                            }
                    )

                    ExposedDropdownMenu(
                        expanded = customerDropdownExpanded,
                        onDismissRequest = {
                            customerDropdownExpanded = false
                            handleCustomerFallback()
                        }
                    ) {
                        if (filteredCustomers.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No se encontraron clientes") },
                                onClick = {
                                    customerDropdownExpanded = false
                                    handleCustomerFallback()
                                }
                            )
                        } else {
                            filteredCustomers.forEach { customer ->
                                val isSelected = customer.id == selectedCustomer?.id
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = customer.name,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                text = "${customer.documentType}: ${customer.documentNumber}" +
                                                        if (customer.isDefault) " (Por defecto)" else "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectCustomer(customer.id)
                                        customerSearchText = customer.name
                                        customerDropdownExpanded = false
                                    },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                }

                if (uiState.isCreditSale && !isCustomerValidForCredit) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Las ventas a crédito requieren seleccionar un cliente específico (no se permite Cliente General).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Tipo de Comprobante / Factura Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tipo de Comprobante / Factura",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InvoiceType.entries.forEach { invoiceType ->
                        val isSelected = uiState.selectedInvoiceType == invoiceType
                        val label = when (invoiceType) {
                            InvoiceType.CONSUMIDOR_FINAL -> "Consumidor Final"
                            InvoiceType.CREDITO_FISCAL -> "Crédito Fiscal"
                            InvoiceType.TICKET -> "Ticket"
                        }
                        val isEbEnabled = uiState.electronicBillingConfig.isEnabled
                        val isChipEnabled = when (invoiceType) {
                            InvoiceType.CONSUMIDOR_FINAL, InvoiceType.CREDITO_FISCAL -> isEbEnabled
                            InvoiceType.TICKET -> true
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectInvoiceType(invoiceType) },
                            enabled = isChipEnabled,
                            label = { Text(label) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Rounded.Check, contentDescription = null) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                if (!uiState.electronicBillingConfig.isEnabled) {
                    Text(
                        text = "Consumidor Final y Crédito Fiscal requieren habilitar Facturación Electrónica en Ajustes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                if (uiState.selectedInvoiceType == InvoiceType.CONSUMIDOR_FINAL ||
                    uiState.selectedInvoiceType == InvoiceType.CREDITO_FISCAL
                ) {
                    val environmentText = when (uiState.electronicBillingConfig.environment) {
                        DteEnvironment.SANDBOX -> "Ambiente DTE activo: Pruebas (Sandbox)"
                        DteEnvironment.PRODUCTION -> "Ambiente DTE activo: Producción"
                    }
                    Text(
                        text = environmentText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
            }

            // Método de Pago Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Método de Pago",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.paymentMethods.forEach { pm ->
                        val isSelected = !uiState.isCreditSale && selectedPaymentMethod?.id == pm.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.setIsCreditSale(false)
                                viewModel.selectPaymentMethod(pm.id)
                            },
                            label = { Text(pm.name) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Rounded.Check, contentDescription = null) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }

                    val isCreditSelected = uiState.isCreditSale
                    FilterChip(
                        selected = isCreditSelected,
                        onClick = {
                            viewModel.setIsCreditSale(!isCreditSelected)
                        },
                        label = { Text("Crédito / A Cuenta") },
                        leadingIcon = if (isCreditSelected) {
                            { Icon(Icons.Rounded.Check, contentDescription = null) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                }
            }

            // Fecha Límite de Pago Section (Visible when Crédito / A Cuenta is selected)
            AnimatedVisibility(
                visible = uiState.isCreditSale,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fecha Límite de Pago",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    val currentDueDate = uiState.creditDueDateMillis ?: (System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L)
                    val formattedDueDate = remember(currentDueDate) {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(currentDueDate))
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 30, 60).forEach { days ->
                            SuggestionChip(
                                onClick = {
                                    val targetMillis = System.currentTimeMillis() + days * 24L * 60 * 60 * 1000L
                                    viewModel.setCreditDueDateMillis(targetMillis)
                                },
                                label = {
                                    Text(
                                        text = "$days días",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = formattedDueDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha de Vencimiento") },
                        trailingIcon = {
                            IconButton(onClick = { showCreditDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarToday,
                                    contentDescription = "Seleccionar Fecha"
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCreditDatePicker = true }
                    )
                }
            }

            // Section "Dinero Rápido" (Visible when Efectivo is selected and NOT credit sale)
            AnimatedVisibility(
                visible = isCash,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Dinero Rápido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Quick Cash Chips: Exacto, $5.00, $10.00, $20.00, $50.00, $100.00
                    val quickCashValues = listOf(
                        "Exacto" to null,
                        "$5.00" to 5.0,
                        "$10.00" to 10.0,
                        "$20.00" to 20.0,
                        "$50.00" to 50.0,
                        "$100.00" to 100.0
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickCashValues.forEach { (label, value) ->
                            val isEnabled = value == null || value >= uiState.total
                            SuggestionChip(
                                onClick = {
                                    val formatted = if (value == null) {
                                        String.format(Locale.US, "%.2f", uiState.total)
                                    } else if (value % 1.0 == 0.0) {
                                        value.toInt().toString()
                                    } else {
                                        String.format(Locale.US, "%.2f", value)
                                    }
                                    viewModel.setCashReceivedStr(formatted)
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                enabled = isEnabled,
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }

                    // Efectivo Recibido TextField
                    OutlinedTextField(
                        value = cashTextFieldValue,
                        onValueChange = { newValue ->
                            val sanitized = sanitizeDecimalTextFieldValue(newValue, cashTextFieldValue)
                            cashTextFieldValue = sanitized
                            viewModel.setCashReceivedStr(sanitized.text)
                        },
                        label = { Text("Efectivo Recibido") },
                        prefix = { Text("$currencySymbol ") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        isError = isCashInsufficient,
                        supportingText = if (isCashInsufficient) {
                            {
                                Text(
                                    text = "El efectivo recibido es insuficiente para completar la venta",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else null
                    )

                    // Cambio / Vuelto Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cambio / Vuelto",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = formatCurrency(uiState.changeAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val isButtonEnabled = uiState.cartItems.isNotEmpty() &&
                    if (uiState.isCreditSale) isCustomerValidForCredit else (!isCash || cashReceived >= totalAmount)
            val buttonText = if (uiState.isCreditSale) "Registrar Venta a Crédito" else "Completar Cobro"

            // Completar Cobro / Registrar Venta a Crédito Button
            Button(
                onClick = {
                    val sale = viewModel.processSale(context = context) { saleId ->
                        onNavigateToReceipt(saleId)
                    }
                    if (sale != null) {
                        Toast.makeText(context, "Venta realizada con éxito", Toast.LENGTH_SHORT).show()
                        onNavigateToReceipt(sale.id)
                    }
                },
                enabled = isButtonEnabled,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isDteEmitting) {
                Dialog(
                    onDismissRequest = { },
                    properties = DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false
                    )
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Emitiendo Documento Tributario Electrónico - MH El Salvador",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            if (dteStatusMessage.isNotBlank()) {
                                Text(
                                    text = dteStatusMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
