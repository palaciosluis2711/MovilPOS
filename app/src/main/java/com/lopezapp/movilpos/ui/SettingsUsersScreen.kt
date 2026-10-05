package com.lopezapp.movilpos.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lopezapp.movilpos.data.model.Role
import com.lopezapp.movilpos.data.model.User
import com.lopezapp.movilpos.ui.theme.MovilPOSTheme
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsUsersScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val users = uiState.users

    var showUserDialog by rememberSaveable { mutableStateOf(value = false) }
    var userToEdit by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usuarios y Permisos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Usuarios Registrados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Button(
                    onClick = {
                        userToEdit = null
                        showUserDialog = true
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp),
                    )
                    Text("Nuevo Usuario")
                }
            }

            if (users.isEmpty()) {
                Text(
                    text = "No hay usuarios registrados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                users.forEach { user ->
                    UserCardItem(
                        user = user,
                        onEdit = {
                            userToEdit = user
                            showUserDialog = true
                        },
                        onDelete = {
                            userToDelete = user
                        },
                    )
                }
            }
        }
    }

    if (showUserDialog) {
        UserAddEditDialog(
            user = userToEdit,
            onDismiss = {
                showUserDialog = false
                userToEdit = null
            },
        ) { name, role, pin, isActive ->
            if (userToEdit == null) {
                viewModel.addUser(
                    User(
                        name = name,
                        role = role,
                        pin = pin,
                        isActive = isActive,
                    ),
                )
            } else {
                userToEdit?.let { existingUser ->
                    viewModel.updateUser(
                        existingUser.copy(
                            name = name,
                            role = role,
                            pin = pin,
                            isActive = isActive,
                        ),
                    )
                }
            }
            showUserDialog = false
            userToEdit = null
        }
    }

    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Eliminar Usuario") },
            text = { Text("¿Está seguro de que desea eliminar al usuario \"${user.name}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUser(user.id)
                        userToDelete = null
                    },
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
fun UserCardItem(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(end = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RoleBadge(role = user.role)
                        StatusBadge(isActive = user.isActive)
                    }
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Usuario",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar Usuario",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
fun RoleBadge(
    role: Role,
    modifier: Modifier = Modifier,
) {
    val (label, containerColor, contentColor) = when (role) {
        Role.ADMINISTRATOR -> Triple(
            "Administrador",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
        )
        Role.SUPERVISOR -> Triple(
            "Supervisor",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Role.CASHIER -> Triple(
            "Cajero",
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun StatusBadge(
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val (label, containerColor, contentColor) = if (isActive) {
        Triple(
            "Activo",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
        )
    } else {
        Triple(
            "Inactivo",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserAddEditDialog(
    user: User?,
    onDismiss: () -> Unit,
    onSave: (name: String, role: Role, pin: String, isActive: Boolean) -> Unit,
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var selectedRole by remember { mutableStateOf(user?.role ?: Role.CASHIER) }
    var pin by remember { mutableStateOf(user?.pin ?: "") }
    var isActive by remember { mutableStateOf(user?.isActive ?: true) }

    var showPinVisibility by remember { mutableStateOf(value = false) }
    var nameError by remember { mutableStateOf(value = false) }
    var pinError by remember { mutableStateOf(value = false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "Nuevo Usuario" else "Editar Usuario") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError && it.isNotBlank()) nameError = false
                    },
                    label = { Text("Nombre del Usuario *") },
                    singleLine = true,
                    isError = nameError,
                    supportingText = {
                        if (nameError) {
                            Text("El nombre es requerido")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Rol de Acceso",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        FilterChip(
                            selected = selectedRole == Role.ADMINISTRATOR,
                            onClick = { selectedRole = Role.ADMINISTRATOR },
                            label = { Text("Administrador") },
                        )
                        FilterChip(
                            selected = selectedRole == Role.SUPERVISOR,
                            onClick = { selectedRole = Role.SUPERVISOR },
                            label = { Text("Supervisor") },
                        )
                        FilterChip(
                            selected = selectedRole == Role.CASHIER,
                            onClick = { selectedRole = Role.CASHIER },
                            label = { Text("Cajero") },
                        )
                    }
                }

                OutlinedTextField(
                    value = pin,
                    onValueChange = { newValue ->
                        if ((newValue.length <= 4) && newValue.all { it.isDigit() }) {
                            pin = newValue
                            if (pinError && (newValue.length == 4)) pinError = false
                        }
                    },
                    label = { Text("PIN de Seguridad (4 dígitos)") },
                    singleLine = true,
                    maxLines = 1,
                    isError = pinError,
                    supportingText = {
                        if (pinError) {
                            Text("El PIN debe ser de exactamente 4 dígitos")
                        } else {
                            Text("Exactamente 4 dígitos numéricos")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = if (showPinVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPinVisibility = !showPinVisibility }) {
                            Icon(
                                imageVector = if (showPinVisibility) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPinVisibility) "Ocultar PIN" else "Mostrar PIN",
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Usuario Activo",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val isNameValid = name.isNotBlank()
                    val isPinValid = (pin.length == 4) && pin.all { it.isDigit() }

                    nameError = !isNameValid
                    pinError = !isPinValid

                    if (isNameValid && isPinValid) {
                        onSave(name.trim(), selectedRole, pin, isActive)
                    }
                },
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsUsersScreenPreview() {
    MovilPOSTheme {
        Surface {
            UserCardItem(
                user = User(
                    id = "1",
                    name = "Carlos López",
                    pin = "1234",
                    role = Role.ADMINISTRATOR,
                    isActive = true,
                ),
                onEdit = {},
                onDelete = {},
            )
        }
    }
}
