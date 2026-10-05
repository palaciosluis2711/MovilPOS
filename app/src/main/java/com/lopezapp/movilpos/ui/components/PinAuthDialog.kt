package com.lopezapp.movilpos.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lopezapp.movilpos.ui.theme.MovilPOSTheme

/**
 * Reusable dialog for 4-digit PIN authentication (e.g. for switching cashier,
 * authorizing protected actions, or confirming admin access).
 *
 * @param title Title text for the dialog
 * @param subtitle Optional subtitle or instructions text
 * @param pinLength Number of PIN digits required (default 4)
 * @param errorMessage Optional error text to display below header (e.g., "PIN incorrecto")
 * @param onPinSubmitted Callback invoked with the completed PIN string
 * @param onDismiss Callback invoked when the user cancels/dismisses the dialog
 */
@Composable
fun PinAuthDialog(
    modifier: Modifier = Modifier,
    title: String = "Autenticación de PIN",
    subtitle: String? = "Ingrese el PIN de 4 dígitos para continuar",
    pinLength: Int = 4,
    errorMessage: String? = null,
    onPinSubmitted: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }
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
                        imageVector = Icons.Default.Lock,
                        contentDescription = "PIN Auth",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                if (!displayError.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = displayError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Masked PIN dot indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp),
                ) {
                    for (i in 0 until pinLength) {
                        val isFilled = i < pin.length
                        val dotColor by animateColorAsState(
                            targetValue = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            label = "pinDotColor_$i"
                        )
                        val borderColor by animateColorAsState(
                            targetValue = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            label = "pinDotBorder_$i"
                        )

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .border(2.dp, borderColor, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3x4 Numeric Keypad
                val keypadKeys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "DEL")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (row in keypadKeys) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (key in row) {
                                FilledTonalButton(
                                    onClick = {
                                        localError = null
                                        when (key) {
                                            "C" -> pin = ""
                                            "DEL" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                            else -> {
                                                if (pin.length < pinLength) {
                                                    pin += key
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    when (key) {
                                        "DEL" -> Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Borrar dígito",
                                            modifier = Modifier.size(22.dp)
                                        )
                                        "C" -> Text(
                                            text = "Limpiar",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        else -> Text(
                                            text = key,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Action Buttons Row (Confirm / Dismiss)
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
                            if (pin.length == pinLength) {
                                onPinSubmitted(pin)
                            } else {
                                localError = "Ingrese un PIN de $pinLength dígitos"
                            }
                        },
                        enabled = pin.length == pinLength,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PinAuthDialogPreview() {
    MovilPOSTheme {
        PinAuthDialog(
            title = "Autenticación de PIN",
            subtitle = "Ingrese el PIN de 4 dígitos para continuar",
            onPinSubmitted = {},
        ) {}
    }
}
