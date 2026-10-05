package com.lopezapp.movilpos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lopezapp.movilpos.data.model.Role
import com.lopezapp.movilpos.data.model.User
import com.lopezapp.movilpos.data.model.toSpanishLabel
import com.lopezapp.movilpos.ui.theme.MovilPOSTheme
import com.lopezapp.movilpos.util.sanitizeDecimalTextFieldValue
import java.util.Locale

/**
 * Dialog for opening a new cash shift ("Apertura de Turno de Caja").
 *
 * @param users List of active users to select from as cashier
 * @param onOpenShift Callback with selected cashier, PIN, and initial float amount
 * @param onDismiss Callback when the user cancels the dialog
 * @param initialFloatDefault Default initial float amount (defaults to 50.00)
 * @param errorMessage Optional external error message to show
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenShiftDialog(
    modifier: Modifier = Modifier,
    users: List<User>,
    onOpenShift: (cashier: User, pin: String, initialFloat: Double) -> Unit,
    onDismiss: () -> Unit,
    initialFloatDefault: Double = 50.0,
    errorMessage: String? = null,
) {
    val activeUsers = remember(users) {
        users.filter { it.isActive }.ifEmpty { users }
    }

    var selectedCashier by remember {
        mutableStateOf(activeUsers.firstOrNull())
    }
    var pin by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(value = false) }

    val initialFloatString = remember(initialFloatDefault) {
        String.format(Locale.US, "%.2f", initialFloatDefault)
    }

    var initialFloatValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialFloatString,
                selection = TextRange(initialFloatString.length),
            ),
        )
    }

    var isDropdownExpanded by remember { mutableStateOf(value = false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val displayError = errorMessage ?: localError

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.PointOfSale,
                        contentDescription = "Apertura de Turno",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title & Subtitle
                Text(
                    text = "Apertura de Turno de Caja",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Seleccione el cajero e ingrese el fondo inicial para comenzar operaciones.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                if (!displayError.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = displayError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 1. Cashier Dropdown / Selector
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCashier?.name ?: "Seleccione un cajero",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cajero Responsable") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        activeUsers.forEach { user ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = user.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = user.role.toSpanishLabel(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedCashier = user
                                    isDropdownExpanded = false
                                    localError = null
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. PIN Input Field
                OutlinedTextField(
                    value = pin,
                    onValueChange = { input ->
                        if (input.length <= 10) {
                            pin = input.filter { it.isDigit() }
                            localError = null
                        }
                    },
                    label = { Text("PIN del Cajero") },
                    placeholder = { Text("0000") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPinVisible = !isPinVisible }) {
                            Icon(
                                imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPinVisible) "Ocultar PIN" else "Mostrar PIN"
                            )
                        }
                    },
                    visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Fondo Inicial de Efectivo Input Field
                OutlinedTextField(
                    value = initialFloatValue,
                    onValueChange = { newValue ->
                        localError = null
                        initialFloatValue = sanitizeDecimalTextFieldValue(newValue, initialFloatValue)
                    },
                    label = { Text("Fondo Inicial de Efectivo") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null
                        )
                    },
                    supportingText = {
                        Text("Efectivo disponible en caja para dar cambio")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Action Buttons Row ("Abrir Turno" / "Cancelar")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val cashier = selectedCashier
                            if (cashier == null) {
                                localError = "Seleccione un cajero válido"
                                return@Button
                            }
                            if (pin.isBlank()) {
                                localError = "Ingrese el PIN para verificar su identidad"
                                return@Button
                            }
                            val floatAmount = initialFloatValue.text.toDoubleOrNull()
                            if ((floatAmount == null) || (floatAmount < 0.0)) {
                                localError = "Ingrese un monto inicial válido"
                                return@Button
                            }

                            onOpenShift(cashier, pin, floatAmount)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Abrir Turno")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OpenShiftDialogPreview() {
    MovilPOSTheme {
        OpenShiftDialog(
            users = listOf(
                User(id = "1", name = "Carlos López", pin = "1234", role = Role.ADMINISTRATOR),
                User(id = "2", name = "María Pérez", pin = "5678", role = Role.CASHIER)
            ),
            onOpenShift = { _, _, _ -> },
            onDismiss = {}
        )
    }
}
