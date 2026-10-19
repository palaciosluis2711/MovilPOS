package com.lopezapp.movilpos.util

import com.lopezapp.movilpos.data.model.BluetoothPrinterConfig
import com.lopezapp.movilpos.data.model.Brand
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.Category
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.CustomerPayment
import com.lopezapp.movilpos.data.model.DocumentType
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import com.lopezapp.movilpos.data.model.Expense
import com.lopezapp.movilpos.data.model.InventoryOutflow
import com.lopezapp.movilpos.data.model.OutflowItem
import com.lopezapp.movilpos.data.model.OutflowReason
import com.lopezapp.movilpos.data.model.PaymentMethod
import com.lopezapp.movilpos.data.model.PriceRule
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.ProductReturn
import com.lopezapp.movilpos.data.model.Purchase
import com.lopezapp.movilpos.data.model.PurchaseItem
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.SaleItem
import com.lopezapp.movilpos.data.model.Supplier
import com.lopezapp.movilpos.data.model.Tax
import com.lopezapp.movilpos.data.model.TaxValueType
import com.lopezapp.movilpos.data.model.TicketConfig
import com.lopezapp.movilpos.data.model.UnitOfMeasure
import com.lopezapp.movilpos.data.model.User
import com.lopezapp.movilpos.data.repository.AppRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FirebaseSyncServiceTest {

    private lateinit var repository: AppRepository

    @Before
    fun setUp() {
        FirebaseSyncService.resetState()
        repository = AppRepository()
    }

    @Test
    fun testUploadLocalDataProducesCompletePayload() = runBlocking {
        val result = FirebaseSyncService.uploadLocalDataToCloud(repository)

        assertTrue(result.isSuccess)
        val payload = result.getOrNull()
        assertNotNull(payload)

        assertTrue(payload!!.containsKey("products"))
        assertTrue(payload.containsKey("categories"))
        assertTrue(payload.containsKey("brands"))
        assertTrue(payload.containsKey("unitsOfMeasure"))
        assertTrue(payload.containsKey("taxes"))
        assertTrue(payload.containsKey("suppliers"))
        assertTrue(payload.containsKey("customers"))
        assertTrue(payload.containsKey("sales"))
        assertTrue(payload.containsKey("purchases"))
        assertTrue(payload.containsKey("inventoryOutflows"))
        assertTrue(payload.containsKey("productReturns"))
        assertTrue(payload.containsKey("customerPayments"))
        assertTrue(payload.containsKey("expenses"))
        assertTrue(payload.containsKey("priceRules"))
        assertTrue(payload.containsKey("paymentMethods"))
        assertTrue(payload.containsKey("users"))
        assertTrue(payload.containsKey("businessInfo"))
        assertTrue(payload.containsKey("ticketConfig"))
        assertTrue(payload.containsKey("electronicBillingConfig"))
        assertTrue(payload.containsKey("bluetoothPrinterConfig"))

        assertEquals(SyncStatus.SYNCED, FirebaseSyncService.syncStatus.value)
        assertNotNull(FirebaseSyncService.lastSyncTimestampMillis.value)
        assertEquals(0, FirebaseSyncService.pendingItemsCount.value)
    }

    @Test
    fun testAutoSyncToggleUpdatesSyncStatus() {
        FirebaseSyncService.setAutoSyncEnabled(false)
        assertEquals(SyncStatus.OFFLINE_MODE, FirebaseSyncService.syncStatus.value)
        assertEquals(false, FirebaseSyncService.isAutoSyncEnabled.value)

        FirebaseSyncService.setAutoSyncEnabled(true)
        assertEquals(true, FirebaseSyncService.isAutoSyncEnabled.value)
    }

    @Test
    fun testSerializationOfIndividualModels() {
        val product = Product(
            id = "prod-1",
            name = "Café Molido",
            price = 4.50,
            stock = 25,
            category = "Bebidas"
        )
        val serializedProduct = FirebaseSyncService.serializeProduct(product)
        assertEquals("prod-1", serializedProduct["id"])
        assertEquals("Café Molido", serializedProduct["name"])
        assertEquals(4.50, serializedProduct["price"])
        assertEquals(25, serializedProduct["stock"])

        val category = Category(id = "cat-1", name = "Bebidas", description = "Bebidas calientes y frías")
        val serializedCategory = FirebaseSyncService.serializeCategory(category)
        assertEquals("cat-1", serializedCategory["id"])
        assertEquals("Bebidas", serializedCategory["name"])

        val brand = Brand(id = "brand-1", name = "Nescafe")
        val serializedBrand = FirebaseSyncService.serializeBrand(brand)
        assertEquals("brand-1", serializedBrand["id"])
        assertEquals("Nescafe", serializedBrand["name"])

        val unit = UnitOfMeasure(id = "unit-1", name = "Libra", abbreviation = "lb")
        val serializedUnit = FirebaseSyncService.serializeUnitOfMeasure(unit)
        assertEquals("unit-1", serializedUnit["id"])
        assertEquals("Libra", serializedUnit["name"])
        assertEquals("lb", serializedUnit["abbreviation"])

        val tax = Tax(id = "tax-1", name = "IVA", valueType = TaxValueType.PERCENTAGE, value = 13.0)
        val serializedTax = FirebaseSyncService.serializeTax(tax)
        assertEquals("tax-1", serializedTax["id"])
        assertEquals("IVA", serializedTax["name"])
        assertEquals("PERCENTAGE", serializedTax["valueType"])
        assertEquals(13.0, serializedTax["value"])

        val customer = Customer(
            id = "cust-1",
            name = "Maria Lopez",
            documentType = DocumentType.DUI,
            documentNumber = "01234567-8",
            department = "San Salvador",
            municipality = "San Salvador",
            district = "Centro"
        )
        val serializedCustomer = FirebaseSyncService.serializeCustomer(customer)
        assertEquals("cust-1", serializedCustomer["id"])
        assertEquals("Maria Lopez", serializedCustomer["name"])
        assertEquals("DUI", serializedCustomer["documentType"])

        val supplier = Supplier(
            id = "supp-1",
            name = "Distribuidora El Sol"
        )
        val serializedSupplier = FirebaseSyncService.serializeSupplier(supplier)
        assertEquals("supp-1", serializedSupplier["id"])
        assertEquals("Distribuidora El Sol", serializedSupplier["name"])

        val sale = Sale(
            id = "sale-1",
            customerId = "cust-1",
            customerName = "Maria Lopez",
            paymentMethodId = "pm-1",
            paymentMethodName = "Efectivo",
            items = listOf(SaleItem("prod-1", "Café Molido", 2, 4.50, 9.00)),
            totalAmount = 9.00
        )
        val serializedSale = FirebaseSyncService.serializeSale(sale)
        assertEquals("sale-1", serializedSale["id"])
        assertEquals(9.00, serializedSale["totalAmount"])

        val purchase = Purchase(
            id = "pur-1",
            supplierId = "supp-1",
            supplierName = "Distribuidora El Sol",
            items = listOf(PurchaseItem("prod-1", "Café Molido", 10, 3.00)),
            totalCost = 30.00
        )
        val serializedPurchase = FirebaseSyncService.serializePurchase(purchase)
        assertEquals("pur-1", serializedPurchase["id"])
        assertEquals(30.00, serializedPurchase["totalCost"])

        val outflow = InventoryOutflow(
            id = "out-1",
            reason = OutflowReason.CONSUMO_INTERNO,
            items = listOf(OutflowItem("prod-1", "Café Molido", 1, 3.00, 3.00)),
            totalCost = 3.00
        )
        val serializedOutflow = FirebaseSyncService.serializeInventoryOutflow(outflow)
        assertEquals("out-1", serializedOutflow["id"])
        assertEquals("CONSUMO_INTERNO", serializedOutflow["reason"])

        val productReturn = ProductReturn(id = "ret-1", originalSaleId = "sale-1", customerName = "Maria Lopez")
        val serializedReturn = FirebaseSyncService.serializeProductReturn(productReturn)
        assertEquals("ret-1", serializedReturn["id"])
        assertEquals("sale-1", serializedReturn["originalSaleId"])

        val customerPayment = CustomerPayment(id = "pay-1", customerId = "cust-1", customerName = "Maria Lopez", amount = 10.0)
        val serializedPayment = FirebaseSyncService.serializeCustomerPayment(customerPayment)
        assertEquals("pay-1", serializedPayment["id"])
        assertEquals(10.0, serializedPayment["amount"])

        val expense = Expense(id = "exp-1", category = "Servicios", description = "Luz", amount = 50.0)
        val serializedExpense = FirebaseSyncService.serializeExpense(expense)
        assertEquals("exp-1", serializedExpense["id"])
        assertEquals("Luz", serializedExpense["description"])

        val priceRule = PriceRule(id = "rule-1", name = "Mayoreo", baseVariable = com.lopezapp.movilpos.data.model.BaseVariable.PRICE, operator = com.lopezapp.movilpos.data.model.ArithmeticOperator.SUBTRACT, value = 1.0)
        val serializedRule = FirebaseSyncService.serializePriceRule(priceRule)
        assertEquals("rule-1", serializedRule["id"])
        assertEquals("Mayoreo", serializedRule["name"])

        val paymentMethod = PaymentMethod(id = "pm-1", name = "Efectivo")
        val serializedPaymentMethod = FirebaseSyncService.serializePaymentMethod(paymentMethod)
        assertEquals("pm-1", serializedPaymentMethod["id"])
        assertEquals("Efectivo", serializedPaymentMethod["name"])

        val user = User(id = "usr-1", name = "Admin", pin = "1234")
        val serializedUser = FirebaseSyncService.serializeUser(user)
        assertEquals("usr-1", serializedUser["id"])
        assertEquals("Admin", serializedUser["name"])

        val businessInfo = BusinessInfo(
            name = "Mi Tienda SV",
            nit = "0614-010190-101-0"
        )
        val serializedBusiness = FirebaseSyncService.serializeBusinessInfo(businessInfo)
        assertEquals("Mi Tienda SV", serializedBusiness["name"])
        assertEquals("0614-010190-101-0", serializedBusiness["nit"])

        val ticketConfig = TicketConfig(footerMessage = "Gracias")
        val serializedTicket = FirebaseSyncService.serializeTicketConfig(ticketConfig)
        assertEquals("Gracias", serializedTicket["footerMessage"])

        val electronicBillingConfig = ElectronicBillingConfig(nit = "0614-010190-101-0")
        val serializedEbilling = FirebaseSyncService.serializeElectronicBillingConfig(electronicBillingConfig)
        assertEquals("0614-010190-101-0", serializedEbilling["nit"])

        val bluetoothPrinterConfig = BluetoothPrinterConfig(macAddress = "00:11:22:33:44:55", deviceName = "POS-Printer", paperWidthMm = 80, autoPrintSales = true)
        val serializedPrinter = FirebaseSyncService.serializeBluetoothPrinterConfig(bluetoothPrinterConfig)
        assertEquals("00:11:22:33:44:55", serializedPrinter["macAddress"])
        assertEquals("POS-Printer", serializedPrinter["deviceName"])
        assertEquals(80, serializedPrinter["paperWidthMm"])
        assertEquals(true, serializedPrinter["autoPrintSales"])
    }

    @Test
    fun testPendingItemsCountCalculation() {
        FirebaseSyncService.resetState()
        val initialPending = FirebaseSyncService.getPendingItemsCount(repository)
        assertTrue(initialPending > 0)

        runBlocking {
            FirebaseSyncService.uploadLocalDataToCloud(repository)
        }
        assertEquals(0, FirebaseSyncService.getPendingItemsCount(repository))
    }
}
