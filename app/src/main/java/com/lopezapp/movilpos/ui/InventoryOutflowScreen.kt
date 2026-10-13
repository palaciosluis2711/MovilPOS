package com.lopezapp.movilpos.ui

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import com.lopezapp.movilpos.data.model.InventoryOutflow
import com.lopezapp.movilpos.data.model.OutflowItem
import com.lopezapp.movilpos.data.model.OutflowReason
import com.lopezapp.movilpos.data.model.toSpanishLabel
import com.lopezapp.movilpos.ui.components.BarcodeScannerDialog
import com.lopezapp.movilpos.ui.components.CompactSearchBar
import com.lopezapp.movilpos.ui.navigation.AppNavDisplay
import com.lopezapp.movilpos.ui.navigation.InventoryOutflowDetailRoute
import com.lopezapp.movilpos.ui.navigation.InventoryOutflowEditRoute
import com.lopezapp.movilpos.ui.navigation.InventoryOutflowsRoute
import com.lopezapp.movilpos.ui.viewmodel.InventoryOutflowViewModel
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.lopezapp.movilpos.util.PdfReportGenerator
import com.lopezapp.movilpos.util.formatCurrency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun InventoryOutflowScreen(
    viewModel: InventoryOutflowViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val backStack = rememberNavBackStack(InventoryOutflowsRoute)

    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

    AppNavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            } else {
                onNavigateBack()
            }
        },
        sceneStrategy = listDetailStrategy,
        modifier = modifier,
        settingsViewModel = settingsViewModel,
        entryProvider = entryProvider {
            entry<InventoryOutflowsRoute>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Selecciona una salida para ver los detalles")
                        }
                    }
                )
            ) {
                InventoryOutflowListScreen(
                    viewModel = viewModel,
                    settingsViewModel = settingsViewModel,
                    onNavigateBack = onNavigateBack,
                    onOutflowClick = { outflowId ->
                        backStack.add(InventoryOutflowDetailRoute(outflowId))
                    },
                    onCreateOutflowClick = {
                        viewModel.resetForm()
                        backStack.add(InventoryOutflowEditRoute)
                    }
                )
            }
            entry<InventoryOutflowDetailRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { route ->
                InventoryOutflowReadOnlyView(
                    outflowId = route.outflowId,
                    viewModel = viewModel,
                    settingsViewModel = settingsViewModel,
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
            }
            entry<InventoryOutflowEditRoute>(
                metadata = ListDetailSceneStrategy.extraPane()
            ) {
                InventoryOutflowEditForm(
                    viewModel = viewModel,
                    settingsViewModel = settingsViewModel,
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryOutflowListScreen(
    viewModel: InventoryOutflowViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onOutflowClick: (String) -> Unit,
    onCreateOutflowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val outflows by viewModel.outflows.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedReasonFilter by viewModel.selectedReasonFilter.collectAsState()

    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val currencySymbol = settingsUiState.currencySymbol
    val defaultDecimalPlaces = settingsUiState.defaultDecimalPlaces
    val allowExtraDecimals = settingsUiState.allowExtraDecimals

    val totalCostAll = remember(outflows) { outflows.sumOf { it.totalCost } }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Salidas y Bajas de Inventario",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateOutflowClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Registrar Salida", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Summary Card
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                            text = "Total Costo de Salidas",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = formatCurrency(totalCostAll, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Compact Search Bar
            CompactSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = "Buscar por nota, usuario o producto...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Filter Chips for OutflowReason
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedReasonFilter == null,
                    onClick = { viewModel.setReasonFilter(null) },
                    label = { Text("Todos") }
                )
                FilterChip(
                    selected = selectedReasonFilter == OutflowReason.CONSUMO_INTERNO,
                    onClick = { viewModel.setReasonFilter(OutflowReason.CONSUMO_INTERNO) },
                    label = { Text("Consumo Propio") }
                )
                FilterChip(
                    selected = selectedReasonFilter == OutflowReason.MERMA_DANADO,
                    onClick = { viewModel.setReasonFilter(OutflowReason.MERMA_DANADO) },
                    label = { Text("Mermas") }
                )
                FilterChip(
                    selected = selectedReasonFilter == OutflowReason.VENCIDO,
                    onClick = { viewModel.setReasonFilter(OutflowReason.VENCIDO) },
                    label = { Text("Vencidos") }
                )
                FilterChip(
                    selected = selectedReasonFilter == OutflowReason.MUESTRA_PROMOCION,
                    onClick = { viewModel.setReasonFilter(OutflowReason.MUESTRA_PROMOCION) },
                    label = { Text("Muestras") }
                )
                FilterChip(
                    selected = selectedReasonFilter == OutflowReason.AJUSTE_INVENTARIO,
                    onClick = { viewModel.setReasonFilter(OutflowReason.AJUSTE_INVENTARIO) },
                    label = { Text("Ajustes") }
                )
            }

            // List of Outflow items
            if (outflows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron salidas de inventario.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(outflows, key = { it.id }) { outflow ->
                        InventoryOutflowCard(
                            outflow = outflow,
                            currencySymbol = currencySymbol,
                            defaultDecimalPlaces = defaultDecimalPlaces,
                            allowExtraDecimals = allowExtraDecimals,
                            onClick = { onOutflowClick(outflow.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryOutflowCard(
    outflow: InventoryOutflow,
    currencySymbol: String,
    defaultDecimalPlaces: Int,
    allowExtraDecimals: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember(outflow.dateMillis) { dateFormat.format(Date(outflow.dateMillis)) }
    val itemsCount = remember(outflow.items) { outflow.items.sumOf { it.quantity } }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutflowReasonBadge(reason = outflow.reason)
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (outflow.notes.isNotBlank()) {
                Text(
                    text = outflow.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
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
                        text = "Prod: ${outflow.items.size} ($itemsCount uds)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (outflow.registeredByUserName.isNotBlank()) {
                        Text(
                            text = "Por: ${outflow.registeredByUserName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = formatCurrency(outflow.totalCost, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun OutflowReasonBadge(
    reason: OutflowReason,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (reason) {
        OutflowReason.CONSUMO_INTERNO -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        OutflowReason.MERMA_DANADO -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        OutflowReason.VENCIDO -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        OutflowReason.MUESTRA_PROMOCION -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        OutflowReason.AJUSTE_INVENTARIO -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = reason.toSpanishLabel(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryOutflowEditForm(
    viewModel: InventoryOutflowViewModel,
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val draftReason by viewModel.draftReason.collectAsState()
    val draftNotes by viewModel.draftNotes.collectAsState()
    val draftItems by viewModel.draftItems.collectAsState()
    val totalOutflowCost by viewModel.totalOutflowCost.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()

    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val currencySymbol = settingsUiState.currencySymbol
    val defaultDecimalPlaces = settingsUiState.defaultDecimalPlaces
    val allowExtraDecimals = settingsUiState.allowExtraDecimals

    var reasonDropdownExpanded by remember { mutableStateOf(false) }
    var productSearchText by remember { mutableStateOf("") }
    var productDropdownExpanded by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    val filteredProducts = remember(allProducts, productSearchText) {
        if (productSearchText.isBlank()) {
            allProducts.take(10)
        } else {
            allProducts.filter {
                it.name.contains(productSearchText, ignoreCase = true) ||
                        (it.barcode != null && it.barcode.contains(productSearchText, ignoreCase = true)) ||
                        it.category.contains(productSearchText, ignoreCase = true) ||
                        (it.differentiator?.contains(productSearchText, ignoreCase = true) == true) ||
                        it.tags.any { tag -> tag.contains(productSearchText, ignoreCase = true) }
            }.take(10)
        }
    }

    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            onDismissRequest = { showBarcodeScanner = false },
            onBarcodeScanned = { scannedCode ->
                showBarcodeScanner = false
                val matchingProduct = allProducts.find {
                    it.barcode == scannedCode || it.id == scannedCode
                }
                if (matchingProduct != null) {
                    viewModel.addDraftItem(matchingProduct, 1)
                    Toast.makeText(context, "Producto ${matchingProduct.name} agregado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Producto con código '$scannedCode' no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Registrar Salida de Inventario", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Motivo de Salida Dropdown / Picker
            Text(
                text = "Motivo de Salida",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            ExposedDropdownMenuBox(
                expanded = reasonDropdownExpanded,
                onExpandedChange = { reasonDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = draftReason.toSpanishLabel(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seleccionar Motivo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = reasonDropdownExpanded,
                    onDismissRequest = { reasonDropdownExpanded = false }
                ) {
                    OutflowReason.entries.forEach { reason ->
                        DropdownMenuItem(
                            text = { Text(reason.toSpanishLabel()) },
                            onClick = {
                                viewModel.setDraftReason(reason)
                                reasonDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Justificación / Nota Required Field
            OutlinedTextField(
                value = draftNotes,
                onValueChange = { viewModel.setDraftNotes(it) },
                label = { Text("Justificación / Nota *") },
                placeholder = { Text("Explique la razón de la baja o salida de inventario...") },
                isError = showErrors && draftNotes.isBlank(),
                supportingText = {
                    if (showErrors && draftNotes.isBlank()) {
                        Text("La justificación es requerida", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Requerido. Explique detalladamente el motivo de retiro.")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            HorizontalDivider()

            // Seleccionar Productos
            Text(
                text = "Seleccionar Productos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = productDropdownExpanded,
                    onExpandedChange = { productDropdownExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = productSearchText,
                        onValueChange = {
                            productSearchText = it
                            productDropdownExpanded = true
                        },
                        label = { Text("Buscar Producto") },
                        placeholder = { Text("Escriba nombre o código...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
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
                                    Column {
                                        Text(
                                            text = product.displayName,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Stock: ${product.stock} | Costo u.: ${formatCurrency(product.cost, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.addDraftItem(product, 1)
                                    productSearchText = ""
                                    productDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { showBarcodeScanner = true },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Escanear código de barras",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Tabla de Productos a Retirar
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tabla de Productos a Retirar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (draftItems.isEmpty()) {
                        Text(
                            text = "No se han seleccionado productos a retirar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        draftItems.forEach { item ->
                            OutflowDraftItemRow(
                                item = item,
                                currencySymbol = currencySymbol,
                                defaultDecimalPlaces = defaultDecimalPlaces,
                                allowExtraDecimals = allowExtraDecimals,
                                onQuantityChange = { newQty ->
                                    viewModel.updateDraftItemQuantity(item.productId, newQty)
                                },
                                onDeleteClick = {
                                    viewModel.removeDraftItem(item.productId)
                                }
                            )
                            HorizontalDivider()
                        }
                    }

                    // Summary Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Costo de Retiro:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatCurrency(totalOutflowCost, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Register Outflow Button
            Button(
                onClick = {
                    if (draftNotes.isBlank() || draftItems.isEmpty()) {
                        showErrors = true
                        Toast.makeText(context, "Complete los campos requeridos y agregue al menos un producto", Toast.LENGTH_SHORT).show()
                    } else {
                        if (viewModel.registerOutflow()) {
                            Toast.makeText(context, "Salida de inventario registrada con éxito", Toast.LENGTH_SHORT).show()
                            onNavigateUp()
                        }
                    }
                },
                enabled = draftNotes.isNotBlank() && draftItems.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar Salida de Inventario", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OutflowDraftItemRow(
    item: OutflowItem,
    currencySymbol: String,
    defaultDecimalPlaces: Int,
    allowExtraDecimals: Boolean,
    onQuantityChange: (Int) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var quantityText by remember(item.quantity) { mutableStateOf(item.quantity.toString()) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.productName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Costo u.: ${formatCurrency(item.unitCost, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)} | Subtotal: ${formatCurrency(item.totalCost, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(
                onClick = {
                    val newQty = item.quantity - 1
                    if (newQty <= 0) {
                        onDeleteClick()
                    } else {
                        onQuantityChange(newQty)
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Disminuir", modifier = Modifier.size(18.dp))
            }

            OutlinedTextField(
                value = quantityText,
                onValueChange = { newText ->
                    quantityText = newText
                    val parsed = newText.toIntOrNull()
                    if (parsed != null && parsed > 0) {
                        onQuantityChange(parsed)
                    }
                },
                modifier = Modifier
                    .width(56.dp)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            val parsed = quantityText.toIntOrNull()
                            if (parsed == null || parsed <= 0) {
                                quantityText = "1"
                                onQuantityChange(1)
                            }
                        }
                    },
                textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            IconButton(
                onClick = { onQuantityChange(item.quantity + 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aumentar", modifier = Modifier.size(18.dp))
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryOutflowReadOnlyView(
    outflowId: String,
    viewModel: InventoryOutflowViewModel,
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val outflow = remember(outflowId) { viewModel.getOutflowById(outflowId) }

    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val currencySymbol = settingsUiState.currencySymbol
    val defaultDecimalPlaces = settingsUiState.defaultDecimalPlaces
    val allowExtraDecimals = settingsUiState.allowExtraDecimals

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Salida", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (outflow == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Salida de inventario no encontrada.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
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
                            OutflowReasonBadge(reason = outflow.reason)
                            Text(
                                text = "ID: ${outflow.id.take(8).uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        DetailRow(label = "Fecha y Hora:", value = dateFormat.format(Date(outflow.dateMillis)))
                        if (outflow.registeredByUserName.isNotBlank()) {
                            DetailRow(label = "Registrado Por:", value = outflow.registeredByUserName)
                        }
                    }
                }

                // Justification Card
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Justificación / Nota",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = outflow.notes.ifBlank { "Sin nota especificada" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Items Table Card
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Productos Retirados (${outflow.items.sumOf { it.quantity }} uds)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        outflow.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.productName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Cant: ${item.quantity} x Costo u.: ${formatCurrency(item.unitCost, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = formatCurrency(item.totalCost, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            HorizontalDivider()
                        }

                        // Total Cost Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Costo Salida:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatCurrency(outflow.totalCost, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Ver / Imprimir Comprobante PDF Button
                Button(
                    onClick = {
                        try {
                            val businessInfo = settingsUiState.businessInfo
                            val pdfFile = PdfReportGenerator.generateOutflowPdf(
                                context = context,
                                outflow = outflow,
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
                            context.startActivity(Intent.createChooser(intent, "Ver Comprobante PDF"))
                            Toast.makeText(
                                context,
                                "Comprobante de salida generado exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(
                                context,
                                "Error al generar comprobante PDF: ${e.localizedMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver / Imprimir Comprobante PDF", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
