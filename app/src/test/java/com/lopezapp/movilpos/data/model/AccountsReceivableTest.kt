package com.lopezapp.movilpos.data.model

import com.lopezapp.movilpos.data.repository.AppRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AccountsReceivableTest {

    private lateinit var repository: AppRepository

    @Before
    fun setUp() {
        repository = AppRepository()
    }

    // ------------------------------------------------------------------
    // 1. CustomerPayment Model Instantiation and Properties
    // ------------------------------------------------------------------

    @Test
    fun customerPayment_instantiationWithCustomValues_setsAllPropertiesCorrectly() {
        val customDate = 1700000000000L
        val payment = CustomerPayment(
            id = "pay_custom_1",
            customerId = "cust_100",
            customerName = "Juan Perez",
            saleId = "sale_200",
            amount = 75.50,
            paymentMethodName = "Transferencia",
            notes = "Pago parcial de factura #200",
            dateMillis = customDate,
        )

        assertEquals("pay_custom_1", payment.id)
        assertEquals("cust_100", payment.customerId)
        assertEquals("Juan Perez", payment.customerName)
        assertEquals("sale_200", payment.saleId)
        assertEquals(75.50, payment.amount, 0.001)
        assertEquals("Transferencia", payment.paymentMethodName)
        assertEquals("Pago parcial de factura #200", payment.notes)
        assertEquals(customDate, payment.dateMillis)
    }

    @Test
    fun customerPayment_defaultValues_areSetCorrectly() {
        val beforeCreation = System.currentTimeMillis()
        val payment = CustomerPayment(
            customerId = "cust_101",
            customerName = "Maria Rodriguez",
            amount = 50.0,
        )
        val afterCreation = System.currentTimeMillis()

        assertNotNull(payment.id)
        assertTrue(payment.id.isNotBlank())
        assertEquals("cust_101", payment.customerId)
        assertEquals("Maria Rodriguez", payment.customerName)
        assertNull(payment.saleId)
        assertEquals(50.0, payment.amount, 0.001)
        assertEquals("Efectivo", payment.paymentMethodName)
        assertNull(payment.notes)
        assertTrue((payment.dateMillis in beforeCreation..afterCreation))
    }

    @Test
    fun customerPayment_copyAndEquality() {
        val original = CustomerPayment(
            id = "pay_1",
            customerId = "cust_102",
            customerName = "Carlos Gomez",
            amount = 100.0
        )

        val updatedAmount = original.copy(amount = 120.0)
        assertEquals("pay_1", updatedAmount.id)
        assertEquals(120.0, updatedAmount.amount, 0.001)

        val duplicate = CustomerPayment(
            id = "pay_1",
            customerId = "cust_102",
            customerName = "Carlos Gomez",
            saleId = original.saleId,
            amount = 100.0,
            paymentMethodName = original.paymentMethodName,
            notes = original.notes,
            dateMillis = original.dateMillis
        )
        assertEquals(original, duplicate)
        assertEquals(original.hashCode(), duplicate.hashCode())
    }

    // ------------------------------------------------------------------
    // 2. Credit Sale Creation on Sale
    // ------------------------------------------------------------------

    @Test
    fun sale_defaultCreditValues_areSetCorrectly() {
        val creditSale = Sale(
            customerId = "cust_200",
            customerName = "Ana Martinez",
            paymentMethodId = "pm_credit",
            paymentMethodName = "Crédito",
            items = listOf(SaleItem("p1", "Producto A", 2, 50.0, 100.0)),
            totalAmount = 100.0,
            isCredit = true
        )

        assertTrue(creditSale.isCredit)
        assertEquals(100.0, creditSale.remainingBalance, 0.001)
        assertEquals(0.0, creditSale.paidAmount, 0.001)
        assertEquals(CreditStatus.UNPAID, creditSale.creditStatus)
    }

    @Test
    fun addSale_creditSaleCreation_initializesCreditFieldsAndUpdatesCustomerDebt() {
        val customer = Customer(
            id = "cust_201",
            name = "Roberto Silva",
            documentType = DocumentType.DUI,
            documentNumber = "00000000-1",
            department = "San Salvador",
            municipality = "San Salvador Centro",
            district = "San Salvador"
        )
        repository.addCustomer(customer)

        val creditSale = Sale(
            id = "sale_credit_101",
            customerId = "cust_201",
            customerName = "Roberto Silva",
            paymentMethodId = "pm_credit",
            paymentMethodName = "Crédito",
            items = listOf(
                SaleItem("p1", "Item 1", 1, 120.0, 120.0),
                SaleItem("p2", "Item 2", 1, 80.0, 80.0)
            ),
            totalAmount = 200.0,
            isCredit = true
        )

        repository.addSale(creditSale)

        val savedSale = repository.sales.value.find { it.id == "sale_credit_101" }
        assertNotNull(savedSale)
        assertTrue(savedSale!!.isCredit)
        assertEquals(200.0, savedSale.remainingBalance, 0.001)
        assertEquals(0.0, savedSale.paidAmount, 0.001)
        assertEquals(CreditStatus.UNPAID, savedSale.creditStatus)

        val updatedCustomer = repository.customers.value.find { it.id == "cust_201" }
        assertNotNull(updatedCustomer)
        assertEquals(200.0, updatedCustomer!!.currentDebt, 0.001)
    }

    // ------------------------------------------------------------------
    // 3. AppRepository.addCustomerPayment
    // ------------------------------------------------------------------

    @Test
    fun addCustomerPayment_partialPayment_updatesPaidAmountRemainingBalanceAndSetsStatusPartiallyPaid() {
        val customer = Customer(
            id = "cust_300",
            name = "Elena Vasquez",
            documentType = DocumentType.DUI,
            documentNumber = "00000000-2",
            department = "San Salvador",
            municipality = "San Salvador Centro",
            district = "San Salvador"
        )
        repository.addCustomer(customer)

        val creditSale = Sale(
            id = "sale_credit_300",
            customerId = "cust_300",
            customerName = "Elena Vasquez",
            paymentMethodId = "pm_credit",
            paymentMethodName = "Crédito",
            items = listOf(SaleItem("p1", "Laptop", 1, 500.0, 500.0)),
            totalAmount = 500.0,
            isCredit = true
        )
        repository.addSale(creditSale)

        val partialPayment = CustomerPayment(
            id = "pay_partial_1",
            customerId = "cust_300",
            customerName = "Elena Vasquez",
            saleId = "sale_credit_300",
            amount = 200.0,
            paymentMethodName = "Efectivo",
            notes = "Primer abono"
        )

        repository.addCustomerPayment(partialPayment)

        val saleAfterPayment = repository.sales.value.find { it.id == "sale_credit_300" }
        assertNotNull(saleAfterPayment)
        assertEquals(200.0, saleAfterPayment!!.paidAmount, 0.001)
        assertEquals(300.0, saleAfterPayment.remainingBalance, 0.001)
        assertEquals(CreditStatus.PARTIALLY_PAID, saleAfterPayment.creditStatus)
    }

    @Test
    fun addCustomerPayment_fullPayment_setsRemainingBalanceToZeroAndStatusPaid() {
        val customer = Customer(
            id = "cust_301",
            name = "Fernando Hernandez",
            documentType = DocumentType.DUI,
            documentNumber = "00000000-3",
            department = "San Salvador",
            municipality = "San Salvador Centro",
            district = "San Salvador"
        )
        repository.addCustomer(customer)

        val creditSale = Sale(
            id = "sale_credit_301",
            customerId = "cust_301",
            customerName = "Fernando Hernandez",
            paymentMethodId = "pm_credit",
            paymentMethodName = "Crédito",
            items = listOf(SaleItem("p1", "Escritorio", 1, 150.0, 150.0)),
            totalAmount = 150.0,
            isCredit = true
        )
        repository.addSale(creditSale)

        val fullPayment = CustomerPayment(
            id = "pay_full_1",
            customerId = "cust_301",
            customerName = "Fernando Hernandez",
            saleId = "sale_credit_301",
            amount = 150.0,
            paymentMethodName = "Transferencia"
        )

        repository.addCustomerPayment(fullPayment)

        val saleAfterPayment = repository.sales.value.find { it.id == "sale_credit_301" }
        assertNotNull(saleAfterPayment)
        assertEquals(150.0, saleAfterPayment!!.paidAmount, 0.001)
        assertEquals(0.0, saleAfterPayment.remainingBalance, 0.001)
        assertEquals(CreditStatus.PAID, saleAfterPayment.creditStatus)
    }

    @Test
    fun addCustomerPayment_reducesCustomerCurrentDebt() {
        val customer = Customer(
            id = "cust_302",
            name = "Sofia Castro",
            documentType = DocumentType.DUI,
            documentNumber = "00000000-4",
            department = "San Salvador",
            municipality = "San Salvador Centro",
            district = "San Salvador"
        )
        repository.addCustomer(customer)

        val creditSale = Sale(
            id = "sale_credit_302",
            customerId = "cust_302",
            customerName = "Sofia Castro",
            paymentMethodId = "pm_credit",
            paymentMethodName = "Crédito",
            items = listOf(SaleItem("p1", "Silla Ergonómica", 1, 250.0, 250.0)),
            totalAmount = 250.0,
            isCredit = true
        )
        repository.addSale(creditSale)

        // Verify initial debt after sale
        val initialCustomer = repository.customers.value.find { it.id == "cust_302" }
        assertEquals(250.0, initialCustomer?.currentDebt ?: 0.0, 0.001)

        // First partial payment
        val payment1 = CustomerPayment(
            id = "pay_step_1",
            customerId = "cust_302",
            customerName = "Sofia Castro",
            saleId = "sale_credit_302",
            amount = 100.0
        )
        repository.addCustomerPayment(payment1)

        val customerAfterPayment1 = repository.customers.value.find { it.id == "cust_302" }
        assertEquals(150.0, customerAfterPayment1?.currentDebt ?: 0.0, 0.001)

        // Second payment finishing debt
        val payment2 = CustomerPayment(
            id = "pay_step_2",
            customerId = "cust_302",
            customerName = "Sofia Castro",
            saleId = "sale_credit_302",
            amount = 150.0
        )
        repository.addCustomerPayment(payment2)

        val customerAfterPayment2 = repository.customers.value.find { it.id == "cust_302" }
        assertEquals(0.0, customerAfterPayment2?.currentDebt ?: 0.0, 0.001)
    }

    @Test
    fun addCustomerPayment_withoutSaleId_appliesToPendingCreditSalesAndReducesDebt() {
        val customer = Customer(
            id = "cust_303",
            name = "Gabriel Morales",
            documentType = DocumentType.DUI,
            documentNumber = "00000000-5",
            department = "San Salvador",
            municipality = "San Salvador Centro",
            district = "San Salvador"
        )
        repository.addCustomer(customer)

        val sale1 = Sale(
            id = "sale_c_1",
            customerId = "cust_303",
            customerName = "Gabriel Morales",
            paymentMethodId = "pm_credit",
            paymentMethodName = "Crédito",
            items = listOf(SaleItem("p1", "Item 1", 1, 100.0, 100.0)),
            totalAmount = 100.0,
            isCredit = true,
            dateMillis = 1000L
        )
        val sale2 = Sale(
            id = "sale_c_2",
            customerId = "cust_303",
            customerName = "Gabriel Morales",
            paymentMethodId = "pm_credit",
            paymentMethodName = "Crédito",
            items = listOf(SaleItem("p2", "Item 2", 1, 200.0, 200.0)),
            totalAmount = 200.0,
            isCredit = true,
            dateMillis = 2000L
        )
        repository.addSale(sale1)
        repository.addSale(sale2)

        // Total initial debt = 300.0
        assertEquals(300.0, repository.customers.value.find { it.id == "cust_303" }?.currentDebt ?: 0.0, 0.001)

        // Payment of 150.0 without saleId -> pays sale1 completely (100.0) and sale2 partially (50.0)
        val generalPayment = CustomerPayment(
            id = "pay_gen_1",
            customerId = "cust_303",
            customerName = "Gabriel Morales",
            saleId = null,
            amount = 150.0
        )
        repository.addCustomerPayment(generalPayment)

        val updatedSale1 = repository.sales.value.find { it.id == "sale_c_1" }
        assertNotNull(updatedSale1)
        assertEquals(100.0, updatedSale1!!.paidAmount, 0.001)
        assertEquals(0.0, updatedSale1.remainingBalance, 0.001)
        assertEquals(CreditStatus.PAID, updatedSale1.creditStatus)

        val updatedSale2 = repository.sales.value.find { it.id == "sale_c_2" }
        assertNotNull(updatedSale2)
        assertEquals(50.0, updatedSale2!!.paidAmount, 0.001)
        assertEquals(150.0, updatedSale2.remainingBalance, 0.001)
        assertEquals(CreditStatus.PARTIALLY_PAID, updatedSale2.creditStatus)

        // Customer debt reduced to 150.0
        assertEquals(150.0, repository.customers.value.find { it.id == "cust_303" }?.currentDebt ?: 0.0, 0.001)
    }
}
