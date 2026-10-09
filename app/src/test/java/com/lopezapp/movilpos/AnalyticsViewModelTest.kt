package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.SaleItem
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.viewmodel.AnalyticsPeriod
import com.lopezapp.movilpos.ui.viewmodel.AnalyticsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AppRepository
    private lateinit var viewModel: AnalyticsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AppRepository()
        viewModel = AnalyticsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun analyticsViewModel_initialDataLoads() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val uiState = viewModel.uiState.value
        assertEquals(AnalyticsPeriod.HOY, uiState.selectedPeriod)
        assertTrue(uiState.totalTransactionsCount >= 0)
    }

    @Test
    fun analyticsViewModel_calculatesMetricsCorrectly() = runTest {
        val prod1 = Product(id = "p100", name = "Producto A", cost = 10.0, price = 20.0, stock = 50)
        val prod2 = Product(id = "p200", name = "Producto B", cost = 5.0, price = 15.0, stock = 50)
        repository.addProduct(prod1)
        repository.addProduct(prod2)

        val sale1 = Sale(
            id = "S1",
            customerId = "c1",
            customerName = "Cliente 1",
            invoiceType = InvoiceType.CONSUMIDOR_FINAL,
            paymentMethodId = "pm_cash",
            paymentMethodName = "Efectivo",
            items = listOf(
                SaleItem(productId = "p100", productName = "Producto A", quantity = 2, unitPrice = 20.0, subtotal = 40.0), // Profit = (20 - 10) * 2 = 20
                SaleItem(productId = "p200", productName = "Producto B", quantity = 1, unitPrice = 15.0, subtotal = 15.0)  // Profit = (15 - 5) * 1 = 10
            ),
            totalAmount = 55.0,
            dateMillis = System.currentTimeMillis()
        )

        val sale2 = Sale(
            id = "S2",
            customerId = "c2",
            customerName = "Cliente 2",
            invoiceType = InvoiceType.CREDITO_FISCAL,
            paymentMethodId = "pm_card",
            paymentMethodName = "Tarjeta de Crédito / Débito",
            items = listOf(
                SaleItem(productId = "p200", productName = "Producto B", quantity = 3, unitPrice = 15.0, subtotal = 45.0)  // Profit = (15 - 5) * 3 = 30
            ),
            totalAmount = 45.0,
            dateMillis = System.currentTimeMillis()
        )

        repository.addSale(sale1)
        repository.addSale(sale2)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onPeriodSelected(AnalyticsPeriod.TODO_EL_HISTORIAL)
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value

        // Initial sale in AppRepository has total 5.0 (2 coffees at price 2.5, cost 0.0 -> profit = 5.0)
        // Total sales = 5.0 + 55.0 + 45.0 = 105.0
        assertEquals(105.0, uiState.totalSalesAmount, 0.001)

        // Total transactions = 3
        assertEquals(3, uiState.totalTransactionsCount)

        // Ticket promedio = 105.0 / 3 = 35.0
        assertEquals(35.0, uiState.averageTicketAmount, 0.001)

        // Total profit = 5.0 (initial) + (20.0 + 10.0) + 30.0 = 65.0
        assertEquals(65.0, uiState.totalGrossProfit, 0.001)

        // Top products:
        // Producto B: 4 units sold (total revenue = 60.0)
        // Coffee (from initial dummy data): 2 units sold (total revenue = 5.0)
        // Producto A: 2 units sold (total revenue = 40.0)
        val topProducts = uiState.topSellingProducts
        assertTrue(topProducts.isNotEmpty())
        assertEquals("Producto B", topProducts.first().productName)
        assertEquals(4, topProducts.first().unitsSold)
        assertEquals(60.0, topProducts.first().totalRevenue, 0.001)

        // Payment method breakdown
        val breakdown = uiState.paymentMethodBreakdown
        assertTrue(breakdown.isNotEmpty())
        val cashPM = breakdown.find { it.paymentMethodName == "Efectivo" }
        assertNotNull(cashPM)
        assertEquals(60.0, cashPM!!.totalAmount, 0.001) // 5.0 initial + 55.0 sale1
        assertEquals(57.1428, cashPM.percentage.toDouble(), 0.1) // 60 / 105 * 100
    }

    @Test
    fun analyticsViewModel_periodFilteringWorks() = runTest {
        val calPastWeek = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -10) }
        val calPastMonth = Calendar.getInstance().apply { add(Calendar.MONTH, -2) }

        val saleOld = Sale(
            id = "S_OLD",
            customerId = "c1",
            customerName = "Old",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = listOf(
                SaleItem(productId = "p1", productName = "Old Item", quantity = 10, unitPrice = 10.0, subtotal = 100.0)
            ),
            totalAmount = 100.0,
            dateMillis = calPastMonth.timeInMillis
        )

        val salePastWeek = Sale(
            id = "S_WEEK",
            customerId = "c1",
            customerName = "Week",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = listOf(
                SaleItem(productId = "p2", productName = "Week Item", quantity = 5, unitPrice = 10.0, subtotal = 50.0)
            ),
            totalAmount = 50.0,
            dateMillis = calPastWeek.timeInMillis
        )

        repository.addSale(saleOld)
        repository.addSale(salePastWeek)
        testDispatcher.scheduler.advanceUntilIdle()

        // HOY filter (should only contain today's sales, e.g. initial sale or today's sales)
        viewModel.onPeriodSelected(AnalyticsPeriod.HOY)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.totalSalesAmount < 100.0)

        // TODO_EL_HISTORIAL filter
        viewModel.onPeriodSelected(AnalyticsPeriod.TODO_EL_HISTORIAL)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.totalSalesAmount >= 155.0)
    }

    @Test
    fun analyticsViewModel_voidedSalesExcludedFromAnalytics() = runTest {
        val saleToVoid = Sale(
            id = "S_VOID",
            customerId = "c1",
            customerName = "Void Customer",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = listOf(
                SaleItem(productId = "p1", productName = "Void Item", quantity = 5, unitPrice = 100.0, subtotal = 500.0)
            ),
            totalAmount = 500.0,
            dateMillis = System.currentTimeMillis()
        )

        repository.addSale(saleToVoid)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onPeriodSelected(AnalyticsPeriod.TODO_EL_HISTORIAL)
        testDispatcher.scheduler.advanceUntilIdle()

        val totalBeforeVoid = viewModel.uiState.value.totalSalesAmount
        assertTrue(totalBeforeVoid >= 500.0)

        // Void sale
        repository.voidSaleDte("S_VOID", "Anulación de prueba")
        testDispatcher.scheduler.advanceUntilIdle()

        val totalAfterVoid = viewModel.uiState.value.totalSalesAmount
        assertEquals(totalBeforeVoid - 500.0, totalAfterVoid, 0.001)
    }

    @Test
    fun analyticsViewModel_topProductsLimitToFive() = runTest {
        for (i in 1..10) {
            val product = Product(id = "prod_$i", name = "Producto $i", price = 10.0, stock = 100)
            repository.addProduct(product)
            val sale = Sale(
                id = "sale_$i",
                customerId = "c1",
                customerName = "Customer",
                paymentMethodId = "pm1",
                paymentMethodName = "Efectivo",
                items = listOf(
                    SaleItem(productId = "prod_$i", productName = "Producto $i", quantity = i, unitPrice = 10.0, subtotal = i * 10.0)
                ),
                totalAmount = i * 10.0,
                dateMillis = System.currentTimeMillis()
            )
            repository.addSale(sale)
        }

        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onPeriodSelected(AnalyticsPeriod.TODO_EL_HISTORIAL)
        testDispatcher.scheduler.advanceUntilIdle()

        val topProducts = viewModel.topSellingProducts.value
        assertEquals(5, topProducts.size)
        // Highest units sold should be #1 (Producto 10 with 10 units sold)
        assertEquals("Producto 10", topProducts.first().productName)
        assertEquals(10, topProducts.first().unitsSold)
    }
}
