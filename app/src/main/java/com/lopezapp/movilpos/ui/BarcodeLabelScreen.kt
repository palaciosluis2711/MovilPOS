package com.lopezapp.movilpos.ui

import android.content.Intent
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lopezapp.movilpos.data.model.BatchLabelItem
import com.lopezapp.movilpos.data.model.LabelSize
import com.lopezapp.movilpos.data.model.PrintMode
import com.lopezapp.movilpos.ui.viewmodel.BarcodeLabelViewModel
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import com.lopezapp.movilpos.util.formatCurrency
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeLabelScreen(
    viewModel: BarcodeLabelViewModel,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val products by viewModel.products.collectAsState()
    val labelSizes by viewModel.labelSizes.collectAsState()
    val config by viewModel.config.collectAsState()
    val batchItems by viewModel.batchItems.collectAsState()
    val isGeneratingPdf by viewModel.isGeneratingPdf.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf(1) }
    var showProductPicker by remember { mutableStateOf(false) }
    var showSaveCustomSizeDialog by remember { mutableStateOf(false) }
    var showClearBatchDialog by remember { mutableStateOf(false) }
    var customSizeNameInput by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (currentStep == 1) "Selección de Productos" else "Configuración de Etiqueta", 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep == 2) {
                            currentStep = 1
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (currentStep == 1 && batchItems.isNotEmpty()) {
                        IconButton(onClick = { showClearBatchDialog = true }) {
                            Icon(Icons.Rounded.DeleteSweep, contentDescription = "Limpiar lote")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                if (currentStep == 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Lote: ${batchItems.sumOf { it.quantity }} etiquetas",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "${batchItems.size} productos seleccionados",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Button(
                            onClick = { currentStep = 2 },
                            enabled = batchItems.isNotEmpty(),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Siguiente", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Lote: ${batchItems.sumOf { it.quantity }} etiquetas",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "${batchItems.size} productos seleccionados",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = 1 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("Anterior", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val file = viewModel.generatePdf(context)
                                        if (file != null && file.exists()) {
                                            try {
                                                val uri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    file
                                                )
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(uri, "application/pdf")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                snackbarMessage = "Error al abrir PDF: ${e.localizedMessage}"
                                            }
                                        } else {
                                            snackbarMessage = "Agregue al menos un producto al lote"
                                        }
                                    }
                                },
                                enabled = batchItems.isNotEmpty() && !isGeneratingPdf,
                                modifier = Modifier
                                    .weight(2f)
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Rounded.Print, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Generar / Imprimir PDF",
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (currentStep == 1) {
                // Step 1: Product Selection & Quantities
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Selección de Productos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = { showProductPicker = true }
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Añadir")
                                }
                            }

                            if (batchItems.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No hay productos en el lote. Haga clic en 'Añadir' para seleccionar productos del inventario.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(batchItems) { item ->
                                        BatchItemRow(
                                            item = item,
                                            onQuantityChange = { qty -> viewModel.updateItemQuantity(item.productId, qty) },
                                            onDeleteClick = { viewModel.removeItem(item.productId) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Step 2: Label Configuration (Clean view without live preview)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Label Size Selection & Custom Size Module
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Tamaño de Etiqueta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )

                            val allSizes = remember(labelSizes) {
                                labelSizes + LabelSize(id = "custom", name = "Personalizada", widthMm = 50.0, heightMm = 25.0, isFavorite = false)
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                allSizes.forEach { size ->
                                    val isSelected = config.selectedSizeId == size.id
                                    val labelText = if (size.id == "custom") {
                                        "Personalizada"
                                    } else {
                                        "${size.name} (${size.widthMm.toInt()}x${size.heightMm.toInt()}mm)"
                                    }

                                    if (size.isFavorite) {
                                        InputChip(
                                            selected = isSelected,
                                            onClick = { viewModel.selectSize(size.id) },
                                            label = {
                                                Text(
                                                    text = labelText,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Rounded.Star,
                                                    contentDescription = "Favorito",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            trailingIcon = {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clickable { viewModel.removeFavoriteSize(size.id) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Rounded.Close,
                                                        contentDescription = "Eliminar favorito",
                                                        modifier = Modifier.size(14.dp),
                                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            },
                                            colors = InputChipDefaults.inputChipColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                    } else {
                                        SuggestionChip(
                                            onClick = { viewModel.selectSize(size.id) },
                                            label = {
                                                Text(
                                                    text = labelText,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                                labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        )
                                    }
                                }
                            }

                            if (config.selectedSizeId == "custom") {
                                HorizontalDivider()
                                Text(
                                    text = "Dimensiones Personalizadas (mm)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    var widthText by remember(config.customWidthMm) { mutableStateOf(config.customWidthMm.toString()) }
                                    var heightText by remember(config.customHeightMm) { mutableStateOf(config.customHeightMm.toString()) }

                                    OutlinedTextField(
                                        value = widthText,
                                        onValueChange = { v ->
                                            widthText = v
                                            v.toIntOrNull()?.let { w ->
                                                if (w > 0) viewModel.setCustomDimensions(w, config.customHeightMm)
                                            }
                                        },
                                        label = { Text("Ancho (mm)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .weight(1f)
                                            .onFocusChanged { focusState ->
                                                if (!focusState.isFocused) {
                                                    val w = widthText.toIntOrNull() ?: 50
                                                    val finalW = if (w <= 0) 50 else w
                                                    widthText = finalW.toString()
                                                    viewModel.setCustomDimensions(finalW, config.customHeightMm)
                                                }
                                            }
                                    )
                                    OutlinedTextField(
                                        value = heightText,
                                        onValueChange = { v ->
                                            heightText = v
                                            v.toIntOrNull()?.let { h ->
                                                if (h > 0) viewModel.setCustomDimensions(config.customWidthMm, h)
                                            }
                                        },
                                        label = { Text("Alto (mm)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .weight(1f)
                                            .onFocusChanged { focusState ->
                                                if (!focusState.isFocused) {
                                                    val h = heightText.toIntOrNull() ?: 25
                                                    val finalH = if (h <= 0) 25 else h
                                                    heightText = finalH.toString()
                                                    viewModel.setCustomDimensions(config.customWidthMm, finalH)
                                                }
                                            }
                                    )
                                    IconButton(
                                        onClick = { showSaveCustomSizeDialog = true },
                                        modifier = Modifier
                                            .size(56.dp)
                                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                                    ) {
                                        Icon(Icons.Rounded.Star, contentDescription = "Guardar tamaño", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                    }
                                }
                            }
                        }
                    }

                    // Print Mode & Gap Checkbox
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Modo de Impresión y Formato",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                FilterChip(
                                    selected = config.printMode == PrintMode.THERMAL_ROLL,
                                    onClick = { viewModel.updateConfig(config.copy(printMode = PrintMode.THERMAL_ROLL)) },
                                    label = { Text("Rollo Térmico") },
                                    leadingIcon = { Icon(Icons.Rounded.Receipt, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = config.printMode == PrintMode.LETTER_SHEET,
                                    onClick = { viewModel.updateConfig(config.copy(printMode = PrintMode.LETTER_SHEET)) },
                                    label = { Text("Hoja Carta (Grid)") },
                                    leadingIcon = { Icon(Icons.Rounded.GridOn, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (config.printMode == PrintMode.LETTER_SHEET) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.updateConfig(config.copy(hasGap = !config.hasGap)) }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Aplicar separación estándar entre etiquetas",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Checkbox(
                                        checked = config.hasGap,
                                        onCheckedChange = { viewModel.updateConfig(config.copy(hasGap = it)) }
                                    )
                                }
                            }
                        }
                    }

                    // Elements to Include (Checkboxes)
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Elementos a Incluir en la Etiqueta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )

                            ToggleOptionRow(
                                title = "Nombre de la Tienda",
                                checked = config.showStoreName,
                                onCheckedChange = { viewModel.updateConfig(config.copy(showStoreName = it)) }
                            )
                            ToggleOptionRow(
                                title = "Nombre del Producto",
                                checked = config.showProductName,
                                onCheckedChange = { viewModel.updateConfig(config.copy(showProductName = it)) }
                            )
                            ToggleOptionRow(
                                title = "Imagen de Código de Barras",
                                checked = config.showBarcodeImage,
                                onCheckedChange = { viewModel.updateConfig(config.copy(showBarcodeImage = it)) }
                            )
                            ToggleOptionRow(
                                title = "Número de Código de Barras",
                                checked = config.showBarcodeNumber,
                                onCheckedChange = { viewModel.updateConfig(config.copy(showBarcodeNumber = it)) }
                            )
                            ToggleOptionRow(
                                title = "Precio del Producto",
                                checked = config.showPrice,
                                onCheckedChange = { viewModel.updateConfig(config.copy(showPrice = it)) }
                            )
                            ToggleOptionRow(
                                title = "Categoría y Marca",
                                checked = config.showCategoryBrand,
                                onCheckedChange = { viewModel.updateConfig(config.copy(showCategoryBrand = it)) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Product Picker Dialog
    if (showProductPicker) {
        var searchQuery by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showProductPicker = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Seleccionar Producto",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showProductPicker = false }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Cerrar")
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar producto...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    val filteredProducts = remember(products, searchQuery) {
                        if (searchQuery.isBlank()) products
                        else products.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                                    it.barcode?.contains(searchQuery, ignoreCase = true) == true
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredProducts) { product ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addProductToBatch(product, 1)
                                        snackbarMessage = "Agregado: ${product.name}"
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = product.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "Cod: ${product.barcode ?: "N/D"} | Stock: ${product.stock}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = formatCurrency(product.price),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showProductPicker = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Listo")
                    }
                }
            }
        }
    }

    // Save Custom Size Dialog
    if (showSaveCustomSizeDialog) {
        AlertDialog(
            onDismissRequest = { showSaveCustomSizeDialog = false },
            title = { Text("Guardar Tamaño en Favoritos") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Guardar tamaño ${config.customWidthMm}x${config.customHeightMm}mm como favorito:")
                    OutlinedTextField(
                        value = customSizeNameInput,
                        onValueChange = { customSizeNameInput = it },
                        label = { Text("Nombre (ej. Especial Tienda)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customSizeNameInput.isNotBlank()) {
                            viewModel.addFavoriteSize(
                                name = customSizeNameInput,
                                widthMm = config.customWidthMm,
                                heightMm = config.customHeightMm
                            )
                            snackbarMessage = "Tamaño guardado en favoritos"
                            customSizeNameInput = ""
                            showSaveCustomSizeDialog = false
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveCustomSizeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Clear Batch Warning Dialog
    if (showClearBatchDialog) {
        AlertDialog(
            onDismissRequest = { showClearBatchDialog = false },
            title = { Text("Limpiar selección") },
            text = { Text("¿Estás seguro de limpiar la selección del lote de productos?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearBatch()
                        showClearBatchDialog = false
                        snackbarMessage = "Lote limpiado"
                    }
                ) {
                    Text("Sí, limpiar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearBatchDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Loading Dialog
    if (isGeneratingPdf) {
        Dialog(onDismissRequest = { }) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Generando archivo PDF de etiquetas...",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun BatchItemRow(
    item: BatchLabelItem,
    onQuantityChange: (Int) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var quantityText by remember(item.productId) { mutableStateOf(item.quantity.toString()) }
    var isQuantityFocused by remember { mutableStateOf(false) }

    LaunchedEffect(item.quantity) {
        if (!isQuantityFocused) {
            quantityText = item.quantity.toString()
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cod: ${item.barcode} | ${formatCurrency(item.price)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        val newQty = (item.quantity - 1).coerceAtLeast(1)
                        quantityText = newQty.toString()
                        onQuantityChange(newQty)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Rounded.Remove, contentDescription = "Menos")
                }

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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .width(72.dp)
                        .height(56.dp)
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

                IconButton(
                    onClick = {
                        val newQty = item.quantity + 1
                        quantityText = newQty.toString()
                        onQuantityChange(newQty)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Más")
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun ToggleOptionRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}
