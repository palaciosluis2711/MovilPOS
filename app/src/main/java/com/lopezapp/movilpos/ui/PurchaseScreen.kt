package com.lopezapp.movilpos.ui

import com.lopezapp.movilpos.ui.components.BarcodeScannerDialog
import com.lopezapp.movilpos.ui.components.CompactSearchBar
import com.lopezapp.movilpos.ui.components.MultiSelectionTopAppBar
import com.lopezapp.movilpos.ui.components.BulkDeleteConfirmationDialog
import androidx.compose.material3.Checkbox

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.Purchase
import com.lopezapp.movilpos.data.model.PurchaseItem
import com.lopezapp.movilpos.data.model.Supplier
import com.lopezapp.movilpos.ui.navigation.AppNavDisplay
import com.lopezapp.movilpos.ui.navigation.PurchaseDetailRoute
import com.lopezapp.movilpos.ui.navigation.PurchaseEditRoute
import com.lopezapp.movilpos.ui.navigation.PurchasesRoute
import com.lopezapp.movilpos.ui.viewmodel.PurchaseViewModel
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import com.lopezapp.movilpos.util.PdfReportGenerator
import com.lopezapp.movilpos.util.formatCurrency
import com.lopezapp.movilpos.util.sanitizeDecimalTextFieldValue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun formatPurchaseDate(dateMillis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(dateMillis))
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun PurchaseScreen(
    viewModel: PurchaseViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val backStack = rememberNavBackStack(PurchasesRoute)

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
            entry<PurchasesRoute>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Selecciona una compra para ver los detalles")
                        }
                    }
                )
            ) {
                PurchaseListScreen(
                    viewModel = viewModel,
                    onNavigateBack = onNavigateBack,
                    onPurchaseClick = { purchaseId ->
                        backStack.add(PurchaseDetailRoute(purchaseId))
                    },
                    onCreatePurchaseClick = {
                        viewModel.resetForm()
                        backStack.add(PurchaseEditRoute())
                    }
                )
            }
            entry<PurchaseDetailRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { route ->
                PurchaseReadOnlyView(
                    purchaseId = route.purchaseId,
                    viewModel = viewModel,
                    onNavigateUp = { backStack.removeLastOrNull() },
                    onNavigateToEdit = { editId -> backStack.add(PurchaseEditRoute(editId)) }
                )
            }
            entry<PurchaseEditRoute>(
                metadata = ListDetailSceneStrategy.extraPane()
            ) { route ->
                PurchaseEditForm(
                    purchaseId = route.purchaseId,
                    viewModel = viewModel,
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PurchaseListScreen(
    viewModel: PurchaseViewModel,
    onNavigateBack: () -> Unit,
    onPurchaseClick: (String) -> Unit,
    onCreatePurchaseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredPurchases by viewModel.filteredPurchases.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSupplierIdFilter by viewModel.selectedSupplierIdFilter.collectAsState()
    val selectedDateFilterMillis by viewModel.selectedDateFilterMillis.collectAsState()

    val allSuppliers by viewModel.allSuppliers.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var supplierDropdownExpanded by remember { mutableStateOf(false) }

    var selectedIds by rememberSaveable { mutableStateOf(setOf<String>()) }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }

    val isFilterActive = searchQuery.isNotBlank() ||
            selectedSupplierIdFilter != null ||
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
            if (selectedIds.isNotEmpty()) {
                MultiSelectionTopAppBar(
                    selectedCount = selectedIds.size,
                    onClearSelection = { selectedIds = emptySet() },
                    onSelectAll = { selectedIds = filteredPurchases.map { it.id }.toSet() },
                    onDeselectAll = { selectedIds = emptySet() },
                    onDeleteSelected = { showBulkDeleteDialog = true }
                )
            } else {
                TopAppBar(
                    title = { Text("Compras") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (selectedIds.isEmpty()) {
                FloatingActionButton(onClick = onCreatePurchaseClick) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Compra")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            CompactSearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                placeholder = "Buscar compra por ID, proveedor o producto...",
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Filter Chips Bar
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

                // Proveedor Filter
                Box {
                    val selectedSupplier = allSuppliers.find { it.id == selectedSupplierIdFilter }
                    FilterChip(
                        selected = selectedSupplierIdFilter != null,
                        onClick = { supplierDropdownExpanded = true },
                        label = { Text(selectedSupplier?.let { "Proveedor: ${it.name}" } ?: "Proveedor") },
                        leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                    )
                    DropdownMenu(
                        expanded = supplierDropdownExpanded,
                        onDismissRequest = { supplierDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos los proveedores") },
                            onClick = {
                                viewModel.setSelectedSupplierIdFilter(null)
                                supplierDropdownExpanded = false
                            }
                        )
                        allSuppliers.forEach { supplier ->
                            DropdownMenuItem(
                                text = { Text(supplier.name) },
                                onClick = {
                                    viewModel.setSelectedSupplierIdFilter(supplier.id)
                                    supplierDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Reset Filters Button
                if (isFilterActive) {
                    IconButton(onClick = { viewModel.resetFilters() }) {
                        Icon(
                            Icons.Default.FilterAltOff,
                            contentDescription = "Limpiar filtros",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (filteredPurchases.isEmpty()) {
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
                        Text(
                            text = if (isFilterActive) "No hay compras que coincidan con los filtros" else "No hay compras registradas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredPurchases, key = { it.id }) { purchase ->
                        val isSelected = selectedIds.contains(purchase.id)
                        PurchaseItemCard(
                            purchase = purchase,
                            isSelected = isSelected,
                            onClick = {
                                if (selectedIds.isNotEmpty()) {
                                    selectedIds = if (isSelected) selectedIds - purchase.id else selectedIds + purchase.id
                                } else {
                                    onPurchaseClick(purchase.id)
                                }
                            },
                            onToggleSelect = {
                                selectedIds = if (isSelected) selectedIds - purchase.id else selectedIds + purchase.id
                            },
                            onLongClick = {
                                if (selectedIds.isEmpty()) {
                                    selectedIds = selectedIds + purchase.id
                                }
                            }
                        )
                    }
                }
            }
        }
        
        if (showBulkDeleteDialog) {
            BulkDeleteConfirmationDialog(
                selectedCount = selectedIds.size,
                onConfirm = {
                    viewModel.deletePurchases(selectedIds)
                    selectedIds = emptySet()
                    showBulkDeleteDialog = false
                },
                onDismiss = { showBulkDeleteDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PurchaseItemCard(
    purchase: Purchase,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onToggleSelect: () -> Unit = onClick,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardDefaults.elevatedShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
        )
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() }
                    )
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = purchase.supplierName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = formatPurchaseDate(purchase.dateMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (purchase.isAutoSystemPurchase) {
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D))
                ) {
                    Text(
                        text = "Auto-Compra (Sistema)",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${purchase.items.sumOf { it.quantity }} producto(s)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(purchase.totalCost),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseReadOnlyView(
    purchaseId: String,
    viewModel: PurchaseViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToEdit: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val purchase = viewModel.getPurchaseById(purchaseId)
    val supplier = purchase?.let { viewModel.getSupplierById(it.supplierId) }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog && purchase != null) {
        val deleteMessage = if (purchase.isAutoSystemPurchase) {
            "¿Está seguro de eliminar esta auto-compra? \n\nNota: Por ser una auto-compra del sistema, eliminarla no afectará el inventario."
        } else {
            "¿Está seguro de eliminar esta compra? \n\nADVERTENCIA: Esta acción descontará del inventario las cantidades que habían sido ingresadas originalmente."
        }
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(if (purchase.isAutoSystemPurchase) "Eliminar Auto-Compra" else "Revertir / Eliminar Compra") },
            text = { Text(deleteMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deletePurchase(purchaseId)
                        val toastText = if (purchase.isAutoSystemPurchase) "Auto-compra eliminada" else "Compra eliminada y stock revertido"
                        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                        onNavigateUp()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (purchase.isAutoSystemPurchase) "Sí, Eliminar" else "Sí, Revertir y Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Compra") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (purchase != null) {
                        if (!purchase.isAutoSystemPurchase) {
                            IconButton(
                                onClick = { onNavigateToEdit?.invoke(purchaseId) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar Compra"
                                )
                            }
                        }
                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar Compra",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        IconButton(
                            onClick = {
                                try {
                                    val pdfFile = PdfReportGenerator.generatePurchasePdf(
                                        context = context,
                                        purchase = purchase,
                                        supplier = supplier
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
                                    context.startActivity(Intent.createChooser(intent, "Ver PDF"))
                                    Toast.makeText(
                                        context,
                                        "Reporte PDF generado exitosamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        context,
                                        "Error al generar PDF: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "Ver PDF"
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (purchase == null) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Compra no encontrada")
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Información General",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Proveedor:",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = purchase.supplierName)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Fecha:",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = formatPurchaseDate(purchase.dateMillis))
                        }
                        if (purchase.isAutoSystemPurchase) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tipo:",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Surface(
                                    color = Color(0xFFFFF3E0),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFFB74D))
                                ) {
                                    Text(
                                        text = "Auto-Compra (Sistema)",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFE65100),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Productos Comprados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Table Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Producto",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(2f)
                            )
                            Text(
                                text = "Cant.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Costo U.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1.5f)
                            )
                            Text(
                                text = "Subtotal",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1.5f)
                            )
                        }

                        HorizontalDivider()

                        purchase.items.forEach { item ->
                            val subtotal = item.quantity * item.unitCost
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.productName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(2f)
                                )
                                Text(
                                    text = "${item.quantity}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = formatCurrency(item.unitCost),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1.5f)
                                )
                                Text(
                                    text = formatCurrency(subtotal),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1.5f)
                                )
                            }
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL COMPRA",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatCurrency(purchase.totalCost),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!purchase.isAutoSystemPurchase) {
                        OutlinedButton(
                            onClick = { onNavigateToEdit?.invoke(purchaseId) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Editar Compra")
                        }
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar Compra")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseEditForm(
    viewModel: PurchaseViewModel,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    purchaseId: String? = null
) {
    val context = LocalContext.current
    val editingPurchaseId by viewModel.editingPurchaseId.collectAsState()

    LaunchedEffect(purchaseId) {
        if (purchaseId != null) {
            viewModel.loadPurchaseForEdit(purchaseId)
        } else if (editingPurchaseId == null) {
            viewModel.resetForm()
        }
    }

    val selectedDateMillis by viewModel.selectedDateMillis.collectAsState()
    val selectedSupplier by viewModel.selectedSupplier.collectAsState()
    val draftItems by viewModel.draftItems.collectAsState()
    val totalCost by viewModel.totalCost.collectAsState()

    val allSuppliers by viewModel.allSuppliers.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var supplierDropdownExpanded by remember { mutableStateOf(false) }

    var productSearchText by remember { mutableStateOf("") }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    val filteredProducts = remember(allProducts, productSearchText) {
        val nonServiceProducts = allProducts.filter { !it.isService }
        if (productSearchText.isBlank()) {
            nonServiceProducts
        } else {
            nonServiceProducts.filter { product ->
                product.name.contains(productSearchText, ignoreCase = true) ||
                (product.barcode?.contains(productSearchText, ignoreCase = true) == true) ||
                (product.differentiator?.contains(productSearchText, ignoreCase = true) == true) ||
                product.tags.any { it.contains(productSearchText, ignoreCase = true) }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
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
                            viewModel.setDateMillis(localCalendar.timeInMillis)
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

    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            onDismissRequest = { showBarcodeScanner = false },
            onBarcodeScanned = { scannedCode ->
                val trimmedCode = scannedCode.trim()
                val matchingProduct = if (trimmedCode.isNotEmpty()) {
                    allProducts.find { product ->
                        !product.isService && (
                            product.barcode?.trim().equals(trimmedCode, ignoreCase = true) ||
                            product.id.trim().equals(trimmedCode, ignoreCase = true)
                        )
                    }
                } else {
                    null
                }

                if (matchingProduct != null) {
                    viewModel.addProductToDraft(matchingProduct)
                    productSearchText = ""
                    productDropdownExpanded = false
                    Toast.makeText(context, "Agregado: ${matchingProduct.name}", Toast.LENGTH_SHORT).show()
                } else {
                    productSearchText = scannedCode
                    productDropdownExpanded = true
                    Toast.makeText(context, "No se encontró producto con el código: $scannedCode", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editingPurchaseId != null || purchaseId != null) "Editar Compra" else "Nueva Compra") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fecha de Compra
            OutlinedTextField(
                value = formatPurchaseDate(selectedDateMillis),
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha de Compra") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )

            // Proveedor
            ExposedDropdownMenuBox(
                expanded = supplierDropdownExpanded,
                onExpandedChange = { supplierDropdownExpanded = !supplierDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedSupplier?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Proveedor") },
                    placeholder = { Text("Seleccione un proveedor") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = supplierDropdownExpanded,
                    onDismissRequest = { supplierDropdownExpanded = false }
                ) {
                    if (allSuppliers.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay proveedores registrados") },
                            onClick = { supplierDropdownExpanded = false }
                        )
                    } else {
                        allSuppliers.forEach { supplier ->
                            DropdownMenuItem(
                                text = { Text(supplier.name) },
                                onClick = {
                                    viewModel.setSupplier(supplier)
                                    supplierDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Selector de Productos
            ExposedDropdownMenuBox(
                expanded = productDropdownExpanded && filteredProducts.isNotEmpty(),
                onExpandedChange = { productDropdownExpanded = it }
            ) {
                CompactSearchBar(
                    query = productSearchText,
                    onQueryChange = {
                        productSearchText = it
                        productDropdownExpanded = true
                    },
                    placeholder = "Buscar producto para agregar...",
                    onClear = {
                        productSearchText = ""
                        productDropdownExpanded = false
                    },
                    onScanBarcodeClick = { showBarcodeScanner = true },
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
                                        text = "Stock: ${product.stock} | Costo actual: ${formatCurrency(product.cost)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

            // Tabla de Productos Comprados
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tabla de Productos Comprados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (draftItems.isEmpty()) {
                        Text(
                            text = "No se han agregado productos a la compra",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        draftItems.forEach { item ->
                            PurchaseDraftItemRow(
                                item = item,
                                onQuantityChange = { qty ->
                                    viewModel.updateDraftItemQuantity(item.productId, qty)
                                },
                                onUnitCostChange = { cost ->
                                    viewModel.updateDraftItemUnitCost(item.productId, cost)
                                },
                                onDeleteClick = {
                                    viewModel.removeDraftItem(item.productId)
                                }
                            )
                            HorizontalDivider()
                        }
                    }

                    // Total Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatCurrency(totalCost),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Botón Guardar Compra
            Button(
                onClick = {
                    if (viewModel.savePurchase()) {
                        onNavigateUp()
                    }
                },
                enabled = selectedSupplier != null && draftItems.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (editingPurchaseId != null || purchaseId != null) "Actualizar Compra" else "Guardar Compra")
            }
        }
    }
}

@Composable
fun PurchaseDraftItemRow(
    item: PurchaseItem,
    onQuantityChange: (Int) -> Unit,
    onUnitCostChange: (Double) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var quantityText by remember(item.productId) { mutableStateOf(item.quantity.toString()) }
    var isQuantityFocused by remember { mutableStateOf(false) }
    var costTextFieldValue by remember(item.productId) {
        mutableStateOf(TextFieldValue(if (item.unitCost == 0.0) "" else item.unitCost.toString()))
    }

    LaunchedEffect(item.quantity) {
        if (!isQuantityFocused) {
            quantityText = item.quantity.toString()
        }
    }

    val subtotal = item.quantity * item.unitCost

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.productName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar producto",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Edit Cantidad
            OutlinedTextField(
                value = quantityText,
                onValueChange = { newText ->
                    val filtered = newText.filter { it.isDigit() }
                    quantityText = filtered
                    val qty = filtered.toIntOrNull()
                    if (qty != null && qty > 0) {
                        onQuantityChange(qty)
                    }
                },
                label = { Text("Cantidad") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState ->
                        isQuantityFocused = focusState.isFocused
                        if (!focusState.isFocused) {
                            val parsed = quantityText.toIntOrNull()
                            if (parsed == null || parsed <= 0) {
                                quantityText = "1"
                                onQuantityChange(1)
                            } else {
                                quantityText = parsed.toString()
                            }
                        }
                    }
            )

            // Edit Costo Unitario using sanitizeDecimalTextFieldValue
            OutlinedTextField(
                value = costTextFieldValue,
                onValueChange = { newValue ->
                    val sanitized = sanitizeDecimalTextFieldValue(newValue, costTextFieldValue)
                    costTextFieldValue = sanitized
                    val parsedCost = sanitized.text.toDoubleOrNull() ?: 0.0
                    onUnitCostChange(parsedCost)
                },
                label = { Text("Costo Unitario") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1.5f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Subtotal: ${formatCurrency(subtotal)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
