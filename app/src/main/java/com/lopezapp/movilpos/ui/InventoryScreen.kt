package com.lopezapp.movilpos.ui

import com.lopezapp.movilpos.ui.components.BarcodeScannerDialog
import com.lopezapp.movilpos.ui.components.CompactSearchBar

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import android.widget.Toast
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import androidx.core.net.toUri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.BrandingWatermark
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TrendingUp
import java.util.Locale
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import com.lopezapp.movilpos.ui.viewmodel.ProductTypeFilter
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.lopezapp.movilpos.util.formatCurrency
import com.lopezapp.movilpos.util.roundToTwoDecimals
import com.lopezapp.movilpos.util.sanitizeDecimalTextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import coil.compose.AsyncImage
import com.lopezapp.movilpos.data.model.BundleItem
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.Tax
import com.lopezapp.movilpos.data.model.TaxValueType
import com.lopezapp.movilpos.ui.navigation.AppNavDisplay
import com.lopezapp.movilpos.ui.viewmodel.InventoryViewModel
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileOutputStream

@Serializable
private object ProductListKey : NavKey

@Serializable
private data class ProductDetailKey(val productId: String) : NavKey

@Serializable
private data class ProductEditKey(val productId: String?) : NavKey // null for new

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToBarcodeLabels: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val currencySymbol = settingsUiState.currencySymbol
    val defaultDecimalPlaces = settingsUiState.defaultDecimalPlaces
    val allowExtraDecimals = settingsUiState.allowExtraDecimals

    val backStack = rememberNavBackStack(ProductListKey)
    
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
            entry<ProductListKey>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Selecciona un producto para ver sus detalles o agregar uno nuevo.")
                        }
                    }
                )
            ) {
                ProductListScreen(
                    viewModel = viewModel,
                    onNavigateBack = onNavigateBack,
                    onProductClick = { backStack.add(ProductDetailKey(it.id)) },
                    onAddProduct = { backStack.add(ProductEditKey(null)) },
                    currencySymbol = currencySymbol,
                    defaultDecimalPlaces = defaultDecimalPlaces,
                    allowExtraDecimals = allowExtraDecimals
                )
            }
            entry<ProductDetailKey>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                ProductDetailScreen(
                    productId = key.productId,
                    viewModel = viewModel,
                    onEditClick = { backStack.add(ProductEditKey(key.productId)) },
                    onNavigateUp = { backStack.removeLastOrNull() },
                    onNavigateToBarcodeLabels = onNavigateToBarcodeLabels,
                    currencySymbol = currencySymbol,
                    defaultDecimalPlaces = defaultDecimalPlaces,
                    allowExtraDecimals = allowExtraDecimals
                )
            }
            entry<ProductEditKey>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                ProductEditScreen(
                    productId = key.productId,
                    viewModel = viewModel,
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit,
    onProductClick: (Product) -> Unit,
    onAddProduct: () -> Unit,
    currencySymbol: String = "$",
    defaultDecimalPlaces: Int = 2,
    allowExtraDecimals: Boolean = true
) {
    val products by viewModel.filteredProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val selectedBrandFilter by viewModel.selectedBrandFilter.collectAsState()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsState()
    val lowStockOnlyFilter by viewModel.lowStockOnlyFilter.collectAsState()

    val categories by viewModel.categories.collectAsState()
    val brands by viewModel.brands.collectAsState()

    var showScannerDialog by remember { mutableStateOf(false) }

    if (showScannerDialog) {
        BarcodeScannerDialog(
            onDismissRequest = { showScannerDialog = false },
            onBarcodeScanned = { scannedCode ->
                viewModel.onSearchQueryChanged(scannedCode)
                showScannerDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduct) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Producto")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            CompactSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = "Buscar productos...",
                onScanBarcodeClick = { showScannerDialog = true },
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Horizontal Filter Chips Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Categoría Dropdown Menu
                var categoryDropdownExpanded by remember { mutableStateOf(false) }
                Box {
                    FilterChip(
                        selected = selectedCategoryFilter != null,
                        onClick = { categoryDropdownExpanded = true },
                        label = { Text(selectedCategoryFilter?.let { "Cat: $it" } ?: "Categoría") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas las categorías") },
                            onClick = {
                                viewModel.setSelectedCategoryFilter(null)
                                categoryDropdownExpanded = false
                            }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    viewModel.setSelectedCategoryFilter(category.name)
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Marca Dropdown Menu
                var brandDropdownExpanded by remember { mutableStateOf(false) }
                Box {
                    FilterChip(
                        selected = selectedBrandFilter != null,
                        onClick = { brandDropdownExpanded = true },
                        label = { Text(selectedBrandFilter?.let { "Marca: $it" } ?: "Marca") },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.BrandingWatermark,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = brandDropdownExpanded,
                        onDismissRequest = { brandDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas las marcas") },
                            onClick = {
                                viewModel.setSelectedBrandFilter(null)
                                brandDropdownExpanded = false
                            }
                        )
                        brands.forEach { brand ->
                            DropdownMenuItem(
                                text = { Text(brand.name) },
                                onClick = {
                                    viewModel.setSelectedBrandFilter(brand.name)
                                    brandDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Tipo Chips: Normal, Bundle, Servicio
                FilterChip(
                    selected = selectedTypeFilter == ProductTypeFilter.NORMAL,
                    onClick = {
                        viewModel.setSelectedTypeFilter(
                            if (selectedTypeFilter == ProductTypeFilter.NORMAL) null else ProductTypeFilter.NORMAL
                        )
                    },
                    label = { Text("Normal") }
                )
                FilterChip(
                    selected = selectedTypeFilter == ProductTypeFilter.BUNDLE,
                    onClick = {
                        viewModel.setSelectedTypeFilter(
                            if (selectedTypeFilter == ProductTypeFilter.BUNDLE) null else ProductTypeFilter.BUNDLE
                        )
                    },
                    label = { Text("Bundle") }
                )
                FilterChip(
                    selected = selectedTypeFilter == ProductTypeFilter.SERVICE,
                    onClick = {
                        viewModel.setSelectedTypeFilter(
                            if (selectedTypeFilter == ProductTypeFilter.SERVICE) null else ProductTypeFilter.SERVICE
                        )
                    },
                    label = { Text("Servicio") }
                )

                // Stock Bajo Filter Chip
                FilterChip(
                    selected = lowStockOnlyFilter,
                    onClick = { viewModel.setLowStockOnlyFilter(!lowStockOnlyFilter) },
                    label = { Text("Stock Bajo") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                // Limpiar Filtros Reset Button
                val isFilterActive = searchQuery.isNotBlank() ||
                        selectedCategoryFilter != null ||
                        selectedBrandFilter != null ||
                        selectedTypeFilter != null ||
                        lowStockOnlyFilter

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

            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    val isFilterActive = searchQuery.isNotBlank() ||
                            selectedCategoryFilter != null ||
                            selectedBrandFilter != null ||
                            selectedTypeFilter != null ||
                            lowStockOnlyFilter

                    Text(
                        text = if (isFilterActive) "No se encontraron productos con los filtros aplicados." else "No hay productos registrados.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(products, key = { it.id }) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onProductClick(product) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!product.imageUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = product.imageUri,
                                        contentDescription = "Foto de ${product.name}",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = if (product.isService && product.bundleItems.isEmpty()) "Stock: Ilimitado" else "Stock: ${product.stock}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Text(
                                    text = formatCurrency(product.price, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    viewModel: InventoryViewModel,
    onEditClick: () -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateToBarcodeLabels: () -> Unit = {},
    currencySymbol: String = "$",
    defaultDecimalPlaces: Int = 2,
    allowExtraDecimals: Boolean = true
) {
    val products by viewModel.inventoryState.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val taxes by viewModel.taxes.collectAsState()
    val product = products.find { it.id == productId }

    if (product != null) {
        ProductReadOnlyView(
            product = product,
            taxes = taxes,
            allProducts = allProducts,
            onEditClick = onEditClick,
            onDeleteClick = {
                viewModel.removeProduct(product.id)
                onNavigateUp()
            },
            onNavigateUp = onNavigateUp,
            onPrintLabelsClick = {
                viewModel.preloadSingleProduct(product)
                onNavigateToBarcodeLabels()
            },
            currencySymbol = currencySymbol,
            defaultDecimalPlaces = defaultDecimalPlaces,
            allowExtraDecimals = allowExtraDecimals
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Producto no encontrado") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("El producto seleccionado ya no existe.")
            }
        }
    }
}

@Composable
fun ProductEditScreen(
    productId: String?,
    viewModel: InventoryViewModel,
    onNavigateUp: () -> Unit
) {
    val products by viewModel.inventoryState.collectAsState()
    val product = if (productId != null) products.find { it.id == productId } else null

    ProductEditForm(
        product = product,
        productId = productId,
        viewModel = viewModel,
        onNavigateUp = onNavigateUp,
        onSaveSuccess = onNavigateUp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductReadOnlyView(
    product: Product,
    taxes: List<Tax> = emptyList(),
    allProducts: List<Product> = emptyList(),
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onNavigateUp: () -> Unit,
    onPrintLabelsClick: () -> Unit = {},
    currencySymbol: String = "$",
    defaultDecimalPlaces: Int = 2,
    allowExtraDecimals: Boolean = true
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar producto") },
            text = { Text("¿Seguro que desea eliminar este producto?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text("Eliminar")
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
                title = { Text("Información del Producto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = onPrintLabelsClick) {
                        Icon(Icons.Rounded.QrCode, contentDescription = "Imprimir Etiquetas")
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Producto")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar Producto")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Overview Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!product.imageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = product.imageUri,
                            contentDescription = "Foto de ${product.name}",
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val capsuleText = when {
                            product.isService -> "Servicio"
                            product.isBundle -> "Producto compuesto (Bundle)"
                            else -> {
                                val categoryText = product.category.ifBlank { "General" }
                                val brandText = product.brand.ifBlank { "Sin marca" }
                                "$categoryText • $brandText"
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (product.isService) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (product.isService) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Text(
                                text = capsuleText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Price & Cost Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Precio de Venta",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(product.price, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Costo",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(product.cost, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Bundle Items & Aggregated Categories/Brands Card (if product.isBundle)
            if (product.isBundle) {
                val bundleChildProducts = product.bundleItems.mapNotNull { item ->
                    val p = allProducts.find { it.id == item.productId }
                    if (p != null) item to p else null
                }
                val aggregatedCategories = bundleChildProducts
                    .map { it.second.category.ifBlank { "General" } }
                    .distinct()
                val aggregatedBrands = bundleChildProducts
                    .map { it.second.brand.ifBlank { "Sin marca" } }
                    .distinct()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Contenido del Bundle",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        if (product.bundleItems.isEmpty()) {
                            Text(
                                text = "Sin productos agregados al bundle",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            val originalTotal = bundleChildProducts.sumOf { (bundleItem, itemProduct) ->
                                itemProduct.price * bundleItem.quantity
                            }
                            val isDiscounted = product.price < originalTotal && originalTotal > 0
                            val ratio = if (isDiscounted) product.price / originalTotal else 1.0

                            val discountColor = if (isSystemInDarkTheme()) Color(0xFFEC407A) else Color(0xFFE91E63)
                            val normalPriceColor = MaterialTheme.colorScheme.primary
                            val priceColor = if (isDiscounted) discountColor else normalPriceColor

                            product.bundleItems.forEach { item ->
                                val child = allProducts.find { it.id == item.productId }
                                val childName = child?.name ?: "Producto desconocido"
                                val childPrice = child?.price ?: 0.0

                                val effectiveUnitPrice = if (isDiscounted) (childPrice * ratio).roundToTwoDecimals() else childPrice
                                val subtotal = if (isDiscounted) (effectiveUnitPrice * item.quantity).roundToTwoDecimals() else (childPrice * item.quantity)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = childName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        val subtitleText = buildAnnotatedString {
                                            append("Cantidad: ${item.quantity} ud.  •  Precio unitario: ")
                                            if (isDiscounted) {
                                                withStyle(SpanStyle(color = discountColor, fontWeight = FontWeight.Bold)) {
                                                    append(formatCurrency(effectiveUnitPrice, currencySymbol, defaultDecimalPlaces, allowExtraDecimals))
                                                }
                                            } else {
                                                append(formatCurrency(effectiveUnitPrice, currencySymbol, defaultDecimalPlaces, allowExtraDecimals))
                                            }
                                        }
                                        Text(
                                            text = subtitleText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = formatCurrency(subtotal, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = priceColor
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }

                        val categoriesSummary = if (aggregatedCategories.isNotEmpty()) {
                            "Categorías incluidas: " + aggregatedCategories.joinToString(", ")
                        } else {
                            "Categorías incluidas: Ninguna"
                        }
                        val brandsSummary = if (aggregatedBrands.isNotEmpty()) {
                            "Marcas incluidas: " + aggregatedBrands.joinToString(", ")
                        } else {
                            "Marcas incluidas: Ninguna"
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = categoriesSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = brandsSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Applied Taxes Calculated on Overall Bundle Price Card
                val appliedTaxes = taxes.filter { product.appliedTaxIds.contains(it.id) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Impuestos aplicados al Bundle",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        if (appliedTaxes.isEmpty()) {
                            Text(
                                text = "Sin impuestos aplicados al bundle",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            var totalTax = 0.0
                            appliedTaxes.forEach { tax ->
                                val taxAmount = if (tax.valueType == TaxValueType.PERCENTAGE) {
                                    if (product.isTaxIncludedInPrice) {
                                        (product.price - (product.price / (1.0 + tax.value / 100.0))).roundToTwoDecimals()
                                    } else {
                                        (product.price * (tax.value / 100.0)).roundToTwoDecimals()
                                    }
                                } else {
                                    tax.value
                                }
                                totalTax = (totalTax + taxAmount).roundToTwoDecimals()

                                val valueStr = if (tax.valueType == TaxValueType.PERCENTAGE) {
                                    "${if (tax.value % 1.0 == 0.0) tax.value.toInt().toString() else tax.value.toString()}%"
                                } else {
                                    formatCurrency(tax.value, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${tax.name} ($valueStr)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (!tax.description.isNullOrBlank()) {
                                            Text(
                                                text = tax.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = formatCurrency(taxAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Impuesto Total Calculado",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatCurrency(totalTax, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = if (product.isTaxIncludedInPrice) {
                                    "Los impuestos están incluidos en el precio del bundle (${formatCurrency(product.price, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)})."
                                } else {
                                    "Los impuestos no están incluidos. Precio total estimado con impuestos: ${formatCurrency(product.price + totalTax, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)}."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Service Required Products Card (if product.isService and has bundleItems)
            if (product.isService && product.bundleItems.isNotEmpty()) {
                val serviceChildProducts = product.bundleItems.mapNotNull { item ->
                    val p = allProducts.find { it.id == item.productId }
                    if (p != null) item to p else null
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Productos utilizados en el servicio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        serviceChildProducts.forEach { (item, child) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = child.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Cantidad: ${item.quantity} ${child.unitOfMeasure.ifBlank { "unidad" }}  •  Precio unitario: ${formatCurrency(child.price, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formatCurrency(child.price * item.quantity, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // Detailed Specifications Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (product.isService) "Detalles del Servicio" else "Detalles del Producto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    val barcode = product.barcode
                    val isBarcodeAvailable = !barcode.isNullOrBlank()
                    val barcodeText = if (isBarcodeAvailable && barcode != null) barcode else "No aplica"
                    val onCopyBarcode = {
                        if (barcode != null) {
                            clipboardManager.setText(AnnotatedString(barcode))
                            Toast.makeText(context, "Código de barras copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        }
                    }

                    InfoRow(
                        icon = Icons.Default.QrCode,
                        label = "Código de barras",
                        value = barcodeText,
                        onClick = if (isBarcodeAvailable) onCopyBarcode else null,
                        trailingContent = if (isBarcodeAvailable) {
                            {
                                IconButton(onClick = onCopyBarcode) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copiar código de barras"
                                    )
                                }
                            }
                        } else null
                    )

                    if (!product.isBundle && !product.isService) {
                        InfoRow(
                            icon = Icons.Default.Category,
                            label = "Categoría",
                            value = product.category.ifBlank { "General" }
                        )

                        InfoRow(
                            icon = Icons.AutoMirrored.Filled.BrandingWatermark,
                            label = "Marca",
                            value = product.brand.ifBlank { "Sin marca" }
                        )
                    }

                    if (!product.isService) {
                        InfoRow(
                            icon = Icons.Default.Straighten,
                            label = "Unidad de medida",
                            value = product.unitOfMeasure.ifBlank { "unidad" }
                        )
                    }

                    InfoRow(
                        icon = Icons.Default.AttachMoney,
                        label = "Costo",
                        value = formatCurrency(product.cost, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)
                    )

                    InfoRow(
                        icon = Icons.Default.Sell,
                        label = "Precio de venta",
                        value = formatCurrency(product.price, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)
                    )

                    val profit = product.price - product.cost
                    val margin = if (product.price > 0) (profit / product.price) * 100 else 0.0
                    val profitColor = if (isSystemInDarkTheme()) Color(0xFF4CAF50) else Color(0xFF2E7D32)
                    InfoRow(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        label = "Ganancia (Margen)",
                        value = "${formatCurrency(profit, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)} (${String.format(Locale.US, "%.1f%%", margin)})",
                        valueColor = profitColor
                    )

                    if (!product.isService || product.bundleItems.isNotEmpty()) {
                        InfoRow(
                            icon = Icons.Default.Warning,
                            label = "Cantidad alerta",
                            value = product.alertQuantity.toString()
                        )
                    }

                    val stockDisplayValue = if (product.isService && product.bundleItems.isEmpty()) {
                        "Ilimitado"
                    } else {
                        product.stock.toString()
                    }

                    InfoRow(
                        icon = Icons.Default.Inventory2,
                        label = if (product.isBundle) "Stock calculado (Bundle)" else if (product.isService) "Stock calculado (Servicio)" else "Cantidad en inventario",
                        value = stockDisplayValue
                    )

                    if (!product.isBundle) {
                        val appliedTaxes = taxes.filter { product.appliedTaxIds.contains(it.id) }
                        val taxesText = if (appliedTaxes.isNotEmpty()) {
                            appliedTaxes.joinToString(", ") { tax ->
                                val valueStr = if (tax.valueType == TaxValueType.PERCENTAGE) {
                                    "${if (tax.value % 1.0 == 0.0) tax.value.toInt().toString() else tax.value.toString()}%"
                                } else {
                                    formatCurrency(tax.value, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)
                                }
                                "${tax.name} ($valueStr)"
                            } + if (product.isTaxIncludedInPrice) " (Incluido)" else " (No incluido)"
                        } else if (product.appliedTaxIds.isNotEmpty()) {
                            "${product.appliedTaxIds.size} impuesto(s)" + if (product.isTaxIncludedInPrice) " (Incluido)" else ""
                        } else {
                            "Sin impuestos"
                        }

                        InfoRow(
                            icon = Icons.Default.Percent,
                            label = "Impuestos aplicados",
                            value = taxesText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .clickable { onClick() }
                } else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
        }
        if (trailingContent != null) {
            trailingContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditForm(
    product: Product?,
    productId: String?,
    viewModel: InventoryViewModel,
    onNavigateUp: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var isBundle by remember(product) { mutableStateOf(product?.isBundle ?: false) }
    var isService by remember(product) { mutableStateOf(product?.isService ?: false) }
    var bundleItems by remember(product) { mutableStateOf(product?.bundleItems ?: emptyList()) }

    var name by remember(product) { mutableStateOf(product?.name ?: "") }
    var barcode by remember(product) { mutableStateOf(product?.barcode ?: "") }
    var category by remember(product) { mutableStateOf(product?.category?.ifBlank { null } ?: if (product?.isService == true) "Servicio" else "General") }
    var brand by remember(product) { mutableStateOf(product?.brand?.ifBlank { null } ?: if (product?.isService == true) "General" else "") }
    var unitOfMeasure by remember(product) { mutableStateOf(product?.unitOfMeasure ?: "unidad") }
    var costState by remember(product) {
        val initialCost = product?.cost?.takeIf { it != 0.0 }?.toString() ?: ""
        mutableStateOf(TextFieldValue(initialCost, selection = TextRange(initialCost.length)))
    }
    var priceState by remember(product) {
        val initialPrice = product?.price?.takeIf { it != 0.0 }?.toString() ?: ""
        mutableStateOf(TextFieldValue(initialPrice, selection = TextRange(initialPrice.length)))
    }
    var alertQuantityStr by remember(product) { mutableStateOf(product?.alertQuantity?.takeIf { it != 0 }?.toString() ?: "") }
    var stockStr by remember(product) { mutableStateOf(product?.stock?.toString() ?: "0") }
    var imageUri by remember(product) { mutableStateOf(product?.imageUri) }

    val taxesList by viewModel.taxes.collectAsState()
    var selectedTaxIds by remember(product) { mutableStateOf(product?.appliedTaxIds?.toSet() ?: emptySet()) }
    var isTaxIncludedInPrice by remember(product) { mutableStateOf(product?.isTaxIncludedInPrice ?: false) }

    var pendingCropUri by remember { mutableStateOf<String?>(null) }
    var barcodeNotApplicable by remember(product) { mutableStateOf(product != null && product.barcode.isNullOrBlank()) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            onDismissRequest = { showBarcodeScanner = false },
            onBarcodeScanned = { scannedCode ->
                barcode = scannedCode
                showBarcodeScanner = false
            }
        )
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingCropUri = uri.toString()
        }
    }

    // Open CropImageDialog when pendingCropUri is set
    if (pendingCropUri != null) {
        CropImageDialog(
            imageUri = pendingCropUri!!,
            onDismiss = { pendingCropUri = null },
            onImageCropped = { croppedUri ->
                imageUri = croppedUri
                pendingCropUri = null
            }
        )
    }

    val categoriesList by viewModel.categories.collectAsState()
    val availableCategoryNames = remember(categoriesList, product) {
        val namesFromRepo = categoriesList.map { it.name }.filter { it.isNotBlank() }
        val currentProductCategory = product?.category
        val combined = if (!currentProductCategory.isNullOrBlank() && !namesFromRepo.contains(currentProductCategory)) {
            namesFromRepo + currentProductCategory
        } else if (namesFromRepo.isEmpty()) {
            listOf("General")
        } else {
            namesFromRepo
        }
        combined.distinct()
    }

    val brandsList by viewModel.brands.collectAsState()
    val availableBrandNames = remember(brandsList, product) {
        val namesFromRepo = brandsList.map { it.name }.filter { it.isNotBlank() }
        val currentProductBrand = product?.brand
        val combined = if (!currentProductBrand.isNullOrBlank() && !namesFromRepo.contains(currentProductBrand)) {
            namesFromRepo + currentProductBrand
        } else if (namesFromRepo.isEmpty()) {
            listOf("Sin marca")
        } else {
            namesFromRepo
        }
        combined.distinct()
    }

    val unitsOfMeasureList by viewModel.unitsOfMeasure.collectAsState()
    val availableUnitNames = remember(unitsOfMeasureList, product, isBundle) {
        val filteredUnits = if (isBundle) {
            unitsOfMeasureList.filter { it.isPackageOrBox }
        } else {
            unitsOfMeasureList
        }
        val namesFromRepo = filteredUnits.map { it.name }.filter { it.isNotBlank() }
        val currentProductUnit = product?.unitOfMeasure
        val combined = if (isBundle) {
            if (namesFromRepo.isEmpty()) {
                listOf("Caja", "Paquete")
            } else {
                namesFromRepo
            }
        } else {
            if (!currentProductUnit.isNullOrBlank() && namesFromRepo.none { it.equals(currentProductUnit, ignoreCase = true) }) {
                namesFromRepo + currentProductUnit
            } else if (namesFromRepo.isEmpty()) {
                listOf("Unidad", "Kilogramo", "Libra", "Caja", "Paquete")
            } else {
                namesFromRepo
            }
        }
        combined.distinct()
    }

    val allProducts by viewModel.allProducts.collectAsState()

    var categoryExpanded by remember { mutableStateOf(false) }
    var brandExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(isBundle, isService, bundleItems, allProducts) {
        if (isBundle || isService) {
            val computedCost = bundleItems.sumOf { item ->
                (allProducts.find { it.id == item.productId }?.cost ?: 0.0) * item.quantity
            }
            val text = computedCost.toString()
            costState = TextFieldValue(text, selection = TextRange(text.length))
        }
    }

    val updateBundleItems: (List<BundleItem>) -> Unit = { newItems ->
        bundleItems = newItems
        val computedCost = newItems.sumOf { item ->
            (allProducts.find { it.id == item.productId }?.cost ?: 0.0) * item.quantity
        }
        if (isBundle) {
            val computedPrice = newItems.sumOf { item ->
                (allProducts.find { it.id == item.productId }?.price ?: 0.0) * item.quantity
            }
            if (computedPrice > 0) {
                val pText = computedPrice.toString()
                priceState = TextFieldValue(pText, selection = TextRange(pText.length))
            } else if (newItems.isEmpty()) {
                priceState = TextFieldValue("")
            }
        }
        val cText = computedCost.toString()
        costState = TextFieldValue(cText, selection = TextRange(cText.length))
    }

    val onBundleToggle: (Boolean) -> Unit = { checked ->
        isBundle = checked
        if (checked) {
            isService = false
            val packageUnits = unitsOfMeasureList.filter { it.isPackageOrBox }
            val validNames = packageUnits.map { it.name }
            if (validNames.isNotEmpty() && !validNames.any { it.equals(unitOfMeasure, ignoreCase = true) }) {
                unitOfMeasure = validNames.first()
            } else if (validNames.isEmpty() && unitOfMeasure != "Caja" && unitOfMeasure != "Paquete") {
                unitOfMeasure = "Caja"
            }
            val computedCost = bundleItems.sumOf { item ->
                (allProducts.find { it.id == item.productId }?.cost ?: 0.0) * item.quantity
            }
            val cText = computedCost.toString()
            costState = TextFieldValue(cText, selection = TextRange(cText.length))
            if (bundleItems.isNotEmpty()) {
                updateBundleItems(bundleItems)
            }
        }
    }

    val onServiceToggle: (Boolean) -> Unit = { checked ->
        isService = checked
        if (checked) {
            isBundle = false
            unitOfMeasure = "Servicio"
            if (category == "General" || category == "Bundle" || category.isBlank()) {
                category = "Servicio"
            }
            if (brand.isBlank()) {
                brand = "General"
            }
            val computedCost = bundleItems.sumOf { item ->
                (allProducts.find { it.id == item.productId }?.cost ?: 0.0) * item.quantity
            }
            val cText = computedCost.toString()
            costState = TextFieldValue(cText, selection = TextRange(cText.length))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productId == null) "Agregar Producto" else "Editar Producto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancelar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Photo Selection & Framing Box
            if (!imageUri.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Vista previa de la imagen",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cambiar")
                            }
                            TextButton(
                                onClick = { imageUri = null },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Quitar")
                            }
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Añadir foto"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir foto / Seleccionar imagen")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del producto") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Checkboxes "Bundle" & "Servicio"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onBundleToggle(!isBundle) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isBundle,
                        onCheckedChange = { onBundleToggle(it) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Bundle",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onServiceToggle(!isService) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isService,
                        onCheckedChange = { onServiceToggle(it) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Servicio",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Código de barras") },
                    enabled = !barcodeNotApplicable,
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showBarcodeScanner = true },
                                enabled = !barcodeNotApplicable
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Escanear código de barras con la cámara"
                                )
                            }
                            if (barcode.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(barcode))
                                        Toast.makeText(context, "Código de barras copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copiar código de barras"
                                    )
                                }
                            }
                            IconButton(
                                onClick = { barcode = (10000000..99999999).random().toString() },
                                enabled = !barcodeNotApplicable
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Generar")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = barcodeNotApplicable,
                        onCheckedChange = { 
                            barcodeNotApplicable = it 
                            if (it) barcode = ""
                        }
                    )
                    Text("No aplica")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Categoría & Marca (Hidden when isBundle or isService is true)
            AnimatedVisibility(
                visible = !isBundle && !isService,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                )
            ) {
                Column {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            availableCategoryNames.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        category = item
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = brandExpanded,
                        onExpandedChange = { brandExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = brand,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Marca") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = brandExpanded,
                            onDismissRequest = { brandExpanded = false }
                        ) {
                            availableBrandNames.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        brand = item
                                        brandExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Unidad de Medida (Hidden when isService is true)
            AnimatedVisibility(
                visible = !isService,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                )
            ) {
                Column {
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = unitOfMeasure,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unidad de medida") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            availableUnitNames.forEach { item ->
                                val matchingUnit = unitsOfMeasureList.find { it.name.equals(item, ignoreCase = true) }
                                val labelText = if (matchingUnit != null && !matchingUnit.abbreviation.isNullOrBlank()) {
                                    "${matchingUnit.name} (${matchingUnit.abbreviation})"
                                } else {
                                    item
                                }
                                DropdownMenuItem(
                                    text = { Text(labelText) },
                                    onClick = {
                                        unitOfMeasure = item
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Section: Productos incluidos / requeridos en el Bundle o Servicio
            AnimatedVisibility(
                visible = isBundle || isService,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                )
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (isService) "Productos incluidos / requeridos en el servicio" else "Productos incluidos en el Bundle",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        val availableProducts = remember(allProducts, product) {
                            allProducts.filter { !it.isBundle && !it.isService && it.id != product?.id }
                        }
                        var itemsSearchQuery by remember { mutableStateOf("") }
                        var addProductExpanded by remember { mutableStateOf(false) }

                        val filteredProducts = remember(availableProducts, itemsSearchQuery) {
                            if (itemsSearchQuery.isBlank()) {
                                availableProducts
                            } else {
                                val query = itemsSearchQuery.trim().lowercase()
                                availableProducts.filter { p ->
                                    p.name.lowercase().contains(query) ||
                                    (p.barcode != null && p.barcode.lowercase().contains(query))
                                }
                            }
                        }

                        if (bundleItems.isEmpty()) {
                            Text(
                                text = if (isService) "No hay productos requeridos para el servicio." else "No hay productos agregados al bundle.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            bundleItems.forEach { bundleItem ->
                                val childProduct = allProducts.find { it.id == bundleItem.productId }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = childProduct?.name ?: "Producto desconocido",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "Precio unitario: ${formatCurrency(childProduct?.price ?: 0.0)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            var quantityText by remember(bundleItem.productId) {
                                                mutableStateOf(bundleItem.quantity.toString())
                                            }
                                            var isFocused by remember { mutableStateOf(false) }

                                            LaunchedEffect(bundleItem.quantity) {
                                                if (!isFocused) {
                                                    quantityText = bundleItem.quantity.toString()
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    val newQty = bundleItem.quantity - 1
                                                    if (newQty <= 0) {
                                                        updateBundleItems(bundleItems.filter { it.productId != bundleItem.productId })
                                                    } else {
                                                        quantityText = newQty.toString()
                                                        updateBundleItems(bundleItems.map {
                                                            if (it.productId == bundleItem.productId) it.copy(quantity = newQty) else it
                                                        })
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = "Disminuir cantidad")
                                            }
                                            OutlinedTextField(
                                                value = quantityText,
                                                onValueChange = { newText ->
                                                    val filtered = newText.filter { it.isDigit() }
                                                    val parsed = filtered.toIntOrNull() ?: filtered.toLongOrNull()?.coerceAtMost(999999L)?.toInt()
                                                    quantityText = if (filtered.isBlank()) "" else (parsed?.toString() ?: filtered)
                                                    val validQty = parsed?.takeIf { it > 0 } ?: 1
                                                    updateBundleItems(bundleItems.map {
                                                        if (it.productId == bundleItem.productId) it.copy(quantity = validQty) else it
                                                    })
                                                },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                textStyle = LocalTextStyle.current.copy(
                                                    textAlign = TextAlign.Center,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                ),
                                                modifier = Modifier
                                                    .width(68.dp)
                                                    .onFocusChanged { focusState ->
                                                        isFocused = focusState.isFocused
                                                        if (!focusState.isFocused) {
                                                            if (quantityText.isBlank() || (quantityText.toIntOrNull() ?: 0) <= 0) {
                                                                quantityText = bundleItem.quantity.toString()
                                                            }
                                                        }
                                                    }
                                            )
                                            IconButton(
                                                onClick = {
                                                    val newQty = bundleItem.quantity + 1
                                                    quantityText = newQty.toString()
                                                    updateBundleItems(bundleItems.map {
                                                        if (it.productId == bundleItem.productId) it.copy(quantity = newQty) else it
                                                    })
                                                }
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Aumentar cantidad")
                                            }
                                            IconButton(
                                                onClick = {
                                                    updateBundleItems(bundleItems.filter { it.productId != bundleItem.productId })
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = if (isService) "Eliminar producto del servicio" else "Eliminar producto del bundle",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = addProductExpanded,
                            onExpandedChange = { addProductExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = itemsSearchQuery,
                                onValueChange = { newValue ->
                                    itemsSearchQuery = newValue
                                    addProductExpanded = true
                                },
                                label = { Text(if (isService) "Buscar y agregar producto al servicio" else "Buscar y agregar producto al bundle") },
                                placeholder = { Text("Nombre o código de barras...") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = addProductExpanded)
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = addProductExpanded,
                                onDismissRequest = { addProductExpanded = false }
                            ) {
                                if (filteredProducts.isEmpty()) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (availableProducts.isEmpty()) "No hay productos disponibles"
                                                else "No se encontraron productos"
                                            )
                                        },
                                        onClick = { addProductExpanded = false }
                                    )
                                } else {
                                    filteredProducts.forEach { p ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = p.name,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                        if (!p.barcode.isNullOrBlank()) {
                                                            Text(
                                                                text = "Código: ${p.barcode}",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = formatCurrency(p.price),
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Medium,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            },
                                            onClick = {
                                                val existing = bundleItems.find { it.productId == p.id }
                                                val newItems = if (existing != null) {
                                                    bundleItems.map {
                                                        if (it.productId == p.id) it.copy(quantity = it.quantity + 1) else it
                                                    }
                                                } else {
                                                    bundleItems + BundleItem(productId = p.id, quantity = 1)
                                                }
                                                updateBundleItems(newItems)
                                                itemsSearchQuery = ""
                                                addProductExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = costState,
                onValueChange = { if (!isBundle && !isService) costState = sanitizeDecimalTextFieldValue(it, costState) },
                label = { Text("Costo") },
                enabled = !isBundle && !isService,
                readOnly = isBundle || isService,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = priceState,
                onValueChange = { priceState = sanitizeDecimalTextFieldValue(it, priceState) },
                label = { Text("Precio de Venta") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (!isService) {
                OutlinedTextField(
                    value = alertQuantityStr,
                    onValueChange = { alertQuantityStr = it },
                    label = { Text("Cantidad Alerta") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (!isBundle && !isService) {
                if (productId != null) {
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Cantidad en Inventario") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nota: Modificar el stock manualmente directamente aquí no es lo ideal. Se recomienda registrar una Compra en el menú de Compras para reabastecer el inventario.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "El stock de bundles y servicios se calcula automáticamente en base a sus insumos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Aplicar Impuestos Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Aplicar Impuestos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (taxesList.isEmpty()) {
                        Text(
                            text = "No hay impuestos configurados.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Selecciona los impuestos aplicables al producto:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        taxesList.forEach { tax ->
                            val isSelected = selectedTaxIds.contains(tax.id)
                            val formattedValue = if (tax.valueType == TaxValueType.PERCENTAGE) {
                                "${if (tax.value % 1.0 == 0.0) tax.value.toInt().toString() else tax.value.toString()}%"
                            } else {
                                formatCurrency(tax.value)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTaxIds = if (isSelected) {
                                            selectedTaxIds - tax.id
                                        } else {
                                            selectedTaxIds + tax.id
                                        }
                                    }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedTaxIds = if (checked) {
                                            selectedTaxIds + tax.id
                                        } else {
                                            selectedTaxIds - tax.id
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${tax.name} ($formattedValue)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (!tax.description.isNullOrBlank()) {
                                        Text(
                                            text = tax.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isTaxIncludedInPrice = !isTaxIncludedInPrice }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isTaxIncludedInPrice,
                            onCheckedChange = { isTaxIncludedInPrice = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Impuesto incluido en el precio del producto",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val price = priceState.text.toDoubleOrNull() ?: 0.0
                    val cost = costState.text.toDoubleOrNull() ?: 0.0
                    val finalAlertQuantity = if (isService) 0 else (alertQuantityStr.toIntOrNull() ?: 0)
                    val finalUnitOfMeasure = if (isService) "Servicio" else unitOfMeasure
                    val finalBarcode = if (barcodeNotApplicable) null else barcode.takeIf { it.isNotBlank() }
                    val finalCategory = if (isBundle) "Bundle" else if (isService) (category.takeIf { it.isNotBlank() && it != "Bundle" } ?: "Servicio") else category
                    val finalBrand = if (isBundle) "" else if (isService) (brand.takeIf { it.isNotBlank() } ?: "General") else brand

                    if (name.isNotBlank()) {
                        if (product != null) {
                            viewModel.updateProduct(
                                product.copy(
                                    name = name,
                                    barcode = finalBarcode,
                                    category = finalCategory,
                                    brand = finalBrand,
                                    unitOfMeasure = finalUnitOfMeasure,
                                    cost = cost,
                                    price = price,
                                    alertQuantity = finalAlertQuantity,
                                    stock = if (isService && bundleItems.isEmpty()) 9999 else if (isBundle || isService) 0 else (stockStr.toIntOrNull() ?: product.stock),
                                    imageUri = imageUri,
                                    appliedTaxIds = selectedTaxIds.toList(),
                                    isTaxIncludedInPrice = isTaxIncludedInPrice,
                                    isBundle = isBundle,
                                    bundleItems = if (isBundle || isService) bundleItems else emptyList(),
                                    isService = isService
                                )
                            )
                        } else {
                            viewModel.addProduct(
                                name = name,
                                barcode = finalBarcode,
                                category = finalCategory,
                                brand = finalBrand,
                                unitOfMeasure = finalUnitOfMeasure,
                                cost = cost,
                                price = price,
                                alertQuantity = finalAlertQuantity,
                                imageUri = imageUri,
                                appliedTaxIds = selectedTaxIds.toList(),
                                isTaxIncludedInPrice = isTaxIncludedInPrice,
                                isBundle = isBundle,
                                bundleItems = if (isBundle || isService) bundleItems else emptyList(),
                                isService = isService
                            )
                        }
                        onSaveSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropImageDialog(
    imageUri: String,
    onDismiss: () -> Unit,
    onImageCropped: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    var userScale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val frameSizeDp = 280.dp
    val density = LocalDensity.current
    val frameSizePx = with(density) { frameSizeDp.toPx() }

    LaunchedEffect(imageUri) {
        isLoading = true
        errorMessage = null
        val bitmap = loadBitmapFromUri(context, imageUri)
        if (bitmap != null) {
            loadedBitmap = bitmap
        } else {
            errorMessage = "No se pudo cargar la imagen."
        }
        isLoading = false
    }

    val initialScale = remember(loadedBitmap, frameSizePx) {
        val bmp = loadedBitmap
        if (bmp != null && bmp.width > 0 && bmp.height > 0) {
            maxOf(frameSizePx / bmp.width.toFloat(), frameSizePx / bmp.height.toFloat())
        } else {
            1f
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Encuadrar y recortar foto",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = {
                            userScale = 1f
                            offset = Offset.Zero
                        },
                        enabled = loadedBitmap != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restablecer encuadre"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Arrastra para centrar y pellizca para ajustar el zoom dentro del círculo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(frameSizeDp)
                        .clipToBounds()
                        .pointerInput(loadedBitmap) {
                            if (loadedBitmap != null) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    userScale = (userScale * zoom).coerceIn(0.5f, 5f)
                                    offset += pan
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> {
                            CircularProgressIndicator()
                        }
                        errorMessage != null -> {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        loadedBitmap != null -> {
                            val bmp = loadedBitmap!!
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.None,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        val totalScale = initialScale * userScale
                                        scaleX = totalScale
                                        scaleY = totalScale
                                        translationX = offset.x
                                        translationY = offset.y
                                    }
                            )

                            // Circular Mask Overlay
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
                                val radius = frameSizePx / 2f

                                val maskPath = Path().apply {
                                    addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
                                    addOval(
                                        Rect(
                                            center.x - radius,
                                            center.y - radius,
                                            center.x + radius,
                                            center.y + radius
                                        )
                                    )
                                    fillType = PathFillType.EvenOdd
                                }

                                drawPath(
                                    path = maskPath,
                                    color = Color.Black.copy(alpha = 0.65f)
                                )

                                drawCircle(
                                    color = Color.White,
                                    radius = radius,
                                    center = center,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSaving
                    ) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val bmp = loadedBitmap ?: return@Button
                            isSaving = true
                            coroutineScope.launch {
                                val savedUri = processCropAndSave(
                                    context = context,
                                    bitmap = bmp,
                                    initialScale = initialScale,
                                    userScale = userScale,
                                    offset = offset,
                                    frameSizePx = frameSizePx
                                )
                                isSaving = false
                                if (savedUri != null) {
                                    onImageCropped(savedUri)
                                }
                            }
                        },
                        enabled = loadedBitmap != null && !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Aplicar")
                    }
                }
            }
        }
    }
}

private suspend fun loadBitmapFromUri(context: Context, uriString: String): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val uri = uriString.toUri()
        val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (bitmap == null) return@withContext null

        val exifInputStream = context.contentResolver.openInputStream(uri)
        val exif = exifInputStream?.use { ExifInterface(it) }
        val orientation = exif?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        ) ?: ExifInterface.ORIENTATION_NORMAL

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        if (!matrix.isIdentity) {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private suspend fun processCropAndSave(
    context: Context,
    bitmap: Bitmap,
    initialScale: Float,
    userScale: Float,
    offset: Offset,
    frameSizePx: Float,
    outSizePx: Int = 512
): String? = withContext(Dispatchers.IO) {
    try {
        val croppedBitmap = Bitmap.createBitmap(outSizePx, outSizePx, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(croppedBitmap)

        val clipPath = android.graphics.Path().apply {
            addCircle(outSizePx / 2f, outSizePx / 2f, outSizePx / 2f, android.graphics.Path.Direction.CW)
        }
        canvas.clipPath(clipPath)

        val totalScale = initialScale * userScale
        val scaleRatio = outSizePx.toFloat() / frameSizePx

        val matrix = Matrix().apply {
            postTranslate(-bitmap.width / 2f, -bitmap.height / 2f)
            postScale(totalScale, totalScale)
            postTranslate(frameSizePx / 2f + offset.x, frameSizePx / 2f + offset.y)
            postScale(scaleRatio, scaleRatio)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(bitmap, matrix, paint)

        val cacheFile = File(context.cacheDir, "cropped_product_${System.currentTimeMillis()}.png")
        FileOutputStream(cacheFile).use { out ->
            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        Uri.fromFile(cacheFile).toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
