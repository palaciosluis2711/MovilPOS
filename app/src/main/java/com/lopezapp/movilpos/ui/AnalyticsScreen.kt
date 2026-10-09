package com.lopezapp.movilpos.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lopezapp.movilpos.ui.theme.MovilPOSTheme
import com.lopezapp.movilpos.ui.viewmodel.AnalyticsPeriod
import com.lopezapp.movilpos.ui.viewmodel.AnalyticsUiState
import com.lopezapp.movilpos.ui.viewmodel.AnalyticsViewModel
import com.lopezapp.movilpos.ui.viewmodel.PaymentMethodBreakdownItem
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import com.lopezapp.movilpos.ui.viewmodel.TopProductItem
import com.lopezapp.movilpos.util.formatCurrency
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()

    AnalyticsContent(
        uiState = uiState,
        currencySymbol = settingsState.currencySymbol,
        defaultDecimalPlaces = settingsState.defaultDecimalPlaces,
        allowExtraDecimals = settingsState.allowExtraDecimals,
        onPeriodSelected = { viewModel.onPeriodSelected(it) },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsContent(
    uiState: AnalyticsUiState,
    currencySymbol: String,
    defaultDecimalPlaces: Int,
    allowExtraDecimals: Boolean,
    onPeriodSelected: (AnalyticsPeriod) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Estadísticas y Analítica")
                    }
                },
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
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Selector
            item {
                PeriodSelector(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodSelected = onPeriodSelected
                )
            }

            // Grid of KPI Cards
            item {
                KpiGridSection(
                    uiState = uiState,
                    currencySymbol = currencySymbol,
                    defaultDecimalPlaces = defaultDecimalPlaces,
                    allowExtraDecimals = allowExtraDecimals
                )
            }

            // Top 5 Productos Estrella Section
            item {
                SectionHeader(
                    title = "Top 5 Productos Estrella",
                    icon = Icons.Rounded.Star,
                    tint = Color(0xFFFFB300)
                )
            }

            if (uiState.topSellingProducts.isEmpty()) {
                item {
                    EmptyDataCard(message = "No se registraron ventas de productos en este período.")
                }
            } else {
                itemsIndexed(uiState.topSellingProducts) { index, item ->
                    TopProductCard(
                        rank = index + 1,
                        product = item,
                        currencySymbol = currencySymbol,
                        defaultDecimalPlaces = defaultDecimalPlaces,
                        allowExtraDecimals = allowExtraDecimals
                    )
                }
            }

            // Ventas por Método de Pago Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(
                    title = "Ventas por Método de Pago",
                    icon = Icons.Rounded.Payments,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (uiState.paymentMethodBreakdown.isEmpty()) {
                item {
                    EmptyDataCard(message = "No hay datos de métodos de pago en este período.")
                }
            } else {
                item {
                    PaymentMethodBreakdownCard(
                        items = uiState.paymentMethodBreakdown,
                        currencySymbol = currencySymbol,
                        defaultDecimalPlaces = defaultDecimalPlaces,
                        allowExtraDecimals = allowExtraDecimals
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: AnalyticsPeriod,
    onPeriodSelected: (AnalyticsPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnalyticsPeriod.entries.forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = { Text(period.label) }
            )
        }
    }
}

@Composable
fun KpiGridSection(
    uiState: AnalyticsUiState,
    currencySymbol: String,
    defaultDecimalPlaces: Int,
    allowExtraDecimals: Boolean,
    modifier: Modifier = Modifier
) {
    val totalSalesFormatted = formatCurrency(
        uiState.totalSalesAmount,
        currencySymbol,
        defaultDecimalPlaces,
        allowExtraDecimals
    )
    val totalProfitFormatted = formatCurrency(
        uiState.totalGrossProfit,
        currencySymbol,
        defaultDecimalPlaces,
        allowExtraDecimals
    )
    val averageTicketFormatted = formatCurrency(
        uiState.averageTicketAmount,
        currencySymbol,
        defaultDecimalPlaces,
        allowExtraDecimals
    )

    val greenColor = Color(0xFF2E7D32)

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            KpiCard(
                title = "Ventas Totales",
                value = totalSalesFormatted,
                icon = Icons.AutoMirrored.Rounded.TrendingUp,
                valueColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Ganancia Bruta",
                value = totalProfitFormatted,
                icon = Icons.Rounded.Savings,
                valueColor = greenColor,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            KpiCard(
                title = "Ticket Promedio",
                value = averageTicketFormatted,
                icon = Icons.Rounded.ConfirmationNumber,
                valueColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "N.º Transacciones",
                value = uiState.totalTransactionsCount.toString(),
                icon = Icons.Rounded.ShoppingCart,
                valueColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = valueColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TopProductCard(
    rank: Int,
    product: TopProductItem,
    currencySymbol: String,
    defaultDecimalPlaces: Int,
    allowExtraDecimals: Boolean,
    modifier: Modifier = Modifier
) {
    val revenueFormatted = formatCurrency(
        product.totalRevenue,
        currencySymbol,
        defaultDecimalPlaces,
        allowExtraDecimals
    )

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (rank == 1) Color(0xFFFFD700) else if (rank == 2) Color(0xFFC0C0C0) else if (rank == 3) Color(0xFFCD7F32) else MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(36.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "#$rank",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (rank <= 3) Color.Black else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${product.unitsSold} ud(s) vendida${if (product.unitsSold != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = revenueFormatted,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun PaymentMethodBreakdownCard(
    items: List<PaymentMethodBreakdownItem>,
    currencySymbol: String,
    defaultDecimalPlaces: Int,
    allowExtraDecimals: Boolean,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items.forEach { item ->
                val amountFormatted = formatCurrency(
                    item.totalAmount,
                    currencySymbol,
                    defaultDecimalPlaces,
                    allowExtraDecimals
                )
                val percentFormatted = String.format(Locale.US, "%.1f%%", item.percentage)

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.paymentMethodName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$amountFormatted ($percentFormatted)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (item.percentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        strokeCap = StrokeCap.Round,
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyDataCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    MovilPOSTheme {
        AnalyticsContent(
            uiState = AnalyticsUiState(
                selectedPeriod = AnalyticsPeriod.HOY,
                totalSalesAmount = 1250.50,
                totalGrossProfit = 450.20,
                totalTransactionsCount = 15,
                averageTicketAmount = 83.37,
                topSellingProducts = listOf(
                    TopProductItem("1", "Café Latte", 25, 75.0),
                    TopProductItem("2", "Sandwich de Jamón", 18, 90.0)
                ),
                paymentMethodBreakdown = listOf(
                    PaymentMethodBreakdownItem("Efectivo", 800.0, 64.0f),
                    PaymentMethodBreakdownItem("Tarjeta de Crédito / Débito", 450.50, 36.0f)
                )
            ),
            currencySymbol = "$",
            defaultDecimalPlaces = 2,
            allowExtraDecimals = true,
            onPeriodSelected = {},
            onNavigateBack = {}
        )
    }
}
