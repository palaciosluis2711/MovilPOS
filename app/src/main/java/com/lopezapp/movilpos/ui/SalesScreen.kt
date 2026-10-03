package com.lopezapp.movilpos.ui

import com.lopezapp.movilpos.ui.components.CompactSearchBar

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.ui.viewmodel.SalesViewModel
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import com.lopezapp.movilpos.util.PdfReportGenerator
import com.lopezapp.movilpos.util.formatCurrency
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesListScreen(
    viewModel: SalesViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCustomerIdFilter by viewModel.selectedCustomerIdFilter.collectAsState()
    val selectedInvoiceTypeFilter by viewModel.selectedInvoiceTypeFilter.collectAsState()
    val selectedPaymentMethodIdFilter by viewModel.selectedPaymentMethodIdFilter.collectAsState()
    val selectedDateFilterMillis by viewModel.selectedDateFilterMillis.collectAsState()

    val filteredSales by viewModel.filteredSales.collectAsState()
    val allCustomers by viewModel.allCustomers.collectAsState()
    val allPaymentMethods by viewModel.allPaymentMethods.collectAsState()

    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val currencySymbol = settingsUiState.currencySymbol
    val defaultDecimalPlaces = settingsUiState.defaultDecimalPlaces
    val allowExtraDecimals = settingsUiState.allowExtraDecimals

    var showDatePicker by remember { mutableStateOf(false) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var paymentMethodDropdownExpanded by remember { mutableStateOf(false) }

    val isFilterActive = searchQuery.isNotBlank() ||
            selectedCustomerIdFilter != null ||
            selectedInvoiceTypeFilter != null ||
            selectedPaymentMethodIdFilter != null ||
            selectedDateFilterMillis != null

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateFilterMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
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
                            viewModel.setSelectedDateFilterMillis(localCalendar.timeInMillis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
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
                title = { Text("Historial de Ventas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            CompactSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = "Cliente, ID o producto...",
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fecha Filter
                val formattedFilterDate = selectedDateFilterMillis?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                }
                FilterChip(
                    selected = selectedDateFilterMillis != null,
                    onClick = { showDatePicker = true },
                    label = { Text(formattedFilterDate?.let { "Fecha: $it" } ?: "Fecha") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (selectedDateFilterMillis != null) {
                            IconButton(
                                onClick = { viewModel.setSelectedDateFilterMillis(null) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Quitar filtro fecha")
                            }
                        }
                    }
                )

                // Cliente Filter
                Box {
                    val selectedCustomer = allCustomers.find { it.id == selectedCustomerIdFilter }
                    FilterChip(
                        selected = selectedCustomerIdFilter != null,
                        onClick = { customerDropdownExpanded = true },
                        label = { Text(selectedCustomer?.let { "Cliente: ${it.name}" } ?: "Cliente") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                    )
                    DropdownMenu(
                        expanded = customerDropdownExpanded,
                        onDismissRequest = { customerDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos los clientes") },
                            onClick = {
                                viewModel.setSelectedCustomerIdFilter(null)
                                customerDropdownExpanded = false
                            }
                        )
                        allCustomers.forEach { customer ->
                            DropdownMenuItem(
                                text = { Text(customer.name) },
                                onClick = {
                                    viewModel.setSelectedCustomerIdFilter(customer.id)
                                    customerDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Tipo de Comprobante Filter
                FilterChip(
                    selected = selectedInvoiceTypeFilter == null,
                    onClick = { viewModel.setSelectedInvoiceTypeFilter(null) },
                    label = { Text("Todos") }
                )
                FilterChip(
                    selected = selectedInvoiceTypeFilter == InvoiceType.CONSUMIDOR_FINAL,
                    onClick = {
                        viewModel.setSelectedInvoiceTypeFilter(
                            if (selectedInvoiceTypeFilter == InvoiceType.CONSUMIDOR_FINAL) null else InvoiceType.CONSUMIDOR_FINAL
                        )
                    },
                    label = { Text("Consumidor Final") }
                )
                FilterChip(
                    selected = selectedInvoiceTypeFilter == InvoiceType.CREDITO_FISCAL,
                    onClick = {
                        viewModel.setSelectedInvoiceTypeFilter(
                            if (selectedInvoiceTypeFilter == InvoiceType.CREDITO_FISCAL) null else InvoiceType.CREDITO_FISCAL
                        )
                    },
                    label = { Text("Crédito Fiscal") }
                )
                FilterChip(
                    selected = selectedInvoiceTypeFilter == InvoiceType.TICKET,
                    onClick = {
                        viewModel.setSelectedInvoiceTypeFilter(
                            if (selectedInvoiceTypeFilter == InvoiceType.TICKET) null else InvoiceType.TICKET
                        )
                    },
                    label = { Text("Ticket") }
                )

                // Método de Pago Filter
                Box {
                    val selectedPaymentMethod = allPaymentMethods.find { it.id == selectedPaymentMethodIdFilter }
                    FilterChip(
                        selected = selectedPaymentMethodIdFilter != null,
                        onClick = { paymentMethodDropdownExpanded = true },
                        label = { Text(selectedPaymentMethod?.let { "Pago: ${it.name}" } ?: "Método de Pago") },
                        leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                    )
                    DropdownMenu(
                        expanded = paymentMethodDropdownExpanded,
                        onDismissRequest = { paymentMethodDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos los métodos") },
                            onClick = {
                                viewModel.setSelectedPaymentMethodIdFilter(null)
                                paymentMethodDropdownExpanded = false
                            }
                        )
                        allPaymentMethods.forEach { pm ->
                            DropdownMenuItem(
                                text = { Text(pm.name) },
                                onClick = {
                                    viewModel.setSelectedPaymentMethodIdFilter(pm.id)
                                    paymentMethodDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Clear Filters Button
                if (isFilterActive) {
                    IconButton(onClick = { viewModel.resetFilters() }) {
                        Icon(Icons.Default.FilterAltOff, contentDescription = "Limpiar filtros", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Sales List
            if (filteredSales.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = if (isFilterActive) "No hay ventas que coincidan con los filtros" else "No hay ventas registradas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                        if (isFilterActive) {
                            Button(onClick = { viewModel.resetFilters() }) {
                                Text("Limpiar filtros")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSales, key = { it.id }) { sale ->
                        SaleCard(
                            sale = sale,
                            currencySymbol = currencySymbol,
                            defaultDecimalPlaces = defaultDecimalPlaces,
                            allowExtraDecimals = allowExtraDecimals,
                            onClick = { onNavigateToDetail(sale.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SaleCard(
    sale: Sale,
    currencySymbol: String,
    defaultDecimalPlaces: Int,
    allowExtraDecimals: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(sale.dateMillis))
    val itemCount = sale.items.sumOf { it.quantity }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: ID and Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Venta #${sale.id.take(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                InvoiceTypeBadge(invoiceType = sale.invoiceType)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Info rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = sale.customerName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = sale.paymentMethodName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• $itemCount prod.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Text(
                    text = formatCurrency(sale.totalAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun InvoiceTypeBadge(
    invoiceType: InvoiceType,
    modifier: Modifier = Modifier
) {
    val (label, containerColor, contentColor) = when (invoiceType) {
        InvoiceType.CONSUMIDOR_FINAL -> Triple("Consumidor Final", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        InvoiceType.CREDITO_FISCAL -> Triple("Crédito Fiscal", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        InvoiceType.TICKET -> Triple("Ticket", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleReadOnlyView(
    saleId: String,
    salesViewModel: SalesViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sale = salesViewModel.getSaleById(saleId)

    val allCustomers by salesViewModel.allCustomers.collectAsState()
    val customer = remember(sale, allCustomers) { allCustomers.find { it.id == sale?.customerId } }

    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val businessInfo = settingsUiState.businessInfo
    val ticketConfig = settingsUiState.ticketConfig
    val currencySymbol = settingsUiState.currencySymbol
    val defaultDecimalPlaces = settingsUiState.defaultDecimalPlaces
    val allowExtraDecimals = settingsUiState.allowExtraDecimals

    fun printOrViewTicketPdf() {
        if (sale == null) return
        try {
            val pdfFile = PdfReportGenerator.generateSaleTicketPdf(
                context = context,
                sale = sale,
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
            context.startActivity(Intent.createChooser(intent, "Ver / Imprimir Ticket PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al generar PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun viewFormalInvoicePdf() {
        if (sale == null) return
        try {
            val pdfFile = PdfReportGenerator.generateSaleInvoicePdf(
                context = context,
                sale = sale,
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
            context.startActivity(Intent.createChooser(intent, "Ver / Imprimir Factura Formal PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al generar Factura PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Venta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (sale != null) {
                        IconButton(onClick = { viewFormalInvoicePdf() }) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "Factura Formal PDF"
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (sale == null) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Venta no encontrada")
            }
        } else {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(sale.dateMillis))

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Información General
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Información General",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            InvoiceTypeBadge(invoiceType = sale.invoiceType)
                        }
                        HorizontalDivider()
                        Text(
                            text = "ID de Venta: ${sale.id}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Fecha y Hora: $formattedDate",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Facturación Electrónica (DTE - MH)
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Facturación Electrónica (DTE - MH)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider()

                        if (sale.isDteIssued) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estado MH:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Surface(
                                    color = Color(0xFFE8F5E9),
                                    contentColor = Color(0xFF2E7D32),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Procesado (Aprobado)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            val dteTypeDesc = when (sale.dteType) {
                                "01" -> "01 (Consumidor Final)"
                                "03" -> "03 (Crédito Fiscal)"
                                else -> sale.dteType ?: (if (sale.invoiceType == InvoiceType.CREDITO_FISCAL) "03 (Crédito Fiscal)" else "01 (Consumidor Final)")
                            }
                            Text(
                                text = "Tipo de DTE: $dteTypeDesc",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Código de Generación: ${sale.dteGenerationCode ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Sello de Recepción: ${sale.dteReceptionSeal ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Número de Control: ${sale.dteControlNumber ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estado MH:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "No Aplica (Ticket / Venta Local)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Cliente
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Cliente",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider()
                        Text(
                            text = "Nombre: ${sale.customerName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Método de Pago & Montos
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Información de Pago",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider()
                        Text(
                            text = "Método de Pago: ${sale.paymentMethodName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (sale.cashReceived > 0 || sale.paymentMethodName.contains("Efectivo", ignoreCase = true)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Efectivo Recibido:", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = formatCurrency(sale.cashReceived, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cambio Devuelto:", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = formatCurrency(sale.changeAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Tabla de Productos Vendidos
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Productos Vendidos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider()

                        // Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Producto",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Cant.",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(45.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "P.Unit",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(65.dp),
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = "Subtotal",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(75.dp),
                                textAlign = TextAlign.End
                            )
                        }

                        HorizontalDivider()

                        // Items List
                        sale.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.productName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = item.quantity.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.width(45.dp),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = formatCurrency(item.unitPrice, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.width(65.dp),
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    text = formatCurrency(item.subtotal, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.width(75.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }

                        HorizontalDivider()

                        // Total Row
                        if (sale.invoiceType == InvoiceType.CREDITO_FISCAL) {
                            val calculatedSubtotal = sale.totalAmount / 1.13
                            val calculatedTax = sale.totalAmount - calculatedSubtotal

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Subtotal (sin IVA):",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = formatCurrency(calculatedSubtotal, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "IVA (13%):",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = formatCurrency(calculatedTax, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOTAL FINAL:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatCurrency(sale.totalAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOTAL:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatCurrency(sale.totalAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Button: Ver / Imprimir Ticket
                Button(
                    onClick = { printOrViewTicketPdf() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ver / Imprimir Ticket",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
