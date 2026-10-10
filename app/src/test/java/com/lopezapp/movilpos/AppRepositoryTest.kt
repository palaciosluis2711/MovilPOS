package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.BluetoothPrinterConfig
import com.lopezapp.movilpos.data.model.CreditStatus
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.CustomerPayment
import com.lopezapp.movilpos.data.model.DocumentType
import com.lopezapp.movilpos.data.model.Expense
import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.BundleItem
import com.lopezapp.movilpos.data.model.Purchase
import com.lopezapp.movilpos.data.model.PurchaseItem
import com.lopezapp.movilpos.data.model.Role
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.SaleItem
import com.lopezapp.movilpos.data.model.ShiftStatus
import com.lopezapp.movilpos.data.model.User
import com.lopezapp.movilpos.data.repository.AppRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppRepositoryTest {

    private lateinit var repository: AppRepository

    @Before
    fun setUp() {
        repository = AppRepository()
    }

    @Test
    fun defaultUsers_containsAdmin() {
        val users = repository.users.value
        assertEquals(1, users.size)
        val admin = users.first()
        assertEquals("admin_1", admin.id)
        assertEquals("Administrador", admin.name)
        assertEquals("1234", admin.pin)
        assertEquals(Role.ADMINISTRATOR, admin.role)
        assertTrue(admin.isActive)
    }

    @Test
    fun userCrud_addsUpdatesAndDeletesUser() {
        val cashier = User(id = "u2", name = "Juan Pérez", pin = "5678", role = Role.CASHIER)
        repository.addUser(cashier)
        assertEquals(2, repository.users.value.size)
        assertTrue(repository.users.value.any { it.id == "u2" })

        val updatedCashier = cashier.copy(name = "Juan Carlos Pérez")
        repository.updateUser(updatedCashier)
        val fetched = repository.users.value.find { it.id == "u2" }
        assertNotNull(fetched)
        assertEquals("Juan Carlos Pérez", fetched?.name)

        repository.deleteUser("u2")
        assertEquals(1, repository.users.value.size)
        assertNull(repository.users.value.find { it.id == "u2" })
    }

    @Test
    fun updateUser_updatesActiveShiftCashierNameWhenActiveShiftMatchesUser() {
        val cashier = User(id = "c1", name = "Cajero Inicial", pin = "1111", role = Role.CASHIER)
        repository.addUser(cashier)
        repository.openShift(cashier, initialFloat = 50.0)

        assertEquals("Cajero Inicial", repository.activeShift.value?.cashierName)

        val updatedCashier = cashier.copy(name = "Cajero Actualizado")
        repository.updateUser(updatedCashier)

        assertEquals("Cajero Actualizado", repository.activeShift.value?.cashierName)
    }

    @Test
    fun validateAdminPin_validatesCorrectRolesAndActiveStatus() {
        // Admin default PIN "1234"
        assertTrue(repository.validateAdminPin("1234"))
        assertFalse(repository.validateAdminPin("9999"))

        // Add Supervisor
        val supervisor = User(id = "sup_1", name = "Supervisor", pin = "4321", role = Role.SUPERVISOR)
        repository.addUser(supervisor)
        assertTrue(repository.validateAdminPin("4321"))

        // Add Cashier
        val cashier = User(id = "cash_1", name = "Cajero", pin = "1111", role = Role.CASHIER)
        repository.addUser(cashier)
        assertFalse(repository.validateAdminPin("1111"))

        // Deactivate Supervisor
        repository.updateUser(supervisor.copy(isActive = false))
        assertFalse(repository.validateAdminPin("4321"))
    }

    @Test
    fun shiftLifecycle_opensAndClosesShift() {
        assertNull(repository.activeShift.value)
        assertTrue(repository.shiftHistory.value.isEmpty())

        val cashier = repository.users.value.first()
        val shift = repository.openShift(cashier, initialFloat = 100.0)

        assertNotNull(repository.activeShift.value)
        assertEquals(shift.id, repository.activeShift.value?.id)
        assertEquals("admin_1", repository.activeShift.value?.cashierId)
        assertEquals(100.0, repository.activeShift.value?.initialFloat ?: 0.0, 0.001)
        assertEquals(ShiftStatus.OPEN, repository.activeShift.value?.status)
        assertEquals(1, repository.shiftHistory.value.size)

        val closed = repository.closeShift(countedCash = 105.0)
        assertNotNull(closed)
        assertEquals(ShiftStatus.CLOSED, closed?.status)
        assertNotNull(closed?.closedAtMillis)
        assertEquals(105.0, closed?.actualCashCounted ?: 0.0, 0.001)
        assertEquals(5.0, closed?.difference ?: 0.0, 0.001)

        assertNull(repository.activeShift.value)
        assertEquals(1, repository.shiftHistory.value.size)
        assertEquals(ShiftStatus.CLOSED, repository.shiftHistory.value.first().status)
    }

    @Test
    fun updateActiveShiftSales_updatesTotalsAndHistory() {
        val cashier = repository.users.value.first()
        repository.openShift(cashier, initialFloat = 50.0)

        repository.updateActiveShiftSales(cashAmount = 20.0, cardAmount = 15.0, otherAmount = 10.0)

        val active = repository.activeShift.value
        assertNotNull(active)
        assertEquals(20.0, active?.totalCashSales ?: 0.0, 0.001)
        assertEquals(15.0, active?.totalCardSales ?: 0.0, 0.001)
        assertEquals(10.0, active?.totalOtherSales ?: 0.0, 0.001)
        assertEquals(70.0, active?.expectedCash ?: 0.0, 0.001)

        val inHistory = repository.shiftHistory.value.first()
        assertEquals(20.0, inHistory.totalCashSales, 0.001)
    }

    @Test
    fun addSale_withActiveShift_assignsShiftAndUpdatesSalesTotals() {
        val cashier = repository.users.value.first()
        val shift = repository.openShift(cashier, initialFloat = 50.0)

        val cashSale = Sale(
            customerId = "c1",
            customerName = "Cliente General",
            invoiceType = InvoiceType.CONSUMIDOR_FINAL,
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = listOf(SaleItem("p1", "Product 1", 1, 10.0, 10.0)),
            totalAmount = 10.0
        )
        repository.addSale(cashSale)

        val recordedSale = repository.sales.value.last()
        assertEquals(shift.id, recordedSale.shiftId)
        assertEquals(cashier.id, recordedSale.cashierId)
        assertEquals(cashier.name, recordedSale.cashierName)

        val updatedShift = repository.activeShift.value
        assertEquals(10.0, updatedShift?.totalCashSales ?: 0.0, 0.001)
        assertEquals(60.0, updatedShift?.expectedCash ?: 0.0, 0.001)

        val cardSale = Sale(
            customerId = "c1",
            customerName = "Cliente General",
            invoiceType = InvoiceType.CONSUMIDOR_FINAL,
            paymentMethodId = "pm2",
            paymentMethodName = "Tarjeta de Crédito",
            items = listOf(SaleItem("p2", "Product 2", 1, 25.0, 25.0)),
            totalAmount = 25.0
        )
        repository.addSale(cardSale)

        val finalShift = repository.activeShift.value
        assertEquals(10.0, finalShift?.totalCashSales ?: 0.0, 0.001)
        assertEquals(25.0, finalShift?.totalCardSales ?: 0.0, 0.001)
        assertEquals(60.0, finalShift?.expectedCash ?: 0.0, 0.001)
    }

    @Test
    fun voidSaleDte_voidsSaleCorrectly() {
        val sale = Sale(
            id = "sale_123",
            customerId = "c1",
            customerName = "Cliente",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 50.0,
            isDteIssued = true,
            dteGenerationCode = "GEN-123"
        )
        repository.addSale(sale)

        val result = repository.voidSaleDte("sale_123", "Error en cobro")
        assertTrue(result.isSuccess)

        val voidedSale = repository.sales.value.find { it.id == "sale_123" }
        assertNotNull(voidedSale)
        assertTrue(voidedSale!!.isVoided)
        assertEquals("Error en cobro", voidedSale.voidReason)
        assertNotNull(voidedSale.voidedAtMillis)
    }

    @Test
    fun addToContingencyQueue_setsContingencyModeTrueAndAddsToQueue() {
        val sale = Sale(
            id = "sale_contingency_1",
            customerId = "c1",
            customerName = "Cliente Contingencia",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 30.0,
            isDteIssued = false
        )
        repository.addToContingencyQueue(sale)

        val contingencyList = repository.contingencyDtes.value
        assertEquals(1, contingencyList.size)
        assertTrue(contingencyList.first().contingencyMode)

        val savedSale = repository.sales.value.find { it.id == "sale_contingency_1" }
        assertNotNull(savedSale)
        assertTrue(savedSale!!.contingencyMode)
    }

    @Test
    fun retryContingencyTransmissions_retransmitsAndClearsQueue() {
        val sale = Sale(
            id = "sale_contingency_2",
            customerId = "c1",
            customerName = "Cliente Retry",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 45.0,
            isDteIssued = false
        )
        repository.addToContingencyQueue(sale)
        assertEquals(1, repository.contingencyDtes.value.size)

        val result = repository.retryContingencyTransmissions()
        assertTrue(result.isSuccess)
        val count = result.getOrNull()
        assertEquals(1, count)

        assertTrue(repository.contingencyDtes.value.isEmpty())
        val updatedSale = repository.sales.value.find { it.id == "sale_contingency_2" }
        assertNotNull(updatedSale)
        assertTrue(updatedSale!!.isDteIssued)
        assertFalse(updatedSale.contingencyMode)
        assertNotNull(updatedSale.dteReceptionSeal)
    }

    @Test
    fun retryContingencyTransmissions_realMode_missingCredentials_failsAndKeepsQueue() {
        val currentConfig = repository.electronicBillingConfig.value
        repository.updateElectronicBillingConfig(
            currentConfig.copy(
                isSimulationMode = false,
                nit = "",
                apiToken = "",
                certificateUri = null
            )
        )

        val sale = Sale(
            id = "sale_contingency_real_1",
            customerId = "c1",
            customerName = "Cliente Real Mode",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 50.0,
            isDteIssued = false
        )
        repository.addToContingencyQueue(sale)
        assertEquals(1, repository.contingencyDtes.value.size)

        val result = repository.retryContingencyTransmissions()
        assertTrue(result.isFailure)
        assertEquals("Faltan credenciales DTE o certificado .p12 para retransmitir", result.exceptionOrNull()?.message)
        assertEquals(1, repository.contingencyDtes.value.size)
    }

    @Test
    fun retryContingencyTransmissions_realMode_invalidCertificate_failsAndKeepsQueue() {
        val currentConfig = repository.electronicBillingConfig.value
        repository.updateElectronicBillingConfig(
            currentConfig.copy(
                isSimulationMode = false,
                nit = "06140101011015",
                apiToken = "some_api_token",
                certificateUri = "/non/existent/path/cert.p12"
            )
        )

        val sale = Sale(
            id = "sale_contingency_real_2",
            customerId = "c1",
            customerName = "Cliente Real Mode Cert Error",
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = emptyList(),
            totalAmount = 50.0,
            isDteIssued = false
        )
        repository.addToContingencyQueue(sale)
        assertEquals(1, repository.contingencyDtes.value.size)

        val result = repository.retryContingencyTransmissions()
        assertTrue(result.isFailure)
        assertEquals("Faltan credenciales DTE o certificado .p12 para retransmitir", result.exceptionOrNull()?.message)
        assertEquals(1, repository.contingencyDtes.value.size)
    }

    @Test
    fun expenses_initialList_containsSampleExpense() {
        val initialExpenses = repository.expenses.value
        assertEquals(1, initialExpenses.size)
        assertEquals("exp_1", initialExpenses.first().id)
        assertEquals(12.50, initialExpenses.first().amount, 0.001)
    }

    @Test
    fun addExpense_withoutActiveShift_addsExpenseToList() {
        val expense = Expense(
            category = "Insumos",
            description = "Compra de papelería",
            amount = 25.0
        )
        repository.addExpense(expense)

        val updatedList = repository.expenses.value
        assertEquals(2, updatedList.size)
        val added = updatedList.last()
        assertEquals("Insumos", added.category)
        assertEquals(25.0, added.amount, 0.001)
        assertNull(added.shiftId)
        assertEquals("", added.cashierId)
        assertEquals("", added.cashierName)
    }

    @Test
    fun addExpense_withActiveShift_associatesShiftDetailsAndUpdatesShiftTotalsAndExpectedCash() {
        val cashier = User(id = "c1", name = "Juan Cajero", pin = "1111", role = Role.CASHIER)
        repository.addUser(cashier)
        val shift = repository.openShift(cashier, initialFloat = 100.0)

        val cashSale = Sale(
            customerId = "cust1",
            customerName = "Cliente General",
            invoiceType = InvoiceType.CONSUMIDOR_FINAL,
            paymentMethodId = "pm1",
            paymentMethodName = "Efectivo",
            items = listOf(SaleItem("p1", "Producto 1", 1, 50.0, 50.0)),
            totalAmount = 50.0
        )
        repository.addSale(cashSale)

        // At this point: initialFloat = 100.0, totalCashSales = 50.0, totalExpenses = 0.0
        // expectedCash = 150.0
        val activeShiftBefore = repository.activeShift.value
        assertNotNull(activeShiftBefore)
        assertEquals(150.0, activeShiftBefore?.expectedCash ?: 0.0, 0.001)

        val expense = Expense(
            id = "exp_shift_1",
            category = "Transporte",
            description = "Flete de mercadería",
            amount = 30.0
        )
        repository.addExpense(expense)

        val savedExpense = repository.expenses.value.find { it.id == "exp_shift_1" }
        assertNotNull(savedExpense)
        assertEquals(shift.id, savedExpense?.shiftId)
        assertEquals("c1", savedExpense?.cashierId)
        assertEquals("Juan Cajero", savedExpense?.cashierName)

        val updatedActiveShift = repository.activeShift.value
        assertNotNull(updatedActiveShift)
        assertEquals(30.0, updatedActiveShift?.totalExpenses ?: 0.0, 0.001)
        // expectedCash = maxOf(0.0, 100.0 + 50.0 - 30.0) = 120.0
        assertEquals(120.0, updatedActiveShift?.expectedCash ?: 0.0, 0.001)

        val shiftInHistory = repository.shiftHistory.value.find { it.id == shift.id }
        assertNotNull(shiftInHistory)
        assertEquals(30.0, shiftInHistory?.totalExpenses ?: 0.0, 0.001)
        assertEquals(120.0, shiftInHistory?.expectedCash ?: 0.0, 0.001)
    }

    @Test
    fun deleteExpense_removesExpenseFromList() {
        val expense = Expense(
            id = "exp_to_delete",
            category = "Mantenimiento",
            description = "Reparación de bombilla",
            amount = 5.0
        )
        repository.addExpense(expense)
        assertTrue(repository.expenses.value.any { it.id == "exp_to_delete" })

        repository.deleteExpense("exp_to_delete")
        assertFalse(repository.expenses.value.any { it.id == "exp_to_delete" })
    }

    @Test
    fun customerPayments_initialValue_isEmpty() {
        assertTrue(repository.customerPayments.value.isEmpty())
    }

    @Test
    fun addSale_creditSale_initializesCreditFieldsAndUpdatesCustomerDebt() {
        val customer = Customer(
            id = "cust_credit_1",
            name = "Cliente Crédito 1",
            documentType = DocumentType.DUI,
            documentNumber = "12345678-9",
            department = "San Salvador",
            municipality = "San Salvador Centro",
            district = "San Salvador"
        )
        repository.addCustomer(customer)

        val creditSale = Sale(
            id = "sale_credit_1",
            customerId = "cust_credit_1",
            customerName = "Cliente Crédito 1",
            paymentMethodId = "pm1",
            paymentMethodName = "Crédito",
            items = listOf(SaleItem("p1", "Producto 1", 1, 150.0, 150.0)),
            totalAmount = 150.0,
            isCredit = true
        )
        repository.addSale(creditSale)

        val savedSale = repository.sales.value.find { it.id == "sale_credit_1" }
        assertNotNull(savedSale)
        assertTrue(savedSale!!.isCredit)
        assertEquals(150.0, savedSale.remainingBalance, 0.001)
        assertEquals(0.0, savedSale.paidAmount, 0.001)
        assertEquals(CreditStatus.UNPAID, savedSale.creditStatus)

        val updatedCustomer = repository.customers.value.find { it.id == "cust_credit_1" }
        assertNotNull(updatedCustomer)
        assertEquals(150.0, updatedCustomer!!.currentDebt, 0.001)
    }

    @Test
    fun addCustomerPayment_withSaleId_updatesSaleAndCustomerDebt() {
        val customer = Customer(
            id = "cust_credit_2",
            name = "Cliente Crédito 2",
            documentType = DocumentType.DUI,
            documentNumber = "87654321-0",
            department = "San Salvador",
            municipality = "San Salvador Centro",
            district = "San Salvador"
        )
        repository.addCustomer(customer)

        val creditSale = Sale(
            id = "sale_credit_2",
            customerId = "cust_credit_2",
            customerName = "Cliente Crédito 2",
            paymentMethodId = "pm1",
            paymentMethodName = "Crédito",
            items = listOf(SaleItem("p1", "Producto 1", 1, 100.0, 100.0)),
            totalAmount = 100.0,
            isCredit = true
        )
        repository.addSale(creditSale)

        val payment1 = CustomerPayment(
            id = "pay_1",
            customerId = "cust_credit_2",
            customerName = "Cliente Crédito 2",
            saleId = "sale_credit_2",
            amount = 40.0
        )
        repository.addCustomerPayment(payment1)

        assertEquals(1, repository.customerPayments.value.size)
        val saleAfterPay1 = repository.sales.value.find { it.id == "sale_credit_2" }
        assertNotNull(saleAfterPay1)
        assertEquals(40.0, saleAfterPay1!!.paidAmount, 0.001)
        assertEquals(60.0, saleAfterPay1.remainingBalance, 0.001)
        assertEquals(CreditStatus.PARTIALLY_PAID, saleAfterPay1.creditStatus)

        val customerAfterPay1 = repository.customers.value.find { it.id == "cust_credit_2" }
        assertEquals(60.0, customerAfterPay1!!.currentDebt, 0.001)

        val payment2 = CustomerPayment(
            id = "pay_2",
            customerId = "cust_credit_2",
            customerName = "Cliente Crédito 2",
            saleId = "sale_credit_2",
            amount = 60.0
        )
        repository.addCustomerPayment(payment2)

        assertEquals(2, repository.customerPayments.value.size)
        val saleAfterPay2 = repository.sales.value.find { it.id == "sale_credit_2" }
        assertNotNull(saleAfterPay2)
        assertEquals(100.0, saleAfterPay2!!.paidAmount, 0.001)
        assertEquals(0.0, saleAfterPay2.remainingBalance, 0.001)
        assertEquals(CreditStatus.PAID, saleAfterPay2.creditStatus)

        val customerAfterPay2 = repository.customers.value.find { it.id == "cust_credit_2" }
        assertEquals(0.0, customerAfterPay2!!.currentDebt, 0.001)
    }

    @Test
    fun addCustomerPayment_withoutSaleId_updatesCustomerDebt() {
        val customer = Customer(
            id = "cust_credit_3",
            name = "Cliente Crédito 3",
            documentType = DocumentType.DUI,
            documentNumber = "11223344-5",
            department = "San Salvador",
            municipality = "San Salvador Centro",
            district = "San Salvador"
        )
        repository.addCustomer(customer)

        val creditSale = Sale(
            id = "sale_credit_3",
            customerId = "cust_credit_3",
            customerName = "Cliente Crédito 3",
            paymentMethodId = "pm1",
            paymentMethodName = "Crédito",
            items = listOf(SaleItem("p1", "Producto 1", 1, 200.0, 200.0)),
            totalAmount = 200.0,
            isCredit = true
        )
        repository.addSale(creditSale)

        val generalPayment = CustomerPayment(
            id = "pay_3",
            customerId = "cust_credit_3",
            customerName = "Cliente Crédito 3",
            saleId = null,
            amount = 50.0
        )
        repository.addCustomerPayment(generalPayment)

        assertEquals(1, repository.customerPayments.value.size)
        val customerAfterPay = repository.customers.value.find { it.id == "cust_credit_3" }
        assertEquals(150.0, customerAfterPay!!.currentDebt, 0.001)
    }

    @Test
    fun bluetoothPrinterConfig_defaultAndUpdates() {
        val defaultConfig = repository.bluetoothPrinterConfig.value
        assertNull(defaultConfig.macAddress)
        assertNull(defaultConfig.deviceName)
        assertFalse(defaultConfig.isConnected)
        assertTrue(defaultConfig.autoPrintSales)

        val newConfig = BluetoothPrinterConfig(
            macAddress = "00:11:22:33:44:55",
            deviceName = "POS Printer",
            isConnected = true,
            autoPrintSales = false
        )
        repository.updateBluetoothPrinterConfig(newConfig)

        val updated = repository.bluetoothPrinterConfig.value
        assertEquals("00:11:22:33:44:55", updated.macAddress)
        assertEquals("POS Printer", updated.deviceName)
        assertTrue(updated.isConnected)
        assertFalse(updated.autoPrintSales)
    }



    @Test
    fun bundleDynamicStock_calculatesMinOfComponentStockOverRequiredQuantity() {
        val comp1 = Product(id = "c1", name = "Ingrediente A", stock = 10, price = 1.0)
        val comp2 = Product(id = "c2", name = "Ingrediente B", stock = 15, price = 1.0)
        val bundle = Product(
            id = "b1",
            name = "Combo Express",
            price = 20.0,
            stock = 0,
            isBundle = true,
            bundleItems = listOf(
                BundleItem("c1", 2), // 10 / 2 = 5
                BundleItem("c2", 3)  // 15 / 3 = 5
            )
        )
        repository.addProduct(comp1)
        repository.addProduct(comp2)
        repository.addProduct(bundle)

        var currentBundle = repository.products.value.find { it.id == "b1" }
        assertEquals(5, currentBundle?.stock)

        // Reduce stock of comp1 so it becomes the limiting factor (4 / 2 = 2)
        repository.updateProduct(comp1.copy(stock = 4))
        currentBundle = repository.products.value.find { it.id == "b1" }
        assertEquals(2, currentBundle?.stock)
    }

    @Test
    fun serviceDynamicStock_unlimitedStockWhenNoComponents() {
        val service = Product(
            id = "s1",
            name = "Consulta General",
            price = 30.0,
            stock = 0,
            isService = true,
            bundleItems = emptyList()
        )
        repository.addProduct(service)

        val currentService = repository.products.value.find { it.id == "s1" }
        assertEquals(9999, currentService?.stock)
    }

    @Test
    fun serviceDynamicStock_calculatedStockWhenComponentsPresent() {
        val comp = Product(id = "sc1", name = "Insumo Médico", stock = 20, price = 2.0)
        val service = Product(
            id = "s2",
            name = "Curación",
            price = 40.0,
            stock = 0,
            isService = true,
            bundleItems = listOf(BundleItem("sc1", 4)) // 20 / 4 = 5
        )
        repository.addProduct(comp)
        repository.addProduct(service)

        val currentService = repository.products.value.find { it.id == "s2" }
        assertEquals(5, currentService?.stock)
    }
}
