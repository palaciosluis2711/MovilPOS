package com.lopezapp.movilpos.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.model.TicketPaperSize
import com.lopezapp.movilpos.ui.viewmodel.POSViewModel
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import com.lopezapp.movilpos.util.PdfReportGenerator
import com.lopezapp.movilpos.util.formatCurrency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun POSTicketReceiptScreen(
    saleId: String,
    posViewModel: POSViewModel,
    settingsViewModel: SettingsViewModel,
    onStartNewSale: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept & disable system back gestures / back button presses on receipt screen
    BackHandler(enabled = true) {
        // Do nothing
    }

    val context = LocalContext.current
    val sales by posViewModel.sales.collectAsState(initial = emptyList())
    val sale = sales.find { it.id == saleId }

    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val businessInfo = settingsUiState.businessInfo
    val ticketConfig = settingsUiState.ticketConfig
    val currencySymbol = settingsUiState.currencySymbol
    val defaultDecimalPlaces = settingsUiState.defaultDecimalPlaces
    val allowExtraDecimals = settingsUiState.allowExtraDecimals

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (sale == null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No se encontró el comprobante de venta.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onStartNewSale) {
                            Text("Volver al POS")
                        }
                    }
                }
            } else {
                // 1. Fixed Header: Success Banner
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Éxito",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¡Venta Realizada con Éxito!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ticket N°: ${sale.id}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // 2. Individually Scrollable Ticket Container
                val receiptWidth = if (ticketConfig.paperSize == TicketPaperSize.SIZE_57MM) 260.dp else 320.dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Surface(
                        modifier = Modifier
                            .width(receiptWidth)
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFAFAFA),
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Business Logo
                            if (ticketConfig.showLogo) {
                                if (!businessInfo.logoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = businessInfo.logoUri,
                                        contentDescription = "Logo",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Storefront,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                            }

                            // Business Info Headers
                            if (ticketConfig.showBusinessName && businessInfo.name.isNotBlank()) {
                                Text(
                                    text = businessInfo.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (ticketConfig.showNit && businessInfo.nit.isNotBlank()) {
                                Text(
                                    text = "NIT: ${businessInfo.nit}",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (ticketConfig.showNrc && businessInfo.nrc.isNotBlank()) {
                                Text(
                                    text = "NRC: ${businessInfo.nrc}",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (ticketConfig.showAddress && businessInfo.address.isNotBlank()) {
                                Text(
                                    text = businessInfo.address,
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (ticketConfig.showPhone && businessInfo.phone.isNotBlank()) {
                                Text(
                                    text = "Tel: ${businessInfo.phone}",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (ticketConfig.showSocialMedia && businessInfo.socialMedia.isNotBlank()) {
                                Text(
                                    text = "Redes: ${businessInfo.socialMedia}",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center
                                )
                            }

                            HorizontalDivider(
                                color = Color.LightGray,
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            // Invoice Type & Sale Meta
                            val invoiceTypeLabel = when (sale.invoiceType) {
                                InvoiceType.CONSUMIDOR_FINAL -> "FACTURA CONSUMIDOR FINAL"
                                InvoiceType.CREDITO_FISCAL -> "COMPROBANTE CRÉDITO FISCAL"
                                InvoiceType.TICKET -> "TICKET DE VENTA"
                            }
                            Text(
                                text = invoiceTypeLabel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )

                            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            val formattedDate = dateFormat.format(Date(sale.dateMillis))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(text = "ID Venta: ${sale.id}", fontSize = 11.sp, color = Color.Black)
                                Text(text = "Fecha: $formattedDate", fontSize = 11.sp, color = Color.Black)
                                Text(text = "Cliente: ${sale.customerName}", fontSize = 11.sp, color = Color.Black)
                                Text(text = "Método de Pago: ${sale.paymentMethodName}", fontSize = 11.sp, color = Color.Black)
                            }

                            if (sale.isDteIssued) {
                                HorizontalDivider(
                                    color = Color.LightGray,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Text(
                                    text = "DOCUMENTO TRIBUTARIO ELECTRÓNICO (DTE)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                                val dteTypeDesc = if (sale.dteType == "03" || sale.invoiceType == InvoiceType.CREDITO_FISCAL) "Crédito Fiscal (03)" else "Consumidor Final (01)"
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(text = "Tipo: $dteTypeDesc", fontSize = 10.sp, color = Color.Black)
                                    Text(text = "Código de Generación: ${sale.dteGenerationCode ?: ""}", fontSize = 10.sp, color = Color.Black)
                                    Text(text = "Sello de Recepción: ${sale.dteReceptionSeal ?: ""}", fontSize = 10.sp, color = Color.Black)
                                    Text(text = "Número de Control: ${sale.dteControlNumber ?: ""}", fontSize = 10.sp, color = Color.Black)
                                }
                            }

                            HorizontalDivider(
                                color = Color.LightGray,
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            // Items Table Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cant x Producto", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black, modifier = Modifier.weight(1f))
                                Text("P.Unit", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black, modifier = Modifier.width(55.dp), textAlign = TextAlign.End)
                                Text("Subtotal", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black, modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
                            }

                            // Items List
                            sale.items.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${item.quantity} x ${item.productName}",
                                        fontSize = 11.sp,
                                        color = Color.Black,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = formatCurrency(item.unitPrice, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                        fontSize = 11.sp,
                                        color = Color.Black,
                                        modifier = Modifier.width(55.dp),
                                        textAlign = TextAlign.End
                                    )
                                    Text(
                                        text = formatCurrency(item.subtotal, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                        fontSize = 11.sp,
                                        color = Color.Black,
                                        modifier = Modifier.width(60.dp),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = Color.LightGray,
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            // Totals Section
                            if (sale.invoiceType == InvoiceType.CREDITO_FISCAL) {
                                val calculatedSubtotal = sale.totalAmount / 1.13
                                val calculatedTax = sale.totalAmount - calculatedSubtotal

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Subtotal (sin IVA):", fontSize = 11.sp, color = Color.Black)
                                    Text(
                                        text = formatCurrency(calculatedSubtotal, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                        fontSize = 11.sp,
                                        color = Color.Black
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("IVA (13%):", fontSize = 11.sp, color = Color.Black)
                                    Text(
                                        text = formatCurrency(calculatedTax, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                        fontSize = 11.sp,
                                        color = Color.Black
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("TOTAL FINAL:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                    Text(
                                        text = formatCurrency(sale.totalAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.Black
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("TOTAL:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                    Text(
                                        text = formatCurrency(sale.totalAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.Black
                                    )
                                }
                            }

                            if (sale.cashReceived > 0 || sale.paymentMethodName.contains("Efectivo", ignoreCase = true)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Recibido:", fontSize = 11.sp, color = Color.Black)
                                    Text(
                                        text = formatCurrency(sale.cashReceived, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                        fontSize = 11.sp,
                                        color = Color.Black
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Cambio:", fontSize = 11.sp, color = Color.Black)
                                    Text(
                                        text = formatCurrency(sale.changeAmount, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                        fontSize = 11.sp,
                                        color = Color.Black
                                    )
                                }
                            }

                            // Footer Message
                            if (ticketConfig.footerMessage.isNotBlank()) {
                                HorizontalDivider(
                                    color = Color.LightGray,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Text(
                                    text = ticketConfig.footerMessage,
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Fixed Footer Action Buttons Container
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Button 1: Imprimir Ticket / Ver Ticket PDF
                        Button(
                            onClick = {
                                try {
                                    val pdfFile = PdfReportGenerator.generateSaleTicketPdf(
                                        context = context,
                                        sale = sale,
                                        businessInfo = businessInfo,
                                        ticketConfig = ticketConfig
                                    )
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        pdfFile
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Ver / Imprimir Ticket PDF"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error al generar PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Imprimir Ticket / Ver PDF",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Button 2: Nueva Venta
                        OutlinedButton(
                            onClick = {
                                posViewModel.clearCart()
                                onStartNewSale()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddShoppingCart,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Nueva Venta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
