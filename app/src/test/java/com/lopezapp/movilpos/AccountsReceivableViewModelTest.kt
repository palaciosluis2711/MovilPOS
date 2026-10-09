package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.DocumentType
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.SaleItem
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.viewmodel.AccountsReceivableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountsReceivableViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AppRepository
    private lateinit var viewModel: AccountsReceivableViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AppRepository()
        viewModel = AccountsReceivableViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasZeroDebtAndNoDebtorCustomers() = runTest {
        assertEquals(0.0, viewModel.totalAccountsReceivable.value, 0.001)
        assertTrue(viewModel.debtorCustomers.value.isEmpty())
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun creditSale_createsDebtorCustomerAndTotalReceivable() = runTest {
        val debtor = Customer(
            id = "debtor_1",
            name = "Juan Deudor",
            documentType = DocumentType.DUI,
            documentNumber = "12345678-9",
            phone = "7000-0000",
            department = "San Salvador",
            municipality = "San Salvador Centro",
            district = "San Salvador"
        )
        repository.addCustomer(debtor)

        val creditSale = Sale(
            id = "sale_credit_1",
            customerId = "debtor_1",
            customerName = "Juan Deudor",
            paymentMethodId = "pm_credit",
            paymentMethodName = "Crédito",
            items = listOf(SaleItem("p1", "Producto Test", 1, 150.0, 150.0)),
            totalAmount = 150.0,
            isCredit = true
        )
        repository.addSale(creditSale)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(150.0, viewModel.totalAccountsReceivable.value, 0.001)
        val debtors = viewModel.debtorCustomers.value
        assertEquals(1, debtors.size)
        assertEquals("Juan Deudor", debtors[0].name)
        assertEquals(150.0, debtors[0].currentDebt, 0.001)
    }

    @Test
    fun searchQuery_filtersDebtorCustomersByNameOrDocument() = runTest {
        val c1 = Customer(
            id = "c1",
            name = "Carlos Fuentes",
            documentType = DocumentType.DUI,
            documentNumber = "11111111-1",
            department = "San Salvador",
            municipality = "San Salvador Centro",
            district = "San Salvador"
        )
        val c2 = Customer(
            id = "c2",
            name = "Maria Lopez",
            documentType = DocumentType.DUI,
            documentNumber = "22222222-2",
            department = "San Salvador",
            municipality = "San Salvador Centro",
            district = "San Salvador"
        )
        repository.addCustomer(c1)
        repository.addCustomer(c2)

        repository.addSale(
            Sale(
                id = "s1",
                customerId = "c1",
                customerName = "Carlos Fuentes",
                paymentMethodId = "pm1",
                paymentMethodName = "Crédito",
                items = listOf(SaleItem("p1", "Item", 1, 100.0, 100.0)),
                totalAmount = 100.0,
                isCredit = true
            )
        )
        repository.addSale(
            Sale(
                id = "s2",
                customerId = "c2",
                customerName = "Maria Lopez",
                paymentMethodId = "pm1",
                paymentMethodName = "Crédito",
                items = listOf(SaleItem("p1", "Item", 1, 200.0, 200.0)),
                totalAmount = 200.0,
                isCredit = true
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.debtorCustomers.value.size)

        // Filter by name
        viewModel.onSearchQueryChanged("Carlos")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.debtorCustomers.value.size)
        assertEquals("Carlos Fuentes", viewModel.debtorCustomers.value[0].name)

        // Filter by document
        viewModel.onSearchQueryChanged("22222222")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.debtorCustomers.value.size)
        assertEquals("Maria Lopez", viewModel.debtorCustomers.value[0].name)

        // Clear filter
        viewModel.onSearchQueryChanged("")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.debtorCustomers.value.size)
    }

    @Test
    fun registerPayment_withSaleId_reducesDebtAndUpdatesSale() = runTest {
        val debtor = Customer(
            id = "debtor_2",
            name = "Pedro Gomez",
            documentType = DocumentType.DUI,
            documentNumber = "33333333-3",
            department = "La Libertad",
            municipality = "La Libertad Sur",
            district = "Santa Tecla"
        )
        repository.addCustomer(debtor)

        val creditSale = Sale(
            id = "sale_100",
            customerId = "debtor_2",
            customerName = "Pedro Gomez",
            paymentMethodId = "pm1",
            paymentMethodName = "Crédito",
            items = listOf(SaleItem("p1", "Item", 1, 100.0, 100.0)),
            totalAmount = 100.0,
            isCredit = true
        )
        repository.addSale(creditSale)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.registerPayment(
            customerId = "debtor_2",
            saleId = "sale_100",
            amount = 40.0,
            paymentMethodName = "Efectivo",
            notes = "Abono parcial"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(60.0, viewModel.totalAccountsReceivable.value, 0.001)

        val pendingSales = viewModel.getPendingCreditSalesForCustomer("debtor_2")
        assertEquals(1, pendingSales.size)
        assertEquals(60.0, pendingSales[0].remainingBalance, 0.001)

        val payments = viewModel.getPaymentHistoryForCustomer("debtor_2")
        assertEquals(1, payments.size)
        assertEquals(40.0, payments[0].amount, 0.001)
        assertEquals("Efectivo", payments[0].paymentMethodName)
        assertEquals("Abono parcial", payments[0].notes)
    }

    @Test
    fun registerPayment_withoutSaleId_reducesDebtAndUpdatesPendingSales() = runTest {
        val debtor = Customer(
            id = "debtor_3",
            name = "Ana Martinez",
            documentType = DocumentType.DUI,
            documentNumber = "44444444-4",
            department = "Santa Ana",
            municipality = "Santa Ana Centro",
            district = "Santa Ana"
        )
        repository.addCustomer(debtor)

        val creditSale = Sale(
            id = "sale_200",
            customerId = "debtor_3",
            customerName = "Ana Martinez",
            paymentMethodId = "pm1",
            paymentMethodName = "Crédito",
            items = listOf(SaleItem("p1", "Item", 1, 200.0, 200.0)),
            totalAmount = 200.0,
            isCredit = true
        )
        repository.addSale(creditSale)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.registerPayment(
            customerId = "debtor_3",
            saleId = null,
            amount = 50.0,
            paymentMethodName = "Transferencia",
            notes = null
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(150.0, viewModel.totalAccountsReceivable.value, 0.001)

        val pendingSales = viewModel.getPendingCreditSalesForCustomer("debtor_3")
        assertEquals(1, pendingSales.size)
        assertEquals(150.0, pendingSales[0].remainingBalance, 0.001)

        val payments = viewModel.getPaymentHistoryForCustomer("debtor_3")
        assertEquals(1, payments.size)
        assertEquals(50.0, payments[0].amount, 0.001)
        assertEquals("Transferencia", payments[0].paymentMethodName)
    }
}
