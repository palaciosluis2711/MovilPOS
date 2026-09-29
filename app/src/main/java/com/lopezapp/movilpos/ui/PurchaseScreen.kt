package com.lopezapp.movilpos.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
            }
            entry<PurchaseEditRoute>(
                metadata = ListDetailSceneStrategy.extraPane()
            ) {
                PurchaseEditForm(
                    viewModel = viewModel,
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseListScreen(
    viewModel: PurchaseViewModel,
    onNavigateBack: () -> Unit,
    onPurchaseClick: (String) -> Unit,
    onCreatePurchaseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val purchases by viewModel.purchases.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compras") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreatePurchaseClick) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Compra")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Buscar compra por proveedor o producto...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true
            )

            if (purchases.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "No hay compras registradas" else "No se encontraron compras",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(purchases, key = { it.id }) { purchase ->
                        PurchaseItemCard(
                            purchase = purchase,
                            onClick = { onPurchaseClick(purchase.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseItemCard(
    purchase: Purchase,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val purchase = viewModel.getPurchaseById(purchaseId)
    val supplier = purchase?.let { viewModel.getSupplierById(it.supplierId) }

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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseEditForm(
    viewModel: PurchaseViewModel,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDateMillis by viewModel.selectedDateMillis.collectAsState()
    val selectedSupplier by viewModel.selectedSupplier.collectAsState()
    val draftItems by viewModel.draftItems.collectAsState()
    val totalCost by viewModel.totalCost.collectAsState()

    val allSuppliers by viewModel.allSuppliers.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var supplierDropdownExpanded by remember { mutableStateOf(false) }

    var productSearchText by remember { mutableStateOf("") }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    val filteredProducts = remember(allProducts, productSearchText) {
        if (productSearchText.isBlank()) {
            allProducts
        } else {
            allProducts.filter { it.name.contains(productSearchText, ignoreCase = true) }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Compra") },
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
                expanded = productDropdownExpanded,
                onExpandedChange = { productDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = productSearchText,
                    onValueChange = {
                        productSearchText = it
                        productDropdownExpanded = true
                    },
                    label = { Text("Agregar Producto") },
                    placeholder = { Text("Buscar producto para agregar...") },
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
                                        text = product.name,
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
                Text("Guardar Compra")
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
    var costTextFieldValue by remember(item.productId) {
        mutableStateOf(TextFieldValue(if (item.unitCost == 0.0) "" else item.unitCost.toString()))
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
                    val qty = filtered.toIntOrNull() ?: 1
                    onQuantityChange(qty)
                },
                label = { Text("Cantidad") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
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
