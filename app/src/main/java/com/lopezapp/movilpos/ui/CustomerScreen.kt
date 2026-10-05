package com.lopezapp.movilpos.ui

import com.lopezapp.movilpos.ui.components.CompactSearchBar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.DocumentType
import com.lopezapp.movilpos.ui.navigation.AppNavDisplay
import com.lopezapp.movilpos.ui.navigation.CustomerDetailRoute
import com.lopezapp.movilpos.ui.navigation.CustomerEditRoute
import com.lopezapp.movilpos.ui.navigation.CustomersRoute
import com.lopezapp.movilpos.ui.viewmodel.CustomerViewModel
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import com.lopezapp.movilpos.util.DuiVisualTransformation
import com.lopezapp.movilpos.util.ElSalvadorCommercialActivities
import com.lopezapp.movilpos.util.ElSalvadorGeography
import com.lopezapp.movilpos.util.NitVisualTransformation
import com.lopezapp.movilpos.util.PhoneVisualTransformation
import com.lopezapp.movilpos.util.formatDui
import com.lopezapp.movilpos.util.formatNit
import com.lopezapp.movilpos.util.formatPhone

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun CustomerScreen(
    viewModel: CustomerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    val backStack = rememberNavBackStack(CustomersRoute)

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
            entry<CustomersRoute>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Selecciona un cliente para ver sus detalles o agregar uno nuevo.")
                        }
                    }
                )
            ) {
                CustomerListScreen(
                    viewModel = viewModel,
                    onNavigateBack = onNavigateBack,
                    onCustomerClick = { customer -> backStack.add(CustomerDetailRoute(customer.id)) },
                    onAddCustomer = { backStack.add(CustomerEditRoute(null)) }
                )
            }
            entry<CustomerDetailRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                CustomerReadOnlyView(
                    customerId = key.customerId,
                    viewModel = viewModel,
                    onEditClick = { backStack.add(CustomerEditRoute(key.customerId)) },
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
            }
            entry<CustomerEditRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                CustomerEditForm(
                    customerId = key.customerId,
                    viewModel = viewModel,
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    viewModel: CustomerViewModel,
    onNavigateBack: () -> Unit,
    onCustomerClick: (Customer) -> Unit,
    onAddCustomer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customers by viewModel.customers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clientes") },
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
                onClick = onAddCustomer,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Cliente"
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
                placeholder = "Buscar por nombre, documento, teléfono...",
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (customers.isEmpty()) {
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
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No hay clientes registrados." else "No se encontraron clientes.",
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
                    items(customers, key = { it.id }) { customer ->
                        CustomerCard(
                            customer = customer,
                            onClick = { onCustomerClick(customer) }
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
fun CustomerCard(
    customer: Customer,
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
            CustomerAvatar(
                name = customer.name,
                size = 48
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (customer.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text("Por defecto", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${customer.documentType.name}: ${customer.documentNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!customer.phone.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = customer.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerReadOnlyView(
    customerId: String,
    viewModel: CustomerViewModel,
    onEditClick: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customers by viewModel.allCustomers.collectAsState()
    val customer = customers.find { it.id == customerId }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (customer == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Cliente") },
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
                Text("Cliente no encontrado")
            }
        }
        return
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Cliente") },
            text = { Text("¿Estás seguro de que deseas eliminar a '${customer.name}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteCustomer(customer.id)
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
                title = { Text("Detalles del Cliente") },
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
                    CustomerAvatar(
                        name = customer.name,
                        size = 80
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (customer.isDefault) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text("Cliente por defecto") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
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
                    CustomerDetailRow(
                        icon = Icons.Default.Badge,
                        label = "Tipo de Documento",
                        value = customer.documentType.name
                    )

                    CustomerDetailRow(
                        icon = Icons.Default.Description,
                        label = "Número de Documento",
                        value = customer.documentNumber
                    )

                    if (customer.documentType == DocumentType.NIT) {
                        if (customer.commercialName.isNotBlank()) {
                            CustomerDetailRow(
                                icon = Icons.Default.Storefront,
                                label = "Nombre Comercial",
                                value = customer.commercialName
                            )
                        }

                        CustomerDetailRow(
                            icon = Icons.Default.Business,
                            label = "NRC",
                            value = customer.nrc.takeIf { !it.isNullOrBlank() } ?: "No especificado"
                        )

                        CustomerDetailRow(
                            icon = Icons.Default.Star,
                            label = "Gran Contribuyente",
                            value = if (customer.isLargeContributor) "Sí" else "No"
                        )

                        CustomerDetailRow(
                            icon = Icons.Default.Work,
                            label = "Actividad Económica",
                            value = customer.commercialActivity.takeIf { !it.isNullOrBlank() } ?: "No especificada"
                        )
                    }

                    CustomerDetailRow(
                        icon = Icons.Default.Phone,
                        label = "Teléfono",
                        value = customer.phone.takeIf { !it.isNullOrBlank() } ?: "No especificado"
                    )

                    CustomerDetailRow(
                        icon = Icons.Default.Email,
                        label = "Correo electrónico",
                        value = customer.email.takeIf { !it.isNullOrBlank() } ?: "No especificado"
                    )

                    CustomerDetailRow(
                        icon = Icons.Default.Public,
                        label = "País",
                        value = customer.country
                    )

                    CustomerDetailRow(
                        icon = Icons.Default.Map,
                        label = "Departamento",
                        value = customer.department
                    )

                    CustomerDetailRow(
                        icon = Icons.Default.LocationCity,
                        label = "Municipio",
                        value = customer.municipality
                    )

                    CustomerDetailRow(
                        icon = Icons.Default.HomeWork,
                        label = "Distrito",
                        value = customer.district
                    )

                    CustomerDetailRow(
                        icon = Icons.Default.LocationOn,
                        label = "Dirección",
                        value = customer.address.takeIf { !it.isNullOrBlank() } ?: "No especificada"
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
                Text("Eliminar Cliente")
            }
        }
    }
}

@Composable
fun CustomerDetailRow(
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
fun CustomerEditForm(
    customerId: String?,
    viewModel: CustomerViewModel,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customers by viewModel.allCustomers.collectAsState()
    val existingCustomer = remember(customerId, customers) {
        customers.find { it.id == customerId }
    }

    var name by remember(existingCustomer) { mutableStateOf(existingCustomer?.name ?: "") }
    var documentType by remember(existingCustomer) { mutableStateOf(existingCustomer?.documentType ?: DocumentType.DUI) }
    var documentNumber by remember(existingCustomer) {
        mutableStateOf(existingCustomer?.documentNumber?.filter { it.isDigit() }?.take(if (documentType == DocumentType.NIT) 14 else 9) ?: "")
    }
    var nrc by remember(existingCustomer) { mutableStateOf(existingCustomer?.nrc ?: "") }
    var phone by remember(existingCustomer) {
        mutableStateOf(existingCustomer?.phone?.filter { it.isDigit() }?.take(8) ?: "")
    }
    var email by remember(existingCustomer) { mutableStateOf(existingCustomer?.email ?: "") }
    var country by remember(existingCustomer) { mutableStateOf(existingCustomer?.country ?: "El Salvador") }

    var department by remember(existingCustomer) {
        mutableStateOf(existingCustomer?.department ?: ElSalvadorGeography.departments.firstOrNull() ?: "San Salvador")
    }
    var municipality by remember(existingCustomer, department) {
        val municipalities = ElSalvadorGeography.getMunicipalities(department)
        mutableStateOf(
            existingCustomer?.municipality?.takeIf { municipalities.contains(it) }
                ?: municipalities.firstOrNull()
                ?: ""
        )
    }
    var district by remember(existingCustomer, department, municipality) {
        val districts = ElSalvadorGeography.getDistricts(department, municipality)
        mutableStateOf(
            existingCustomer?.district?.takeIf { districts.contains(it) }
                ?: districts.firstOrNull()
                ?: ""
        )
    }

    var address by remember(existingCustomer) { mutableStateOf(existingCustomer?.address ?: "") }
    var isLargeContributor by remember(existingCustomer) { mutableStateOf(existingCustomer?.isLargeContributor ?: false) }
    var commercialActivity by remember(existingCustomer) { mutableStateOf(existingCustomer?.commercialActivity ?: "") }
    var commercialName by remember(existingCustomer) { mutableStateOf(existingCustomer?.commercialName ?: "") }
    var isDefault by remember(existingCustomer) { mutableStateOf(existingCustomer?.isDefault ?: false) }

    var isNameError by remember { mutableStateOf(false) }

    val handleSave = {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            isNameError = true
        } else {
            val formattedDocNum = when (documentType) {
                DocumentType.DUI -> formatDui(documentNumber)
                DocumentType.NIT -> formatNit(documentNumber)
            }
            val formattedPhone = formatPhone(phone)

            if (existingCustomer == null) {
                val newCustomer = Customer(
                    name = trimmedName,
                    documentType = documentType,
                    documentNumber = formattedDocNum,
                    nrc = if (documentType == DocumentType.NIT) nrc.ifBlank { null } else null,
                    phone = formattedPhone.ifBlank { null },
                    email = email.ifBlank { null },
                    country = country.ifBlank { "El Salvador" },
                    department = department,
                    municipality = municipality,
                    district = district,
                    address = address.ifBlank { null },
                    isLargeContributor = if (documentType == DocumentType.NIT) isLargeContributor else false,
                    commercialActivity = if (documentType == DocumentType.NIT) commercialActivity.ifBlank { null } else null,
                    commercialName = if (documentType == DocumentType.NIT) commercialName.trim() else "",
                    isDefault = isDefault
                )
                viewModel.addCustomer(newCustomer)
            } else {
                val updatedCustomer = existingCustomer.copy(
                    name = trimmedName,
                    documentType = documentType,
                    documentNumber = formattedDocNum,
                    nrc = if (documentType == DocumentType.NIT) nrc.ifBlank { null } else null,
                    phone = formattedPhone.ifBlank { null },
                    email = email.ifBlank { null },
                    country = country.ifBlank { "El Salvador" },
                    department = department,
                    municipality = municipality,
                    district = district,
                    address = address.ifBlank { null },
                    isLargeContributor = if (documentType == DocumentType.NIT) isLargeContributor else false,
                    commercialActivity = if (documentType == DocumentType.NIT) commercialActivity.ifBlank { null } else null,
                    commercialName = if (documentType == DocumentType.NIT) commercialName.trim() else "",
                    isDefault = isDefault
                )
                viewModel.updateCustomer(updatedCustomer)
            }
            onNavigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (existingCustomer == null) "Nuevo Cliente" else "Editar Cliente")
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
                    TextButton(onClick = handleSave) {
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
            // Nombre / Razón Social *
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (isNameError && it.isNotBlank()) {
                        isNameError = false
                    }
                },
                label = { Text("Nombre / Razón Social *") },
                placeholder = { Text("Ej. Juan Pérez o Comercial El Sol S.A.") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
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

            // Nombre Comercial (Solo si es NIT)
            if (documentType == DocumentType.NIT) {
                OutlinedTextField(
                    value = commercialName,
                    onValueChange = { commercialName = it },
                    label = { Text("Nombre Comercial") },
                    placeholder = { Text("Ej. Mi Negocio Comercial") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Storefront, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Tipo Documento Dropdown
            DropdownField(
                label = "Tipo de Documento",
                options = DocumentType.entries.map { it.name },
                selectedOption = documentType.name,
                onOptionSelected = { selected ->
                    val newType = DocumentType.valueOf(selected)
                    if (newType != documentType) {
                        documentType = newType
                        documentNumber = documentNumber.filter { it.isDigit() }.take(if (newType == DocumentType.DUI) 9 else 14)
                    }
                },
                leadingIcon = Icons.Default.Badge
            )

            // Document Number Field
            OutlinedTextField(
                value = documentNumber,
                onValueChange = { input ->
                    documentNumber = input.filter { it.isDigit() }.take(if (documentType == DocumentType.DUI) 9 else 14)
                },
                visualTransformation = when (documentType) {
                    DocumentType.DUI -> DuiVisualTransformation()
                    DocumentType.NIT -> NitVisualTransformation()
                },
                label = {
                    Text(
                        if (documentType == DocumentType.DUI) "Número de DUI (00000000-0) *"
                        else "Número de NIT (0000-000000-000-0) *"
                    )
                },
                placeholder = {
                    Text(if (documentType == DocumentType.DUI) "00000000-0" else "0000-000000-000-0")
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // NIT Specific Fields
            AnimatedVisibility(
                visible = documentType == DocumentType.NIT,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                )
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // NRC
                    OutlinedTextField(
                        value = nrc,
                        onValueChange = { nrc = it },
                        label = { Text("NRC (Número de Registro de Contribuyente)") },
                        placeholder = { Text("Ej. 123456-7") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Business, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Gran Contribuyente Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .toggleable(
                                value = isLargeContributor,
                                onValueChange = { isLargeContributor = it },
                                role = Role.Checkbox
                            )
                            .padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = isLargeContributor,
                            onCheckedChange = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gran Contribuyente",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    // Actividad Económica
                    EditableSearchableDropdownField(
                        value = commercialActivity,
                        onValueChange = { commercialActivity = it },
                        label = "Actividad Económica",
                        options = ElSalvadorCommercialActivities.activities,
                        placeholder = "Buscar o escribir actividad...",
                        leadingIcon = Icons.Default.Work,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Teléfono
            OutlinedTextField(
                value = phone,
                onValueChange = { input ->
                    phone = input.filter { it.isDigit() }.take(8)
                },
                visualTransformation = PhoneVisualTransformation(),
                label = { Text("Teléfono") },
                placeholder = { Text("0000-0000") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Correo electrónico
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                placeholder = { Text("ejemplo@correo.com") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // País
            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                label = { Text("País") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Public, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Departamento Dropdown
            DropdownField(
                label = "Departamento",
                options = ElSalvadorGeography.departments,
                selectedOption = department,
                onOptionSelected = { newDept ->
                    if (newDept != department) {
                        department = newDept
                        val municipalities = ElSalvadorGeography.getMunicipalities(newDept)
                        municipality = municipalities.firstOrNull() ?: ""
                        val districts = ElSalvadorGeography.getDistricts(newDept, municipality)
                        district = districts.firstOrNull() ?: ""
                    }
                },
                leadingIcon = Icons.Default.Map
            )

            // Municipio Cascading Dropdown
            val availableMunicipalities = remember(department) {
                ElSalvadorGeography.getMunicipalities(department)
            }
            DropdownField(
                label = "Municipio",
                options = availableMunicipalities,
                selectedOption = municipality,
                onOptionSelected = { newMuni ->
                    if (newMuni != municipality) {
                        municipality = newMuni
                        val districts = ElSalvadorGeography.getDistricts(department, newMuni)
                        district = districts.firstOrNull() ?: ""
                    }
                },
                leadingIcon = Icons.Default.LocationCity,
                enabled = availableMunicipalities.isNotEmpty()
            )

            // Distrito Cascading Dropdown
            val availableDistricts = remember(department, municipality) {
                ElSalvadorGeography.getDistricts(department, municipality)
            }
            DropdownField(
                label = "Distrito",
                options = availableDistricts,
                selectedOption = district,
                onOptionSelected = { newDist ->
                    district = newDist
                },
                leadingIcon = Icons.Default.HomeWork,
                enabled = availableDistricts.isNotEmpty()
            )

            // Dirección
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Dirección detallada") },
                placeholder = { Text("Ej. Calle Principal, Colonia Escalón #123") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Establecer como default Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .toggleable(
                        value = isDefault,
                        onValueChange = { isDefault = it },
                        role = Role.Checkbox
                    )
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = isDefault,
                    onCheckedChange = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Establecer como cliente por defecto",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = handleSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (existingCustomer == null) "Guardar Cliente" else "Guardar Cambios",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            leadingIcon = leadingIcon?.let {
                { Icon(imageVector = it, contentDescription = null) }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableSearchableDropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    placeholder: String = ""
) {
    var expanded by remember { mutableStateOf(false) }

    val filteredOptions = remember(value, options) {
        if (value.isBlank()) {
            options
        } else {
            val query = value.trim().lowercase()
            options.filter { option ->
                option.lowercase().contains(query)
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                expanded = true
            },
            label = { Text(label) },
            placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
            leadingIcon = leadingIcon?.let {
                { Icon(imageVector = it, contentDescription = null) }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (filteredOptions.isNotEmpty()) {
                filteredOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            } else if (value.isNotBlank()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Sin sugerencias. Se usará el texto ingresado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    },
                    onClick = { expanded = false },
                    enabled = false
                )
            }
        }
    }
}

@Composable
fun CustomerAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 48
) {
    val initial = name.trim().take(1).uppercase().ifEmpty { "C" }
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


