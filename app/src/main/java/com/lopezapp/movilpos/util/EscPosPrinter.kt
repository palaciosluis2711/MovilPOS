package com.lopezapp.movilpos.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.CashShift
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.CustomerPayment
import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.ShiftStatus
import com.lopezapp.movilpos.data.model.TicketConfig
import com.lopezapp.movilpos.data.model.TicketPaperSize
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object EscPosPrinter {

    val INIT: ByteArray = byteArrayOf(0x1B, 0x40)
    val ALIGN_LEFT: ByteArray = byteArrayOf(0x1B, 0x61, 0x00)
    val ALIGN_CENTER: ByteArray = byteArrayOf(0x1B, 0x61, 0x01)
    val ALIGN_RIGHT: ByteArray = byteArrayOf(0x1B, 0x61, 0x02)
    val BOLD_ON: ByteArray = byteArrayOf(0x1B, 0x45, 0x01)
    val BOLD_OFF: ByteArray = byteArrayOf(0x1B, 0x45, 0x00)
    val TEXT_SIZE_NORMAL: ByteArray = byteArrayOf(0x1D, 0x21, 0x00)
    val TEXT_SIZE_DOUBLE: ByteArray = byteArrayOf(0x1D, 0x21, 0x11)
    val CUT_PAPER: ByteArray = byteArrayOf(0x1D, 0x56, 0x42, 0x00)
    val PAPER_CUT: ByteArray = CUT_PAPER
    val LINE_FEED: ByteArray = byteArrayOf(0x0A)

    fun getLineWidth(paperWidthMm: Int): Int = when (paperWidthMm) {
        58 -> 32
        80 -> 48
        else -> 48
    }

    private fun getLineWidth(paperSize: TicketPaperSize): Int = when (paperSize) {
        TicketPaperSize.SIZE_57MM -> 32
        TicketPaperSize.SIZE_80MM -> 48
    }

    fun formatCurrency(amount: Double): String {
        return com.lopezapp.movilpos.util.formatCurrency(amount)
    }

    fun wrapTableRow(left: String, right: String, paperWidthMm: Int): String {
        val width = getLineWidth(paperWidthMm)
        val maxLeftWidth = width - right.length - 1
        val trimmedLeft = if ((maxLeftWidth > 0) && (left.length > maxLeftWidth)) {
            left.take(maxLeftWidth)
        } else {
            left
        }
        val spaces = width - trimmedLeft.length - right.length
        return if (spaces > 0) {
            trimmedLeft + " ".repeat(spaces) + right
        } else {
            "$trimmedLeft $right"
        }
    }

    private fun formatTwoColumns(left: String, right: String, paperSize: TicketPaperSize): String {
        return wrapTableRow(left, right, when (paperSize) {
            TicketPaperSize.SIZE_57MM -> 58
            TicketPaperSize.SIZE_80MM -> 80
        })
    }

    private fun divider(paperSize: TicketPaperSize, char: Char = '-'): String {
        val width = getLineWidth(paperSize)
        return char.toString().repeat(width)
    }

    private fun divider(paperWidthMm: Int, char: Char = '-'): String {
        val width = getLineWidth(paperWidthMm)
        return char.toString().repeat(width)
    }

    fun buildSaleReceiptBytes(
        sale: Sale,
        businessInfo: BusinessInfo,
        ticketConfig: TicketConfig
    ): ByteArray {
        return formatSaleTicket(sale, businessInfo, ticketConfig)
    }

    fun buildPaymentReceiptBytes(
        payment: CustomerPayment,
        customer: Customer?,
        businessInfo: BusinessInfo,
        ticketConfig: TicketConfig
    ): ByteArray {
        return formatPaymentTicket(payment, customer, businessInfo, ticketConfig)
    }

    fun buildShiftReportBytes(
        shift: CashShift,
        businessInfo: BusinessInfo,
        ticketConfig: TicketConfig
    ): ByteArray {
        return formatShiftReportTicket(shift, businessInfo, ticketConfig)
    }

    fun formatTestTicket(paperSize: TicketPaperSize, businessInfo: BusinessInfo): ByteArray {
        val builder = TicketBuilder()
            .append(INIT)
            .append(ALIGN_CENTER)
            .append(BOLD_ON)
            .append(TEXT_SIZE_DOUBLE)

        val name = businessInfo.name.ifBlank { "MovilPOS" }
        builder.appendLine(name)

        builder.append(TEXT_SIZE_NORMAL)
            .append(BOLD_OFF)

        if (businessInfo.nit.isNotBlank()) builder.appendLine("NIT: ${businessInfo.nit}")
        if (businessInfo.nrc.isNotBlank()) builder.appendLine("NRC: ${businessInfo.nrc}")
        if (businessInfo.address.isNotBlank()) builder.appendLine(businessInfo.address)
        if (businessInfo.phone.isNotBlank()) builder.appendLine("Tel: ${businessInfo.phone}")

        builder.appendLine(divider(paperSize, '='))
            .append(BOLD_ON)
            .appendLine("TICKET DE PRUEBA")
            .append(BOLD_OFF)
            .appendLine(divider(paperSize))

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        builder.append(ALIGN_LEFT)
            .appendLine("Fecha: ${dateFormat.format(Date())}")
            .appendLine("Ancho: ${paperSize.label} (${getLineWidth(paperSize)} cols)")
            .appendLine("Estado: Conexión Exitosa")
            .appendLine(divider(paperSize))
            .append(ALIGN_CENTER)
            .appendLine("¡Impresora ESC/POS lista!")
            .appendLine("Gracias por utilizar MovilPOS")
            .appendLine(divider(paperSize, '='))
            .feedLines(3)
            .append(PAPER_CUT)

        return builder.toByteArray()
    }

    fun formatSaleTicket(
        sale: Sale,
        businessInfo: BusinessInfo,
        ticketConfig: TicketConfig,
    ): ByteArray {
        val paperSize = ticketConfig.paperSize
        val paperWidth = when (paperSize) {
            TicketPaperSize.SIZE_57MM -> 58
            TicketPaperSize.SIZE_80MM -> 80
        }
        val builder = TicketBuilder()
            .append(INIT)
            .append(ALIGN_CENTER)

        if (ticketConfig.showBusinessName && businessInfo.name.isNotBlank()) {
            builder.append(BOLD_ON)
                .append(TEXT_SIZE_DOUBLE)
                .appendLine(businessInfo.name)
                .append(TEXT_SIZE_NORMAL)
                .append(BOLD_OFF)
        }

        if (ticketConfig.showNit && businessInfo.nit.isNotBlank()) {
            builder.appendLine("NIT: ${businessInfo.nit}")
        }
        if (ticketConfig.showNrc && businessInfo.nrc.isNotBlank()) {
            builder.appendLine("NRC: ${businessInfo.nrc}")
        }
        if (ticketConfig.showAddress && businessInfo.address.isNotBlank()) {
            builder.appendLine(businessInfo.address)
        }
        if (ticketConfig.showPhone && businessInfo.phone.isNotBlank()) {
            builder.appendLine("Tel: ${businessInfo.phone}")
        }
        if (ticketConfig.showSocialMedia && businessInfo.socialMedia.isNotBlank()) {
            builder.appendLine("Redes: ${businessInfo.socialMedia}")
        }

        builder.appendLine(divider(paperSize, '='))

        val invoiceTypeLabel = when (sale.invoiceType) {
            InvoiceType.CONSUMIDOR_FINAL -> "FACTURA CONSUMIDOR FINAL"
            InvoiceType.CREDITO_FISCAL -> "COMPROBANTE CRÉDITO FISCAL"
            InvoiceType.TICKET -> "TICKET DE VENTA"
        }

        builder.append(BOLD_ON)
            .appendLine(invoiceTypeLabel)
            .append(BOLD_OFF)
            .appendLine(divider(paperSize))

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        builder.append(ALIGN_LEFT)
            .appendLine("N° Ticket: ${sale.id.take(8).uppercase()}")
            .appendLine("Fecha: ${dateFormat.format(Date(sale.dateMillis))}")
            .appendLine("Cliente: ${sale.customerName}")
            .appendLine("Método Pago: ${sale.paymentMethodName}")

        if (sale.cashierName.isNotBlank()) {
            builder.appendLine("Cajero: ${sale.cashierName}")
        }

        if (sale.isDteIssued) {
            builder.appendLine(divider(paperSize))
                .append(ALIGN_CENTER)
                .append(BOLD_ON)
                .appendLine("DOCUMENTO TRIBUTARIO ELECTRÓNICO")
                .append(BOLD_OFF)
                .append(ALIGN_LEFT)

            val dteTypeDesc = if ((sale.dteType == "03") || (sale.invoiceType == InvoiceType.CREDITO_FISCAL)) {
                "Crédito Fiscal (03)"
            } else {
                "Consumidor Final (01)"
            }
            builder.appendLine("Tipo DTE: $dteTypeDesc")

            sale.dteGenerationCode?.let {
                builder.appendLine("Cód. Gen: $it")
            }
            sale.dteReceptionSeal?.let {
                builder.appendLine("Sello: $it")
            }
            sale.dteControlNumber?.let {
                builder.appendLine("N° Control: $it")
            }
        }

        builder.appendLine(divider(paperSize))

        builder.appendLine("Cant x Producto")
        sale.items.forEach { item ->
            builder.appendLine("${item.quantity} x ${item.productName}")
            val subtotalFormatted = formatCurrency(item.subtotal)
            val priceDetail = "  @ ${formatCurrency(item.unitPrice)}"
            builder.appendLine(wrapTableRow(priceDetail, subtotalFormatted, paperWidth))
        }

        builder.appendLine(divider(paperSize))

        if (sale.invoiceType == InvoiceType.CREDITO_FISCAL) {
            val calculatedSubtotal = sale.totalAmount / 1.13
            val calculatedTax = sale.totalAmount - calculatedSubtotal
            builder.appendLine(wrapTableRow("Subtotal (sin IVA):", formatCurrency(calculatedSubtotal), paperWidth))
                .appendLine(wrapTableRow("IVA (13%):", formatCurrency(calculatedTax), paperWidth))
        }

        builder.append(BOLD_ON)
            .appendLine(wrapTableRow("TOTAL:", formatCurrency(sale.totalAmount), paperWidth))
            .append(BOLD_OFF)

        if ((sale.cashReceived > 0) || sale.paymentMethodName.contains("Efectivo", ignoreCase = true)) {
            builder.appendLine(wrapTableRow("Recibido:", formatCurrency(sale.cashReceived), paperWidth))
                .appendLine(wrapTableRow("Cambio:", formatCurrency(sale.changeAmount), paperWidth))
        }

        if (sale.isCredit) {
            builder.appendLine(wrapTableRow("Abonado:", formatCurrency(sale.paidAmount), paperWidth))
                .appendLine(wrapTableRow("Saldo Pendiente:", formatCurrency(sale.remainingBalance), paperWidth))
        }

        if (ticketConfig.footerMessage.isNotBlank()) {
            builder.appendLine(divider(paperSize))
                .append(ALIGN_CENTER)
                .appendLine(ticketConfig.footerMessage)
        }

        builder.feedLines(3)
            .append(PAPER_CUT)

        return builder.toByteArray()
    }

    fun formatPaymentTicket(
        payment: CustomerPayment,
        customer: Customer?,
        businessInfo: BusinessInfo,
        ticketConfig: TicketConfig,
    ): ByteArray {
        val paperSize = ticketConfig.paperSize
        val paperWidth = when (paperSize) {
            TicketPaperSize.SIZE_57MM -> 58
            TicketPaperSize.SIZE_80MM -> 80
        }
        val builder = TicketBuilder()
            .append(INIT)
            .append(ALIGN_CENTER)

        if (ticketConfig.showBusinessName && businessInfo.name.isNotBlank()) {
            builder.append(BOLD_ON)
                .append(TEXT_SIZE_DOUBLE)
                .appendLine(businessInfo.name)
                .append(TEXT_SIZE_NORMAL)
                .append(BOLD_OFF)
        }

        if (ticketConfig.showNit && businessInfo.nit.isNotBlank()) {
            builder.appendLine("NIT: ${businessInfo.nit}")
        }
        if (ticketConfig.showNrc && businessInfo.nrc.isNotBlank()) {
            builder.appendLine("NRC: ${businessInfo.nrc}")
        }
        if (ticketConfig.showAddress && businessInfo.address.isNotBlank()) {
            builder.appendLine(businessInfo.address)
        }
        if (ticketConfig.showPhone && businessInfo.phone.isNotBlank()) {
            builder.appendLine("Tel: ${businessInfo.phone}")
        }
        if (ticketConfig.showSocialMedia && businessInfo.socialMedia.isNotBlank()) {
            builder.appendLine("Redes: ${businessInfo.socialMedia}")
        }

        builder.appendLine(divider(paperSize, '='))
            .append(BOLD_ON)
            .appendLine("COMPROBANTE DE PAGO / ABONO")
            .append(BOLD_OFF)
            .appendLine(divider(paperSize))

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        builder.append(ALIGN_LEFT)
            .appendLine("N° Recibo: ${payment.id.take(8).uppercase()}")
            .appendLine("Fecha: ${dateFormat.format(Date(payment.dateMillis))}")
            .appendLine("Cliente: ${payment.customerName}")

        customer?.let {
            if (it.documentNumber.isNotBlank()) {
                builder.appendLine("${it.documentType.name}: ${it.documentNumber}")
            }
        }

        builder.appendLine("Método Pago: ${payment.paymentMethodName}")

        payment.saleId?.let {
            builder.appendLine("Venta Ref: ${it.take(8).uppercase()}")
        }

        builder.appendLine(divider(paperSize))
            .append(BOLD_ON)
            .appendLine(wrapTableRow("MONTO PAGADO:", formatCurrency(payment.amount), paperWidth))
            .append(BOLD_OFF)

        customer?.let {
            builder.appendLine(wrapTableRow("Saldo Actual:", formatCurrency(it.currentDebt), paperWidth))
        }

        if (!payment.notes.isNullOrBlank()) {
            builder.appendLine("Notas: ${payment.notes}")
        }

        if (ticketConfig.footerMessage.isNotBlank()) {
            builder.appendLine(divider(paperSize))
                .append(ALIGN_CENTER)
                .appendLine(ticketConfig.footerMessage)
        }

        builder.feedLines(3)
            .append(PAPER_CUT)

        return builder.toByteArray()
    }

    fun formatShiftReportTicket(
        shift: CashShift,
        businessInfo: BusinessInfo,
        ticketConfig: TicketConfig,
    ): ByteArray {
        val paperSize = ticketConfig.paperSize
        val paperWidth = when (paperSize) {
            TicketPaperSize.SIZE_57MM -> 58
            TicketPaperSize.SIZE_80MM -> 80
        }
        val builder = TicketBuilder()
            .append(INIT)
            .append(ALIGN_CENTER)

        if (ticketConfig.showBusinessName && businessInfo.name.isNotBlank()) {
            builder.append(BOLD_ON)
                .append(TEXT_SIZE_DOUBLE)
                .appendLine(businessInfo.name)
                .append(TEXT_SIZE_NORMAL)
                .append(BOLD_OFF)
        }

        if (ticketConfig.showNit && businessInfo.nit.isNotBlank()) {
            builder.appendLine("NIT: ${businessInfo.nit}")
        }
        if (ticketConfig.showNrc && businessInfo.nrc.isNotBlank()) {
            builder.appendLine("NRC: ${businessInfo.nrc}")
        }
        if (ticketConfig.showAddress && businessInfo.address.isNotBlank()) {
            builder.appendLine(businessInfo.address)
        }
        if (ticketConfig.showPhone && businessInfo.phone.isNotBlank()) {
            builder.appendLine("Tel: ${businessInfo.phone}")
        }
        if (ticketConfig.showSocialMedia && businessInfo.socialMedia.isNotBlank()) {
            builder.appendLine("Redes: ${businessInfo.socialMedia}")
        }

        builder.appendLine(divider(paperSize, '='))
            .append(BOLD_ON)
            .appendLine("REPORTE DE CORTE DE CAJA")
            .append(BOLD_OFF)
            .appendLine(divider(paperSize))

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        builder.append(ALIGN_LEFT)
            .appendLine("Cajero: ${shift.cashierName}")
            .appendLine("Estado: ${if (shift.status == ShiftStatus.OPEN) "ABIERTA" else "CERRADA"}")
            .appendLine("Apertura: ${dateFormat.format(Date(shift.openedAtMillis))}")

        shift.closedAtMillis?.let {
            builder.appendLine("Cierre: ${dateFormat.format(Date(it))}")
        }

        builder.appendLine(divider(paperSize))
            .appendLine(wrapTableRow("Fondo Inicial:", formatCurrency(shift.initialFloat), paperWidth))
            .appendLine(wrapTableRow("Ventas Efectivo:", formatCurrency(shift.totalCashSales), paperWidth))
            .appendLine(wrapTableRow("Ventas Tarjeta:", formatCurrency(shift.totalCardSales), paperWidth))
            .appendLine(wrapTableRow("Otras Ventas:", formatCurrency(shift.totalOtherSales), paperWidth))
            .appendLine(wrapTableRow("Gastos:", formatCurrency(shift.totalExpenses), paperWidth))
            .appendLine(divider(paperSize))
            .append(BOLD_ON)
            .appendLine(wrapTableRow("Efectivo Esperado:", formatCurrency(shift.expectedCash), paperWidth))
            .append(BOLD_OFF)

        shift.actualCashCounted?.let { actual ->
            builder.appendLine(wrapTableRow("Efectivo Contado:", formatCurrency(actual), paperWidth))
            val diff = shift.difference ?: 0.0
            val diffLabel = when {
                diff > 0 -> "Sobrante:"
                diff < 0 -> "Faltante:"
                else -> "Diferencia:"
            }
            builder.append(BOLD_ON)
                .appendLine(wrapTableRow(diffLabel, formatCurrency(diff), paperWidth))
                .append(BOLD_OFF)
        }

        if (ticketConfig.footerMessage.isNotBlank()) {
            builder.appendLine(divider(paperSize))
                .append(ALIGN_CENTER)
                .appendLine(ticketConfig.footerMessage)
        }

        builder.feedLines(3)
            .append(PAPER_CUT)

        return builder.toByteArray()
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    fun printBytes(context: Context?, macAddress: String, bytes: ByteArray): Result<Unit> {
        return printBytesViaBluetooth(macAddress, bytes)
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    fun printBytes(macAddress: String, bytes: ByteArray): Result<Unit> {
        return printBytesViaBluetooth(macAddress, bytes)
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    fun printBytesViaBluetooth(macAddress: String, bytes: ByteArray): Result<Unit> {
        return try {
            if (macAddress.isBlank()) {
                return Result.failure(IllegalArgumentException("La dirección MAC de la impresora no es válida."))
            }

            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                ?: return Result.failure(IllegalStateException("El dispositivo no soporta Bluetooth."))

            if (!bluetoothAdapter.isEnabled) {
                return Result.failure(IllegalStateException("El Bluetooth está desactivado. Por favor, actívelo."))
            }

            val device: BluetoothDevice = try {
                bluetoothAdapter.getRemoteDevice(macAddress)
            } catch (_: Exception) {
                return Result.failure(IllegalArgumentException("Dispositivo Bluetooth no encontrado con la MAC: $macAddress"))
            }

            try {
                if (bluetoothAdapter.isDiscovering) {
                    bluetoothAdapter.cancelDiscovery()
                }
            } catch (_: SecurityException) {
            }

            val sppUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(sppUuid)

            try {
                socket.connect()
                val outputStream = socket.outputStream
                outputStream.write(bytes)
                outputStream.flush()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Error al comunicarse con la impresora Bluetooth: ${e.localizedMessage}", e))
            } finally {
                try {
                    socket.close()
                } catch (_: Exception) {
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private class TicketBuilder {
        private val outputStream = ByteArrayOutputStream()
        private val latin1Charset: Charset = Charset.forName("ISO-8859-1")

        fun append(bytes: ByteArray): TicketBuilder {
            outputStream.write(bytes)
            return this
        }

        fun appendLine(text: String = ""): TicketBuilder {
            if (text.isNotEmpty()) {
                outputStream.write(text.toByteArray(latin1Charset))
            }
            outputStream.write(LINE_FEED)
            return this
        }

        fun feedLines(count: Int): TicketBuilder {
            repeat(count) {
                outputStream.write(LINE_FEED)
            }
            return this
        }

        fun toByteArray(): ByteArray = outputStream.toByteArray()
    }
}
