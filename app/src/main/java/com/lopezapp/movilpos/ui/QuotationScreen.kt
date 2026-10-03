package com.lopezapp.movilpos.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.lopezapp.movilpos.ui.components.BarcodeScannerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.compose.animation.AnimatedContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lopezapp.movilpos.ui.navigation.buildNavTransition
import com.lopezapp.movilpos.data.model.PriceRule
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.Quotation
import com.lopezapp.movilpos.data.model.QuotationItem
import com.lopezapp.movilpos.data.model.calculatePriceWithoutTax
import com.lopezapp.movilpos.ui.components.CompactSearchBar
import com.lopezapp.movilpos.ui.theme.PriceRuleColors
import com.lopezapp.movilpos.ui.viewmodel.QuotationViewModel
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
fun QuotationListScreen(
    viewModel: QuotationViewModel,
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredQuotations by viewModel.filteredQuotations.collectAsState()

    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val currencySymbol = settingsUiState.currencySymbol
    val defaultDecimalPlaces = settingsUiState.defaultDecimalPlaces
    val allowExtraDecimals = settingsUiState.allowExtraDecimals

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cotizaciones de Oferta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.resetForm()
                    onNavigateToCreate()
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Cotización")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CompactSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = "Buscar por cliente, correo o producto..."
            )

            if (filteredQuotations.isEmpty()) {
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
                            imageVector = Icons.Default.RequestQuote,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "No se encontraron cotizaciones" else "No hay cotizaciones registradas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredQuotations, key = { it.id }) { quotation ->
                        QuotationCard(
                            quotation = quotation,
                            currencySymbol = currencySymbol,
                            defaultDecimalPlaces = defaultDecimalPlaces,
                            allowExtraDecimals = allowExtraDecimals,
                            onClick = { onNavigateToDetail(quotation.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuotationCard(
    quotation: Quotation,
    currencySymbol: String = "$",
    defaultDecimalPlaces: Int = 2,
    allowExtraDecimals: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val formattedIssueDate = remember(quotation.dateMillis) { dateFormat.format(Date(quotation.dateMillis)) }
    val formattedExpDate = remember(quotation.expirationDateMillis) { dateFormat.format(Date(quotation.expirationDateMillis)) }
    val isExpired = remember(quotation.expirationDateMillis) {
        quotation.expirationDateMillis < System.currentTimeMillis()
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = quotation.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = if (isExpired) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isExpired) "Vencida" else "Vigente",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isExpired) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            quotation.customerEmail?.takeIf { it.isNotBlank() }?.let { email ->
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Emisión: $formattedIssueDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Vence: $formattedExpDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(quotation.totalAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationReadOnlyView(
    quotationId: String,
    viewModel: QuotationViewModel,
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateToEdit: (String) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val quotation = viewModel.getQuotationById(quotationId)
    val businessInfo by viewModel.businessInfo.collectAsState()
    val taxes by viewModel.taxes.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val activeTaxLabel by viewModel.activeTaxLabel.collectAsState()

    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val currencySymbol = settingsUiState.currencySymbol
    val defaultDecimalPlaces = settingsUiState.defaultDecimalPlaces
    val allowExtraDecimals = settingsUiState.allowExtraDecimals

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (quotation == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Cotización") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Cotización no encontrada")
            }
        }
        return
    }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val formattedIssueDate = remember(quotation.dateMillis) { dateFormat.format(Date(quotation.dateMillis)) }
    val formattedExpDate = remember(quotation.expirationDateMillis) { dateFormat.format(Date(quotation.expirationDateMillis)) }
    val isExpired = remember(quotation.expirationDateMillis) {
        quotation.expirationDateMillis < System.currentTimeMillis()
    }

    val onShareQuotation: () -> Unit = {
        try {
            val pdfFile = PdfReportGenerator.generateQuotationPdf(context, quotation, businessInfo, taxes, products = allProducts)
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val shareText = "Estimado/a ${quotation.customerName},\n\n" +
                "Le saludamos cordialmente de parte de ${businessInfo.name.ifBlank { "nuestro equipo" }}. Nos complace compartirle la cotización correspondiente a su solicitud.\n\n" +
                "Adjunto encontrará el documento PDF con todos los detalles de la oferta.\n\n" +
                "Quedamos a su entera disposición para cualquier duda o consulta. ¡Gracias por su confianza!"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir Cotización"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error al compartir cotización: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Cotización") },
            text = { Text("¿Deseas eliminar permanentemente esta cotización?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteQuotation(quotation.id)
                        showDeleteDialog = false
                        Toast.makeText(context, "Cotización eliminada", Toast.LENGTH_SHORT).show()
                        onNavigateUp()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Cotización") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onShareQuotation) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                    IconButton(onClick = { onNavigateToEdit(quotation.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cotización N° ${quotation.id.take(8)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = if (isExpired) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isExpired) "Vencida" else "Vigente",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isExpired) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider()

                    Text(
                        text = "Cliente: ${quotation.customerName}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    quotation.customerEmail?.takeIf { it.isNotBlank() }?.let { email ->
                        Text(
                            text = "Correo: $email",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Emisión: $formattedIssueDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Vence: $formattedExpDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isExpired) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Items Section
            Text(
                text = "Productos Cotizados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    quotation.items.forEachIndexed { index, item ->
                        var itemUnitPrice = item.unitPrice
                        var itemSubtotal = item.quantity * item.unitPrice
                        
                        if (quotation.showTaxBreakdown) {
                            val product = allProducts.find { it.id == item.productId }
                            val applicableTaxes = if (product != null && product.appliedTaxIds.isNotEmpty()) {
                                taxes.filter { tax -> product.appliedTaxIds.contains(tax.id) }
                            } else {
                                taxes
                            }
                            itemUnitPrice = calculatePriceWithoutTax(item.unitPrice, applicableTaxes)
                            itemSubtotal = calculatePriceWithoutTax(itemSubtotal, applicableTaxes)
                        }

                        if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.productName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.quantity} x ${formatCurrency(itemUnitPrice, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (item.isDiscounted) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = item.appliedRuleName ?: "Regla de precio",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Text(
                                text = formatCurrency(itemSubtotal, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Total Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                if (quotation.showTaxBreakdown) {
                    val subtotal = quotation.totalAmount - quotation.taxAmount
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Subtotal:",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = formatCurrency(subtotal, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val taxLabelText = if (activeTaxLabel.endsWith(":")) activeTaxLabel else "$activeTaxLabel:"
                            Text(
                                text = taxLabelText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = formatCurrency(quotation.taxAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Final:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = formatCurrency(quotation.totalAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL COTIZACIÓN",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = formatCurrency(quotation.totalAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onShareQuotation,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir")
                }
                OutlinedButton(
                    onClick = {
                        try {
                            val pdfFile = PdfReportGenerator.generateQuotationPdf(context, quotation, businessInfo, taxes, products = allProducts)
                            val uri: Uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                pdfFile
                            )
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Abrir PDF de Cotización"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error al generar PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver PDF")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationEditForm(
    viewModel: QuotationViewModel,
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val currencySymbol = settingsUiState.currencySymbol
    val defaultDecimalPlaces = settingsUiState.defaultDecimalPlaces
    val allowExtraDecimals = settingsUiState.allowExtraDecimals
    val customerNameInput by viewModel.customerNameInput.collectAsState()
    val customerEmailInput by viewModel.customerEmailInput.collectAsState()
    val expirationDateMillis by viewModel.expirationDateMillis.collectAsState()
    val draftItems by viewModel.draftItems.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val showTaxBreakdown by viewModel.showTaxBreakdown.collectAsState()
    val showSignatureBlock by viewModel.showSignatureBlock.collectAsState()
    val showStampBlock by viewModel.showStampBlock.collectAsState()
    val showContactBlock by viewModel.showContactBlock.collectAsState()
    val calculatedTaxAmount by viewModel.calculatedTaxAmount.collectAsState()
    val activeTaxLabel by viewModel.activeTaxLabel.collectAsState()

    val selectedQuotationItemIds by viewModel.selectedQuotationItemIds.collectAsState()
    val selectedPriceRuleId by viewModel.selectedPriceRuleId.collectAsState()

    val editingQuotationId by viewModel.editingQuotationId.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()

    val allCustomers by viewModel.allCustomers.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val allPriceRules by viewModel.allPriceRules.collectAsState()

    var currentStep by rememberSaveable { mutableIntStateOf(1) }
    var productSearchText by remember { mutableStateOf("") }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    var isManualCustomer by rememberSaveable { mutableStateOf(false) }
    var isCustomerFocused by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCustomerDropdown by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }

    LaunchedEffect(allCustomers, editingQuotationId) {
        if (editingQuotationId == null && customerNameInput.isBlank() && selectedCustomer == null && allCustomers.isNotEmpty()) {
            val defaultCustomer = allCustomers.find { it.isDefault } ?: allCustomers.firstOrNull()
            if (defaultCustomer != null) {
                viewModel.selectCustomer(defaultCustomer)
            }
        }
    }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val formattedExpDate = remember(expirationDateMillis) { dateFormat.format(Date(expirationDateMillis)) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = expirationDateMillis
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
                                    23, 59, 59
                                )
                            }
                            viewModel.setExpirationDateMillis(localCalendar.timeInMillis)
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

    if (showScannerDialog) {
        BarcodeScannerDialog(
            onDismissRequest = { showScannerDialog = false },
            onBarcodeScanned = { scannedCode ->
                showScannerDialog = false
                val trimmedCode = scannedCode.trim()
                if (trimmedCode.isNotEmpty()) {
                    val matchingProduct = allProducts.find { product ->
                        product.barcode?.trim().equals(trimmedCode, ignoreCase = true)
                    }
                    if (matchingProduct != null) {
                        viewModel.addProductToDraft(matchingProduct)
                        productSearchText = ""
                        productDropdownExpanded = false
                        Toast.makeText(context, "Agregado: ${matchingProduct.name}", Toast.LENGTH_SHORT).show()
                    } else {
                        productSearchText = trimmedCode
                        productDropdownExpanded = true
                        Toast.makeText(context, "Filtrando por código: $trimmedCode", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (currentStep == 1) "Productos en Cotización (Paso 1 de 2)"
                        else "Detalles de la Cotización (Paso 2 de 2)"
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (currentStep == 2) {
                                currentStep = 1
                            } else {
                                onNavigateUp()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                if (currentStep == 1) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Monto Total",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = "${draftItems.sumOf { it.quantity }} item(s) en total",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                Text(
                                    text = formatCurrency(totalAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Button(
                            onClick = { currentStep = 2 },
                            enabled = draftItems.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Siguiente")
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = 1 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Anterior")
                            }

                            Button(
                                onClick = {
                                    val quotation = viewModel.saveQuotation()
                                    if (quotation != null) {
                                        Toast.makeText(context, "Cotización guardada exitosamente", Toast.LENGTH_SHORT).show()
                                        onNavigateUp()
                                    } else {
                                        Toast.makeText(context, "Por favor ingrese el nombre del cliente", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Guardar Cotización", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                buildNavTransition(
                    animationType = settingsUiState.animationType,
                    durationMs = settingsUiState.animationDurationMs,
                    isPop = targetState < initialState
                )
            },
            label = "QuotationEditStepTransition",
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) { step ->
            if (step == 1) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                // FIXED TOP HEADER SECTION
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Seleccionar Producto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val filteredProducts = remember(allProducts, productSearchText) {
                        if (productSearchText.isBlank()) {
                            allProducts
                        } else {
                            allProducts.filter { product ->
                                product.name.contains(productSearchText, ignoreCase = true) ||
                                        product.category.contains(productSearchText, ignoreCase = true) ||
                                        (product.barcode?.contains(productSearchText, ignoreCase = true) == true)
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = productDropdownExpanded && filteredProducts.isNotEmpty(),
                        onExpandedChange = { productDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = productSearchText,
                            onValueChange = {
                                productSearchText = it
                                productDropdownExpanded = true
                            },
                            label = { Text("Buscar producto para agregar") },
                            placeholder = { Text("Nombre, categoría o código...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { showScannerDialog = true }) {
                                        Icon(
                                            imageVector = Icons.Default.QrCodeScanner,
                                            contentDescription = "Escanear código de barras",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = productDropdownExpanded && filteredProducts.isNotEmpty()
                                    )
                                }
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            singleLine = true,
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryEditable)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = productDropdownExpanded && filteredProducts.isNotEmpty(),
                            onDismissRequest = { productDropdownExpanded = false }
                        ) {
                            filteredProducts.forEach { product ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = product.name,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "Stock: ${product.stock} | Cat: ${product.category}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = formatCurrency(product.price, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.addProductToDraft(product)
                                        productSearchText = ""
                                        productDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Compact Price Rules Capsules (Fixed in Top Header)
                    val activePriceRules = remember(allPriceRules) { allPriceRules.filter { it.isActive } }
                    if (activePriceRules.isNotEmpty()) {
                        PriceRuleCapsules(
                            priceRules = activePriceRules,
                            selectedPriceRuleId = selectedPriceRuleId,
                            onRuleSelected = { viewModel.selectPriceRule(it) },
                            isCompact = true
                        )
                    }

                    // Header for Quotation items list
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Productos en Cotización (${draftItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (draftItems.isNotEmpty()) {
                        val validSelectedCount = remember(draftItems, selectedQuotationItemIds) {
                            draftItems.count { selectedQuotationItemIds.contains(it.productId) }
                        }
                        val allSelected = draftItems.isNotEmpty() && validSelectedCount == draftItems.size

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    if (allSelected) viewModel.clearQuotationItemSelection() else viewModel.selectAllQuotationItems()
                                }
                            ) {
                                Checkbox(
                                    checked = allSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) viewModel.selectAllQuotationItems() else viewModel.clearQuotationItemSelection()
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val selectionText = if (validSelectedCount == 1) {
                                    "1 de ${draftItems.size} seleccionado"
                                } else {
                                    "$validSelectedCount de ${draftItems.size} seleccionados"
                                }
                                Text(
                                    text = if (validSelectedCount == 0) "Seleccionar todos" else selectionText,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (validSelectedCount > 0) {
                                TextButton(
                                    onClick = { viewModel.clearQuotationItemSelection() },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Text("Desmarcar todos", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // MIDDLE SECTION: INDIVIDUALLY SCROLLABLE LIST OF ADDED ITEMS
                if (draftItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No se han agregado productos. Busca un producto arriba o escanea su código para comenzar.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    val activePriceRules = remember(allPriceRules) { allPriceRules.filter { it.isActive } }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(draftItems, key = { it.productId }) { item ->
                            QuotationItemRow(
                                item = item,
                                isSelected = selectedQuotationItemIds.contains(item.productId),
                                availableRules = activePriceRules,
                                currencySymbol = currencySymbol,
                                defaultDecimalPlaces = defaultDecimalPlaces,
                                allowExtraDecimals = allowExtraDecimals,
                                onToggleSelection = { viewModel.toggleSelectQuotationItem(item.productId) },
                                onQuantityChange = { qty -> viewModel.updateDraftItemQuantity(item.productId, qty) },
                                onPriceChange = { price -> viewModel.updateDraftItemUnitPrice(item.productId, price) },
                                onApplyRule = { rule -> viewModel.applyPriceRuleToDraftItem(item.productId, rule) },
                                onRemoveRule = { viewModel.removePriceRuleFromDraftItem(item.productId) },
                                onRemoveItem = { viewModel.removeDraftItem(item.productId) }
                            )
                        }
                    }
                }
            }
        } else {
            // STEP 2: Detalles de la Cotización
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Información del Cliente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isManualCustomer = !isManualCustomer }
                ) {
                    Checkbox(
                        checked = isManualCustomer,
                        onCheckedChange = { isManualCustomer = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ingresar cliente manualmente",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (!isManualCustomer) {
                    val filteredCustomers = remember(allCustomers, customerNameInput) {
                        if (customerNameInput.isBlank()) {
                            allCustomers
                        } else {
                            allCustomers.filter { customer ->
                                customer.name.contains(customerNameInput, ignoreCase = true) ||
                                        (customer.email?.contains(customerNameInput, ignoreCase = true) == true)
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = showCustomerDropdown,
                        onExpandedChange = { expanded ->
                            showCustomerDropdown = expanded
                            if (!expanded) {
                                viewModel.handleCustomerFallback()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = customerNameInput,
                            onValueChange = {
                                viewModel.setCustomerName(it)
                                showCustomerDropdown = true
                            },
                            label = { Text("Nombre del cliente *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = showCustomerDropdown
                                )
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            singleLine = true,
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryEditable)
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (isCustomerFocused && !focusState.isFocused) {
                                        viewModel.handleCustomerFallback()
                                    }
                                    isCustomerFocused = focusState.isFocused
                                }
                        )

                        ExposedDropdownMenu(
                            expanded = showCustomerDropdown,
                            onDismissRequest = {
                                showCustomerDropdown = false
                                viewModel.handleCustomerFallback()
                            }
                        ) {
                            if (filteredCustomers.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No se encontraron clientes") },
                                    onClick = {
                                        showCustomerDropdown = false
                                        viewModel.handleCustomerFallback()
                                    }
                                )
                            } else {
                                filteredCustomers.forEach { customer ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${customer.name}${if (!customer.email.isNullOrBlank()) " (${customer.email})" else ""}"
                                            )
                                        },
                                        onClick = {
                                            viewModel.selectCustomer(customer)
                                            showCustomerDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customerNameInput,
                        onValueChange = { viewModel.setCustomerName(it) },
                        label = { Text("Nombre del cliente *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customerEmailInput,
                        onValueChange = { viewModel.setCustomerEmail(it) },
                        label = { Text("Correo electrónico (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }

                // Vigencia / Expiration Date Picker
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Vigencia de la Cotización",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = formattedExpDate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = { showDatePicker = true }
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cambiar")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(7, 15, 30).forEach { days ->
                                val isSelected = remember<Boolean>(expirationDateMillis, days) {
                                    isDaysFromToday(expirationDateMillis, days)
                                }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setExpirationDaysFromToday(days) },
                                    label = { Text("$days días") }
                                )
                            }
                        }
                    }
                }

                // Tax breakdown option
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setShowTaxBreakdown(!showTaxBreakdown) }
                            .padding(12.dp)
                    ) {
                        Checkbox(
                            checked = showTaxBreakdown,
                            onCheckedChange = { viewModel.setShowTaxBreakdown(it) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Desglose de Impuestos",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Calcular e incluir desglose de impuestos en la cotización",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Opciones de Impresión
                Text(
                    text = "Opciones de Impresión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setShowSignatureBlock(!showSignatureBlock) }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = showSignatureBlock,
                                onCheckedChange = { viewModel.setShowSignatureBlock(it) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Firma del ofertante",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setShowStampBlock(!showStampBlock) }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = showStampBlock,
                                onCheckedChange = { viewModel.setShowStampBlock(it) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sello del ofertante",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setShowContactBlock(!showContactBlock) }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = showContactBlock,
                                onCheckedChange = { viewModel.setShowContactBlock(it) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Información de contacto del ofertante",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Summary card displaying total items and total quotation amount
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Resumen de Cotización",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total de ítems:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${draftItems.sumOf { it.quantity }} unidad(es) (${draftItems.size} producto(s))",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        if (showTaxBreakdown) {
                            val subtotal = totalAmount - calculatedTaxAmount
                            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Subtotal:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = formatCurrency(subtotal, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val taxLabelText = if (activeTaxLabel.endsWith(":")) activeTaxLabel else "$activeTaxLabel:"
                                Text(
                                    text = taxLabelText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = formatCurrency(calculatedTaxAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Final:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = formatCurrency(totalAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Monto Total:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = formatCurrency(totalAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun QuotationItemRow(
    item: QuotationItem,
    isSelected: Boolean,
    availableRules: List<PriceRule>,
    currencySymbol: String = "$",
    defaultDecimalPlaces: Int = 2,
    allowExtraDecimals: Boolean = true,
    onToggleSelection: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onPriceChange: (Double) -> Unit,
    onApplyRule: (PriceRule) -> Unit,
    onRemoveRule: () -> Unit,
    onRemoveItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRuleDropdown by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() },
                modifier = Modifier.padding(end = 4.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Item Header: Product Name + Delete Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.productName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onToggleSelection() }
                    )

                    IconButton(
                        onClick = onRemoveItem,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar item",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Discount Badge / Rule Picker Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (item.isDiscounted) {
                        Surface(
                            onClick = onRemoveRule,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = item.appliedRuleName ?: "Dcto",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Quitar descuento",
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    } else if (availableRules.isNotEmpty()) {
                        Box {
                            TextButton(
                                onClick = { showRuleDropdown = true },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Aplicar regla", style = MaterialTheme.typography.labelSmall)
                            }

                            DropdownMenu(
                                expanded = showRuleDropdown,
                                onDismissRequest = { showRuleDropdown = false }
                            ) {
                                availableRules.forEach { rule ->
                                    DropdownMenuItem(
                                        text = { Text("${rule.name} (${rule.formulaRepresentation()})") },
                                        onClick = {
                                            onApplyRule(rule)
                                            showRuleDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Quantity Selector & Price/Subtotal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quantity Selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { onQuantityChange(item.quantity - 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Restar", modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = item.quantity.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        IconButton(
                            onClick = { onQuantityChange(item.quantity + 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Sumar", modifier = Modifier.size(16.dp))
                        }
                    }

                    // Price and Subtotal
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "P. U.: ${formatCurrency(item.unitPrice, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Subtotal: ${formatCurrency(item.quantity * item.unitPrice, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private fun isDaysFromToday(expMillis: Long, days: Int): Boolean {
    val targetCal = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, days)
    }
    val expCal = Calendar.getInstance().apply {
        timeInMillis = expMillis
    }
    return targetCal.get(Calendar.YEAR) == expCal.get(Calendar.YEAR) &&
            targetCal.get(Calendar.DAY_OF_YEAR) == expCal.get(Calendar.DAY_OF_YEAR)
}
