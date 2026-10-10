package com.lopezapp.movilpos.ui

import com.lopezapp.movilpos.ui.components.CompactSearchBar

import android.net.Uri
import com.lopezapp.movilpos.util.PhoneVisualTransformation
import com.lopezapp.movilpos.util.formatPhone
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import coil.compose.AsyncImage
import com.lopezapp.movilpos.data.model.Supplier
import com.lopezapp.movilpos.ui.navigation.AppNavDisplay
import com.lopezapp.movilpos.ui.navigation.SupplierDetailRoute
import com.lopezapp.movilpos.ui.navigation.SupplierEditRoute
import com.lopezapp.movilpos.ui.navigation.SuppliersRoute
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import com.lopezapp.movilpos.ui.viewmodel.SupplierViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SupplierScreen(
    viewModel: SupplierViewModel,
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(SuppliersRoute)

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
            entry<SuppliersRoute>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Selecciona un proveedor para ver sus detalles o agregar uno nuevo.")
                        }
                    }
                )
            ) {
                SupplierListScreen(
                    viewModel = viewModel,
                    onNavigateBack = onNavigateBack,
                    onSupplierClick = { supplier -> backStack.add(SupplierDetailRoute(supplier.id)) },
                    onAddSupplier = { backStack.add(SupplierEditRoute(null)) }
                )
            }
            entry<SupplierDetailRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                SupplierReadOnlyView(
                    supplierId = key.supplierId,
                    viewModel = viewModel,
                    onEditClick = { backStack.add(SupplierEditRoute(key.supplierId)) },
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
            }
            entry<SupplierEditRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                SupplierEditForm(
                    supplierId = key.supplierId,
                    viewModel = viewModel,
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierListScreen(
    viewModel: SupplierViewModel,
    onNavigateBack: () -> Unit,
    onSupplierClick: (Supplier) -> Unit,
    onAddSupplier: () -> Unit,
    modifier: Modifier = Modifier
) {
    val suppliers by viewModel.suppliers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proveedores") },
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
                onClick = onAddSupplier,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Proveedor"
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            CompactSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = "Buscar proveedor por nombre...",
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (suppliers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No hay proveedores registrados." else "No se encontraron proveedores.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(suppliers, key = { it.id }) { supplier ->
                        SupplierCard(
                            supplier = supplier,
                            onClick = { onSupplierClick(supplier) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierCard(
    supplier: Supplier,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SupplierAvatar(
                logoUri = supplier.logoUri,
                supplierName = supplier.name,
                size = 48
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = supplier.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                val subtitle = when {
                    !supplier.phone.isNullOrBlank() -> supplier.phone
                    !supplier.email.isNullOrBlank() -> supplier.email
                    !supplier.address.isNullOrBlank() -> supplier.address
                    else -> null
                }
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierReadOnlyView(
    supplierId: String,
    viewModel: SupplierViewModel,
    onEditClick: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val suppliers by viewModel.allSuppliers.collectAsState()
    val supplier = suppliers.find { it.id == supplierId }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (supplier == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Proveedor") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                Text("Proveedor no encontrado")
            }
        }
        return
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Proveedor") },
            text = { Text("¿Estás seguro de que deseas eliminar a '${supplier.name}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSupplier(supplier.id)
                        onNavigateUp()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Eliminar")
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
                title = { Text("Detalles del Proveedor") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    SupplierAvatar(
                        logoUri = supplier.logoUri,
                        supplierName = supplier.name,
                        size = 96
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = supplier.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailRow(
                        icon = Icons.Default.LocationOn,
                        label = "Dirección",
                        value = supplier.address.takeIf { !it.isNullOrBlank() } ?: "No especificada"
                    )

                    DetailRow(
                        icon = Icons.Default.Email,
                        label = "Correo electrónico",
                        value = supplier.email.takeIf { !it.isNullOrBlank() } ?: "No especificado"
                    )

                    DetailRow(
                        icon = Icons.Default.Phone,
                        label = "Teléfono",
                        value = supplier.phone.takeIf { !it.isNullOrBlank() } ?: "No especificado"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Eliminar Proveedor")
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierEditForm(
    supplierId: String?,
    viewModel: SupplierViewModel,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val suppliers by viewModel.allSuppliers.collectAsState()
    val existingSupplier = remember(supplierId, suppliers) {
        suppliers.find { it.id == supplierId }
    }

    var name by remember(existingSupplier) { mutableStateOf(existingSupplier?.name ?: "") }
    var logoUri by remember(existingSupplier) { mutableStateOf(existingSupplier?.logoUri ?: "") }
    var address by remember(existingSupplier) { mutableStateOf(existingSupplier?.address ?: "") }
    var email by remember(existingSupplier) { mutableStateOf(existingSupplier?.email ?: "") }
    var phone by remember(existingSupplier) {
        mutableStateOf(existingSupplier?.phone?.filter { it.isDigit() }?.take(8) ?: "")
    }

    var isNameError by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            logoUri = uri.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (existingSupplier == null) "Nuevo Proveedor" else "Editar Proveedor")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val trimmedName = name.trim()
                            if (trimmedName.isEmpty()) {
                                isNameError = true
                            } else {
                                val formattedPhone = formatPhone(phone)
                                if (existingSupplier == null) {
                                    viewModel.addSupplier(
                                        name = trimmedName,
                                        logoUri = logoUri.ifBlank { null },
                                        address = address.ifBlank { null },
                                        email = email.ifBlank { null },
                                        phone = formattedPhone.ifBlank { null }
                                    )
                                } else {
                                    val updated = existingSupplier.copy(
                                        name = trimmedName,
                                        logoUri = logoUri.ifBlank { null },
                                        address = address.ifBlank { null },
                                        email = email.ifBlank { null },
                                        phone = formattedPhone.ifBlank { null }
                                    )
                                    viewModel.updateSupplier(updated)
                                }
                                onNavigateUp()
                            }
                        }
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
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
            // Logo Picker Section
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Logo o Imagen del Proveedor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SupplierAvatar(
                        logoUri = logoUri.ifBlank { null },
                        supplierName = name.ifBlank { "Proveedor" },
                        size = 80
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") }
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Galería")
                        }

                        if (logoUri.isNotBlank()) {
                            OutlinedButton(
                                onClick = { logoUri = "" },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Quitar")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = logoUri,
                        onValueChange = { logoUri = it },
                        label = { Text("URL de la imagen (opcional)") },
                        placeholder = { Text("https://ejemplo.com/logo.png") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Link, contentDescription = null)
                        },
                        trailingIcon = {
                            if (logoUri.isNotEmpty()) {
                                IconButton(onClick = { logoUri = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Required Name Field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (isNameError && it.isNotBlank()) {
                        isNameError = false
                    }
                },
                label = { Text("Nombre / Razón Social *") },
                placeholder = { Text("Ej. Distribuidora Central S.A.") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Business, contentDescription = null)
                },
                isError = isNameError,
                supportingText = {
                    if (isNameError) {
                        Text("El nombre es requerido", color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Optional Address Field
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Dirección (opcional)") },
                placeholder = { Text("Ej. Av. Principal #123") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Optional Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico (opcional)") },
                placeholder = { Text("ejemplo@proveedor.com") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Optional Phone Field
            OutlinedTextField(
                value = phone,
                onValueChange = { input ->
                    phone = input.filter { it.isDigit() }.take(8)
                },
                visualTransformation = PhoneVisualTransformation(),
                label = { Text("Teléfono (opcional)") },
                placeholder = { Text("0000-0000") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isEmpty()) {
                        isNameError = true
                    } else {
                        val formattedPhone = formatPhone(phone)
                        if (existingSupplier == null) {
                            viewModel.addSupplier(
                                name = trimmedName,
                                logoUri = logoUri.ifBlank { null },
                                address = address.ifBlank { null },
                                email = email.ifBlank { null },
                                phone = formattedPhone.ifBlank { null }
                            )
                        } else {
                            val updated = existingSupplier.copy(
                                name = trimmedName,
                                logoUri = logoUri.ifBlank { null },
                                address = address.ifBlank { null },
                                email = email.ifBlank { null },
                                phone = formattedPhone.ifBlank { null }
                            )
                            viewModel.updateSupplier(updated)
                        }
                        onNavigateUp()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (existingSupplier == null) "Guardar Proveedor" else "Guardar Cambios",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun SupplierAvatar(
    logoUri: String?,
    supplierName: String,
    modifier: Modifier = Modifier,
    size: Int = 48
) {
    if (!logoUri.isNullOrBlank()) {
        AsyncImage(
            model = logoUri,
            contentDescription = supplierName,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape)
        )
    } else {
        val initial = supplierName.trim().take(1).uppercase().ifEmpty { "P" }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = modifier.size(size.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = initial,
                    style = if (size > 60) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
