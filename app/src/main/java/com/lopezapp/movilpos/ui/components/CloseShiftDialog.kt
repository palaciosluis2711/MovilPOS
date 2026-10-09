package com.lopezapp.movilpos.ui.components

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lopezapp.movilpos.data.model.BluetoothPrinterConfig
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.CashShift
import com.lopezapp.movilpos.data.model.TicketConfig
import com.lopezapp.movilpos.ui.theme.MovilPOSTheme
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import com.lopezapp.movilpos.util.EscPosPrinter
import com.lopezapp.movilpos.util.formatCurrency
import com.lopezapp.movilpos.util.sanitizeDecimalTextFieldValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Dialog for closing shift & cash count ("Cierre y Arqueo de Caja").
 *
 * @param cashShift The active cash shift details
 * @param onCloseShift Callback invoked with the actual cash counted when closing shift
 * @param onDismiss Callback invoked when cancelling the closing process
 * @param errorMessage Optional error message to display
 * @param settingsViewModel Optional settings view model for Bluetooth printer config
 */
@Composable
fun CloseShiftDialog(
    modifier: Modifier = Modifier,
    cashShift: CashShift,
    onCloseShift: (actualCashCounted: Double) -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String? = null,
    settingsViewModel: SettingsViewModel? = null,
) {
    val context = LocalContext.current
    val settingsUiState = settingsViewModel?.uiState?.collectAsState()?.value
    val businessInfo = settingsUiState?.businessInfo ?: BusinessInfo()
    val ticketConfig = settingsUiState?.ticketConfig ?: TicketConfig()
    val bluetoothPrinterConfig = settingsUiState?.bluetoothPrinterConfig ?: BluetoothPrinterConfig()

    val initialExpectedString = remember(cashShift.expectedCash) {
        String.format(Locale.US, "%.2f", cashShift.expectedCash)
    }

    var cashCountedValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialExpectedString,
                selection = TextRange(initialExpectedString.length),
            ),
        )
    }

    var localError by remember { mutableStateOf<String?>(null) }
    val displayError = errorMessage ?: localError

    val actualCashCounted = cashCountedValue.text.toDoubleOrNull() ?: 0.0
    val difference = actualCashCounted - cashShift.expectedCash
    val isPositiveOrZero = difference >= -0.001

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
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Cierre de Turno",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(28.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title & Subtitle
                Text(
                    text = "Cierre y Arqueo de Caja",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Verifique las ventas del turno e ingrese el efectivo contado en caja.",
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

                // 1. Shift Summary Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Resumen del Turno",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        DetailRow(
                            icon = Icons.Default.Person,
                            label = "Cajero:",
                            value = cashShift.cashierName
                        )

                        DetailRow(
                            icon = Icons.Default.Schedule,
                            label = "Hora de Apertura:",
                            value = formatTimestamp(cashShift.openedAtMillis)
                        )

                        DetailRow(
                            icon = Icons.Default.Money,
                            label = "Fondo Inicial:",
                            value = formatCurrency(cashShift.initialFloat)
                        )

                        DetailRow(
                            icon = Icons.Default.Payments,
                            label = "Ventas en Efectivo:",
                            value = formatCurrency(cashShift.totalCashSales)
                        )

                        DetailRow(
                            icon = Icons.Default.CreditCard,
                            label = "Ventas con Tarjeta:",
                            value = formatCurrency(cashShift.totalCardSales)
                        )

                        DetailRow(
                            icon = Icons.Default.MoneyOff,
                            label = "Total Gastos / Egresos (-):",
                            value = if (cashShift.totalExpenses > 0) "-${formatCurrency(cashShift.totalExpenses)}" else formatCurrency(0.0),
                            valueColor = MaterialTheme.colorScheme.error
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Efectivo Esperado en Caja:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formatCurrency(cashShift.expectedCash),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Efectivo Contado en Caja Input Field
                OutlinedTextField(
                    value = cashCountedValue,
                    onValueChange = { newValue ->
                        localError = null
                        cashCountedValue = sanitizeDecimalTextFieldValue(newValue, cashCountedValue)
                    },
                    label = { Text("Efectivo Contado en Caja ($)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null
                        )
                    },
                    supportingText = {
                        Text("Ingrese el total físico de efectivo arqueado en la caja")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Real-time Calculation Card (Diferencia - Sobrante / Faltante)
                val cardBgColor = if (isPositiveOrZero) {
                    if (abs(difference) < 0.001) Color(0xFFE8F5E9) else Color(0xFFE8F5E9)
                } else {
                    Color(0xFFFFEBEE)
                }

                val textColor = if (isPositiveOrZero) {
                    Color(0xFF2E7D32)
                } else {
                    Color(0xFFC62828)
                }

                val statusIcon = if (isPositiveOrZero) {
                    if (abs(difference) < 0.001) Icons.Default.CheckCircle else Icons.Default.Info
                } else {
                    Icons.Default.Warning
                }

                val statusLabel = when {
                    abs(difference) < 0.001 -> "Caja Cuadrada (Sin Diferencia)"
                    difference > 0 -> "Sobrante en Caja (+${formatCurrency(difference)})"
                    else -> "Faltante en Caja (${formatCurrency(difference)})"
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Diferencia de Arqueo",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }

                        HorizontalDivider(color = textColor.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Efectivo Contado:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                            Text(
                                text = formatCurrency(actualCashCounted),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Efectivo Esperado:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                            Text(
                                text = formatCurrency(cashShift.expectedCash),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Resultado:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Action Buttons Row ("Imprimir por Bluetooth", "Cerrar Turno", "Cancelar")
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val macAddress = bluetoothPrinterConfig.macAddress
                            if (macAddress.isNullOrBlank()) {
                                Toast.makeText(context, "No hay impresora Bluetooth configurada.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Imprimiendo reporte de cierre de turno...", Toast.LENGTH_SHORT).show()
                                val bytes = EscPosPrinter.formatShiftReportTicket(cashShift, businessInfo, ticketConfig)
                                val result = EscPosPrinter.printBytesViaBluetooth(macAddress, bytes)
                                if (result.isSuccess) {
                                    Toast.makeText(context, "Reporte de turno impreso exitosamente", Toast.LENGTH_SHORT).show()
                                } else {
                                    val err = result.exceptionOrNull()?.message ?: "Error desconocido"
                                    Toast.makeText(context, "Error al imprimir: $err", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Imprimir por Bluetooth")
                    }

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
                                val count = cashCountedValue.text.toDoubleOrNull()
                                if ((count == null) || (count < 0.0)) {
                                    localError = "Ingrese un monto contado en caja válido"
                                    return@Button
                                }
                                onCloseShift(count)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cerrar Turno")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

private fun formatTimestamp(millis: Long): String {
    return try {
        val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        formatter.format(Date(millis))
    } catch (_: Exception) {
        "N/A"
    }
}

@Preview(showBackground = true)
@Composable
fun CloseShiftDialogPreview() {
    MovilPOSTheme {
        CloseShiftDialog(
            cashShift = CashShift(
                cashierId = "1",
                cashierName = "Carlos López",
                openedAtMillis = System.currentTimeMillis() - 28800000,
                initialFloat = 50.0,
                totalCashSales = 245.50,
                totalCardSales = 120.00
            ),
            onCloseShift = {},
            onDismiss = {}
        )
    }
}
