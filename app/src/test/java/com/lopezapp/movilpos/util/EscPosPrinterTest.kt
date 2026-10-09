package com.lopezapp.movilpos.util

import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.CashShift
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.CustomerPayment
import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.SaleItem
import com.lopezapp.movilpos.data.model.TicketConfig
import com.lopezapp.movilpos.data.model.TicketPaperSize
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EscPosPrinterTest {

    private val businessInfo = BusinessInfo(
        name = "Test Store",
        nit = "0614-123456-123-1",
        nrc = "123456-7",
        address = "San Salvador, El Salvador",
        phone = "2222-3333"
    )

    private val sale = Sale(
        id = "sale_12345678",
        customerId = "cust_1",
        customerName = "Juan Perez",
        invoiceType = InvoiceType.CONSUMIDOR_FINAL,
        paymentMethodId = "pm_1",
        paymentMethodName = "Efectivo",
        items = listOf(
            SaleItem("p1", "Producto 1", 2, 5.0, 10.0),
            SaleItem("p2", "Producto 2", 1, 15.0, 15.0)
        ),
        totalAmount = 25.0,
        cashReceived = 30.0,
        changeAmount = 5.0,
        cashierName = "Cajero 1"
    )

    private val payment = CustomerPayment(
        id = "pay_12345678",
        customerId = "cust_1",
        customerName = "Juan Perez",
        amount = 50.0,
        paymentMethodName = "Efectivo",
        notes = "Abono a deuda"
    )

    private val customer = Customer(
        id = "cust_1",
        name = "Juan Perez",
        documentNumber = "01234567-8",
        department = "San Salvador",
        municipality = "San Salvador",
        district = "San Salvador",
        currentDebt = 100.0
    )

    private val shift = CashShift(
        id = "shift_123",
        cashierId = "u_1",
        cashierName = "Cajero 1",
        initialFloat = 50.0,
        totalCashSales = 150.0,
        totalCardSales = 100.0,
        totalExpenses = 20.0,
        actualCashCounted = 180.0
    )

    @Test
    fun testSaleReceiptBytes_58mm() {
        val config = TicketConfig(paperSize = TicketPaperSize.SIZE_57MM)
        val bytes = EscPosPrinter.buildSaleReceiptBytes(sale, businessInfo, config)
        assertNotNull(bytes)
        assertTrue(bytes.isNotEmpty())
    }

    @Test
    fun testSaleReceiptBytes_80mm() {
        val config = TicketConfig(paperSize = TicketPaperSize.SIZE_80MM)
        val bytes = EscPosPrinter.buildSaleReceiptBytes(sale, businessInfo, config)
        assertNotNull(bytes)
        assertTrue(bytes.isNotEmpty())
    }

    @Test
    fun testPaymentReceiptBytes_58mm() {
        val config = TicketConfig(paperSize = TicketPaperSize.SIZE_57MM)
        val bytes = EscPosPrinter.buildPaymentReceiptBytes(payment, customer, businessInfo, config)
        assertNotNull(bytes)
        assertTrue(bytes.isNotEmpty())
    }

    @Test
    fun testPaymentReceiptBytes_80mm() {
        val config = TicketConfig(paperSize = TicketPaperSize.SIZE_80MM)
        val bytes = EscPosPrinter.buildPaymentReceiptBytes(payment, customer, businessInfo, config)
        assertNotNull(bytes)
        assertTrue(bytes.isNotEmpty())
    }

    @Test
    fun testShiftReportBytes_58mm() {
        val config = TicketConfig(paperSize = TicketPaperSize.SIZE_57MM)
        val bytes = EscPosPrinter.buildShiftReportBytes(shift, businessInfo, config)
        assertNotNull(bytes)
        assertTrue(bytes.isNotEmpty())
    }

    @Test
    fun testShiftReportBytes_80mm() {
        val config = TicketConfig(paperSize = TicketPaperSize.SIZE_80MM)
        val bytes = EscPosPrinter.buildShiftReportBytes(shift, businessInfo, config)
        assertNotNull(bytes)
        assertTrue(bytes.isNotEmpty())
    }
}
