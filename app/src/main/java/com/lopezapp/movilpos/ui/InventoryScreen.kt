package com.lopezapp.movilpos.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import androidx.core.net.toUri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Straighten
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import coil.compose.AsyncImage
import com.lopezapp.movilpos.data.model.Product
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
private data class ProductDetailKey(val productId: String?) : NavKey // null for new

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    onAddProduct = { backStack.add(ProductDetailKey(null)) }
                )
            }
            entry<ProductDetailKey>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                ProductDetailScreen(
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
    onAddProduct: () -> Unit
) {
    val products by viewModel.inventoryState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

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
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Buscar productos...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

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
                                Text(text = "Stock: ${product.stock}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(text = "$${product.price}", style = MaterialTheme.typography.titleMedium)
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
    productId: String?,
    viewModel: InventoryViewModel,
    onNavigateUp: () -> Unit
) {
    val products by viewModel.inventoryState.collectAsState()
    val product = products.find { it.id == productId }

    var isEditMode by remember(productId) { mutableStateOf(productId == null) }

    BackHandler(enabled = (isEditMode && productId != null)) {
        isEditMode = false
    }

    if (isEditMode) {
        ProductEditForm(
            product = product,
            productId = productId,
            viewModel = viewModel,
            onNavigateUp = {
                if (productId != null) {
                    isEditMode = false
                } else {
                    onNavigateUp()
                }
            },
            onSaveSuccess = {
                if (productId != null) {
                    isEditMode = false
                } else {
                    onNavigateUp()
                }
            }
        )
    } else if (product != null) {
        ProductReadOnlyView(
            product = product,
            onEditClick = { isEditMode = true },
            onDeleteClick = {
                viewModel.removeProduct(product.id)
                onNavigateUp()
            },
            onNavigateUp = onNavigateUp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductReadOnlyView(
    product: Product,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onNavigateUp: () -> Unit
) {
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
            // Main Overview Card with Circular Image to the left of Product Name and Category/Brand Capsule
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Product Photo (or Circular Placeholder)
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

                    // Product Title & Capsule Chip (Category and Brand)
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

                        val categoryText = product.category.ifBlank { "General" }
                        val brandText = product.brand.ifBlank { "Sin marca" }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Text(
                                text = "$categoryText • $brandText",
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
                            text = "$${product.price}",
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
                            text = "$${product.cost}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
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
                        text = "Detalles del Producto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    InfoRow(
                        icon = Icons.Default.QrCode,
                        label = "Código de barras",
                        value = if (product.barcode.isNullOrBlank()) "No aplica" else product.barcode
                    )

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

                    InfoRow(
                        icon = Icons.Default.Straighten,
                        label = "Unidad de medida",
                        value = product.unitOfMeasure.ifBlank { "unidad" }
                    )

                    InfoRow(
                        icon = Icons.Default.AttachMoney,
                        label = "Costo",
                        value = "$${product.cost}"
                    )

                    InfoRow(
                        icon = Icons.Default.Sell,
                        label = "Precio de venta",
                        value = "$${product.price}"
                    )

                    InfoRow(
                        icon = Icons.Default.Warning,
                        label = "Cantidad alerta",
                        value = product.alertQuantity.toString()
                    )

                    InfoRow(
                        icon = Icons.Default.Inventory2,
                        label = "Stock actual",
                        value = product.stock.toString()
                    )
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
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                color = MaterialTheme.colorScheme.onSurface
            )
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
    var name by remember(product) { mutableStateOf(product?.name ?: "") }
    var barcode by remember(product) { mutableStateOf(product?.barcode ?: "") }
    var category by remember(product) { mutableStateOf(product?.category ?: "General") }
    var brand by remember(product) { mutableStateOf(product?.brand ?: "") }
    var unitOfMeasure by remember(product) { mutableStateOf(product?.unitOfMeasure ?: "unidad") }
    var costStr by remember(product) { mutableStateOf(product?.cost?.takeIf { it != 0.0 }?.toString() ?: "") }
    var priceStr by remember(product) { mutableStateOf(product?.price?.takeIf { it != 0.0 }?.toString() ?: "") }
    var alertQuantityStr by remember(product) { mutableStateOf(product?.alertQuantity?.takeIf { it != 0 }?.toString() ?: "") }
    var imageUri by remember(product) { mutableStateOf(product?.imageUri) }

    var pendingCropUri by remember { mutableStateOf<String?>(null) }
    var barcodeNotApplicable by remember(product) { mutableStateOf(product != null && product.barcode.isNullOrBlank()) }

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

    val categories = listOf("General", "Bebidas", "Comida", "Snacks", "Electrónica")
    val brands = listOf("Sin Marca", "Marca A", "Marca B", "Marca C")
    val units = listOf("unidad", "libra", "kilo", "litro")

    var categoryExpanded by remember { mutableStateOf(false) }
    var brandExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

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
                        IconButton(
                            onClick = { barcode = (10000000..99999999).random().toString() },
                            enabled = !barcodeNotApplicable
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Generar")
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
                    categories.forEach { item ->
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
                    brands.forEach { item ->
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
                    units.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                unitOfMeasure = item
                                unitExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = costStr,
                onValueChange = { costStr = it },
                label = { Text("Costo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = priceStr,
                onValueChange = { priceStr = it },
                label = { Text("Precio de Venta") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = alertQuantityStr,
                onValueChange = { alertQuantityStr = it },
                label = { Text("Cantidad Alerta") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    val cost = costStr.toDoubleOrNull() ?: 0.0
                    val alertQuantity = alertQuantityStr.toIntOrNull() ?: 0
                    val finalBarcode = if (barcodeNotApplicable) null else barcode.takeIf { it.isNotBlank() }
                    
                    if (name.isNotBlank()) {
                        if (product != null) {
                            viewModel.updateProduct(
                                product.copy(
                                    name = name,
                                    barcode = finalBarcode,
                                    category = category,
                                    brand = brand,
                                    unitOfMeasure = unitOfMeasure,
                                    cost = cost,
                                    price = price,
                                    alertQuantity = alertQuantity,
                                    imageUri = imageUri
                                )
                            )
                        } else {
                            viewModel.addProduct(
                                name = name,
                                barcode = finalBarcode,
                                category = category,
                                brand = brand,
                                unitOfMeasure = unitOfMeasure,
                                cost = cost,
                                price = price,
                                alertQuantity = alertQuantity,
                                imageUri = imageUri
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
