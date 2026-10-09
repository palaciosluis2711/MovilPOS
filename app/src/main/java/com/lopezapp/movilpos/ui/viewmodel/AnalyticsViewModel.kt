package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class AnalyticsPeriod(val label: String) {
    HOY("Hoy"),
    ESTA_SEMANA("Esta Semana"),
    ESTE_MES("Este Mes"),
    TODO_EL_HISTORIAL("Todo")
}

data class TopProductItem(
    val productId: String,
    val productName: String,
    val unitsSold: Int,
    val totalRevenue: Double
)

data class PaymentMethodBreakdownItem(
    val paymentMethodName: String,
    val totalAmount: Double,
    val percentage: Float
)

data class AnalyticsUiState(
    val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.HOY,
    val totalSalesAmount: Double = 0.0,
    val totalGrossProfit: Double = 0.0,
    val totalTransactionsCount: Int = 0,
    val averageTicketAmount: Double = 0.0,
    val topSellingProducts: List<TopProductItem> = emptyList(),
    val paymentMethodBreakdown: List<PaymentMethodBreakdownItem> = emptyList()
)

class AnalyticsViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(AnalyticsPeriod.HOY)
    val selectedPeriod: StateFlow<AnalyticsPeriod> = _selectedPeriod.asStateFlow()

    val uiState: StateFlow<AnalyticsUiState> = combine(
        repository.sales,
        repository.products,
        _selectedPeriod
    ) { salesList, productsList, period ->
        val filteredSales = filterSalesByPeriod(salesList, period)
        calculateAnalyticsUiState(filteredSales, productsList, period)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AnalyticsUiState()
    )

    val totalSalesAmount: StateFlow<Double> = uiState
        .map { it.totalSalesAmount }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val totalGrossProfit: StateFlow<Double> = uiState
        .map { it.totalGrossProfit }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val totalTransactionsCount: StateFlow<Int> = uiState
        .map { it.totalTransactionsCount }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val averageTicketAmount: StateFlow<Double> = uiState
        .map { it.averageTicketAmount }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val topSellingProducts: StateFlow<List<TopProductItem>> = uiState
        .map { it.topSellingProducts }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val paymentMethodBreakdown: StateFlow<List<PaymentMethodBreakdownItem>> = uiState
        .map { it.paymentMethodBreakdown }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun onPeriodSelected(period: AnalyticsPeriod) {
        _selectedPeriod.value = period
    }

    private fun filterSalesByPeriod(
        sales: List<Sale>,
        period: AnalyticsPeriod
    ): List<Sale> {
        val nonVoidedSales = sales.filter { !it.isVoided }
        if (period == AnalyticsPeriod.TODO_EL_HISTORIAL) {
            return nonVoidedSales
        }

        val startMillis = getStartOfPeriodMillis(period)
        return nonVoidedSales.filter { it.dateMillis >= startMillis }
    }

    private fun getStartOfPeriodMillis(period: AnalyticsPeriod): Long {
        val cal = Calendar.getInstance()
        return when (period) {
            AnalyticsPeriod.HOY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            AnalyticsPeriod.ESTA_SEMANA -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
                cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
                cal.timeInMillis
            }
            AnalyticsPeriod.ESTE_MES -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            AnalyticsPeriod.TODO_EL_HISTORIAL -> 0L
        }
    }

    private fun calculateAnalyticsUiState(
        sales: List<Sale>,
        products: List<Product>,
        period: AnalyticsPeriod
    ): AnalyticsUiState {
        val productsMap = products.associateBy { it.id }

        val totalSalesAmount = sales.sumOf { it.totalAmount }
        val totalTransactionsCount = sales.size
        val averageTicketAmount = if (totalTransactionsCount > 0) {
            totalSalesAmount / totalTransactionsCount
        } else {
            0.0
        }

        var totalGrossProfit = 0.0
        val productSalesMap = mutableMapOf<String, ProductSalesAccumulator>()

        for (sale in sales) {
            for (item in sale.items) {
                val productCost = productsMap[item.productId]?.cost ?: 0.0
                val profitPerItem = (item.unitPrice - productCost) * item.quantity
                totalGrossProfit += profitPerItem

                val key = item.productId.ifBlank { item.productName }
                val accum = productSalesMap.getOrPut(key) {
                    ProductSalesAccumulator(item.productId, item.productName, 0, 0.0)
                }
                productSalesMap[key] = accum.copy(
                    unitsSold = accum.unitsSold + item.quantity,
                    totalRevenue = accum.totalRevenue + item.subtotal
                )
            }
        }

        val topSellingProducts = productSalesMap.values
            .sortedWith(compareByDescending<ProductSalesAccumulator> { it.unitsSold }.thenByDescending { it.totalRevenue })
            .take(5)
            .map {
                TopProductItem(
                    productId = it.productId,
                    productName = it.productName,
                    unitsSold = it.unitsSold,
                    totalRevenue = it.totalRevenue
                )
            }

        val paymentMethodSales = sales.groupBy { it.paymentMethodName.ifBlank { "Efectivo" } }
            .mapValues { entry -> entry.value.sumOf { it.totalAmount } }

        val paymentMethodBreakdown = paymentMethodSales.map { (pmName, amount) ->
            val percentage = if (totalSalesAmount > 0) {
                ((amount / totalSalesAmount) * 100).toFloat()
            } else {
                0f
            }
            PaymentMethodBreakdownItem(
                paymentMethodName = pmName,
                totalAmount = amount,
                percentage = percentage
            )
        }.sortedByDescending { it.totalAmount }

        return AnalyticsUiState(
            selectedPeriod = period,
            totalSalesAmount = totalSalesAmount,
            totalGrossProfit = totalGrossProfit,
            totalTransactionsCount = totalTransactionsCount,
            averageTicketAmount = averageTicketAmount,
            topSellingProducts = topSellingProducts,
            paymentMethodBreakdown = paymentMethodBreakdown
        )
    }

    private data class ProductSalesAccumulator(
        val productId: String,
        val productName: String,
        val unitsSold: Int,
        val totalRevenue: Double
    )

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
                return AnalyticsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
