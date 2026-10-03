package com.lopezapp.movilpos.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.model.Purchase
import com.lopezapp.movilpos.data.model.Quotation
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.Supplier
import com.lopezapp.movilpos.data.model.Tax
import com.lopezapp.movilpos.data.model.TicketConfig
import com.lopezapp.movilpos.data.model.TicketPaperSize
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.calculatePriceWithoutTax
import com.lopezapp.movilpos.data.model.getFormattedTaxLabel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generatePurchasePdf(
        context: Context,
        purchase: Purchase,
        supplier: Supplier? = null,
    ): File {
        val pdfDocument = PdfDocument()

        // Standard Letter dimensions in points at 72 DPI: 612 x 792 points
        val pageWidth = 612
        val pageHeight = 792
        val margin = 40f

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        // Paint definitions
        val paintTitle = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(24, 43, 73)
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintHeader = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(30, 30, 30)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintText = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(50, 50, 50)
            textSize = 10f
        }

        val paintTableHeader = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintTableText = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 10f
        }

        val paintGrid = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val paintTableBg = Paint().apply {
            color = Color.rgb(235, 240, 245)
            style = Paint.Style.FILL
        }

        var yPos = 40f

        // Document Header
        canvas.drawText("MOVILPOS - REPORTE DE COMPRA", margin, yPos + 18f, paintTitle)
        yPos += 30f

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(purchase.dateMillis))

        canvas.drawText("ID de Compra: ${purchase.id}", margin, yPos + 10f, paintText)
        yPos += 16f
        canvas.drawText("Fecha de Compra: $formattedDate", margin, yPos + 10f, paintText)
        yPos += 22f

        // Separator line
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paintGrid)
        yPos += 16f

        // Supplier Info Section
        canvas.drawText("INFORMACIÓN DEL PROVEEDOR", margin, yPos + 12f, paintHeader)
        yPos += 20f

        val supplierName = supplier?.name ?: purchase.supplierName
        canvas.drawText("Nombre: $supplierName", margin, yPos + 10f, paintText)
        yPos += 16f

        supplier?.address?.takeIf { it.isNotBlank() }?.let {
            canvas.drawText("Dirección: $it", margin, yPos + 10f, paintText)
            yPos += 16f
        }

        supplier?.phone?.takeIf { it.isNotBlank() }?.let {
            canvas.drawText("Teléfono: $it", margin, yPos + 10f, paintText)
            yPos += 16f
        }

        supplier?.email?.takeIf { it.isNotBlank() }?.let {
            canvas.drawText("Email: $it", margin, yPos + 10f, paintText)
            yPos += 16f
        }

        yPos += 10f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paintGrid)
        yPos += 20f

        // Items Table Section
        canvas.drawText("DETALLE DE PRODUCTOS", margin, yPos + 12f, paintHeader)
        yPos += 20f

        // Table setup
        // Columns: Producto (242), Cantidad (70), Costo Unitario (110), Subtotal (110) -> Total width: 532
        val colLefts = floatArrayOf(margin, margin + 242f, margin + 312f, margin + 422f)
        val colWidths = floatArrayOf(242f, 70f, 110f, 110f)
        val headers = arrayOf("Producto", "Cantidad", "Costo Unitario", "Subtotal")
        val rowHeight = 24f

        fun drawTableHeader(c: Canvas, topY: Float) {
            c.drawRect(margin, topY, pageWidth - margin, topY + rowHeight, paintTableBg)
            for (i in headers.indices) {
                c.drawText(headers[i], colLefts[i] + 6f, topY + 16f, paintTableHeader)
            }
            c.drawRect(margin, topY, pageWidth - margin, topY + rowHeight, paintGrid)
            for (i in 1 until colLefts.size) {
                c.drawLine(colLefts[i], topY, colLefts[i], topY + rowHeight, paintGrid)
            }
        }

        fun drawFooter(c: Canvas) {
            paintText.textSize = 9f
            paintText.color = Color.GRAY
            val footerText = "Generado automáticamente por MovilPOS"
            val footerWidth = paintText.measureText(footerText)
            c.drawText(footerText, (pageWidth - footerWidth) / 2f, pageHeight - 30f, paintText)
            paintText.textSize = 10f
            paintText.color = Color.rgb(50, 50, 50)
        }

        drawTableHeader(canvas, yPos)
        yPos += rowHeight

        purchase.items.forEach { item ->
            if ((yPos + rowHeight) > (pageHeight - 100f)) {
                drawFooter(canvas)
                pdfDocument.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 40f

                drawTableHeader(canvas, yPos)
                yPos += rowHeight
            }

            val subtotal = item.quantity * item.unitCost

            var name = item.productName
            val maxNameWidth = colWidths[0] - 12f
            if (paintTableText.measureText(name) > maxNameWidth) {
                while (name.isNotEmpty() && (paintTableText.measureText("$name...") > maxNameWidth)) {
                    name = name.dropLast(1)
                }
                name = "$name..."
            }

            canvas.drawText(name, colLefts[0] + 6f, yPos + 16f, paintTableText)
            canvas.drawText(item.quantity.toString(), colLefts[1] + 6f, yPos + 16f, paintTableText)
            canvas.drawText(formatCurrency(item.unitCost), colLefts[2] + 6f, yPos + 16f, paintTableText)
            canvas.drawText(formatCurrency(subtotal), colLefts[3] + 6f, yPos + 16f, paintTableText)

            canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, paintGrid)
            for (i in 1 until colLefts.size) {
                canvas.drawLine(colLefts[i], yPos, colLefts[i], yPos + rowHeight, paintGrid)
            }

            yPos += rowHeight
        }

        yPos += 20f

        if ((yPos + 40f) > (pageHeight - 80f)) {
            drawFooter(canvas)
            pdfDocument.finishPage(page)

            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPos = 40f
        }

        // Totals Section
        val totalFormatted = formatCurrency(purchase.totalCost)
        val totalLabel = "TOTAL COMPRA: $totalFormatted"

        paintHeader.textSize = 14f
        val totalWidth = paintHeader.measureText(totalLabel)
        canvas.drawText(totalLabel, pageWidth - margin - totalWidth, yPos + 16f, paintHeader)

        // Footer
        drawFooter(canvas)

        pdfDocument.finishPage(page)

        val outputFile = File(context.cacheDir, "compra_${purchase.id}.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    fun generateSaleTicketPdf(
        context: Context,
        sale: Sale,
        businessInfo: BusinessInfo,
        ticketConfig: TicketConfig
    ): File {
        val pdfDocument = PdfDocument()

        val is57mm = ticketConfig.paperSize == TicketPaperSize.SIZE_57MM
        val pageWidth = if (is57mm) 162 else 227
        val margin = if (is57mm) 8f else 12f

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(sale.dateMillis))

        val paintTitleCenter = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = if (is57mm) 10f else 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val paintHeaderCenter = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = if (is57mm) 8.5f else 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val paintTextCenter = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = if (is57mm) 7.5f else 8.5f
            textAlign = Paint.Align.CENTER
        }

        val paintTextLeft = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = if (is57mm) 7.5f else 8.5f
            textAlign = Paint.Align.LEFT
        }

        val paintTextRight = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = if (is57mm) 7.5f else 8.5f
            textAlign = Paint.Align.RIGHT
        }

        val paintBoldLeft = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = if (is57mm) 8f else 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }

        val paintBoldRight = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = if (is57mm) 8f else 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val paintLine = Paint().apply {
            color = Color.DKGRAY
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
        }

        var logoBitmap: Bitmap? = null
        if (ticketConfig.showLogo && !businessInfo.logoUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(businessInfo.logoUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val decoded = BitmapFactory.decodeStream(stream)
                    if (decoded != null) {
                        val maxLogoDim = if (is57mm) 36 else 48
                        val scale = minOf(maxLogoDim.toFloat() / decoded.width, maxLogoDim.toFloat() / decoded.height)
                        val w = (decoded.width * scale).toInt().coerceAtLeast(1)
                        val h = (decoded.height * scale).toInt().coerceAtLeast(1)
                        logoBitmap = Bitmap.createScaledBitmap(decoded, w, h, true)
                    }
                }
            } catch (_: Exception) {
                logoBitmap = null
            }
        }

        // Measure page height
        var estimatedHeight = margin * 2 + 10f
        if (logoBitmap != null) estimatedHeight += logoBitmap.height + 6f
        if (ticketConfig.showBusinessName && businessInfo.name.isNotBlank()) estimatedHeight += 14f
        if (ticketConfig.showNit && businessInfo.nit.isNotBlank()) estimatedHeight += 12f
        if (ticketConfig.showNrc && businessInfo.nrc.isNotBlank()) estimatedHeight += 12f
        if (ticketConfig.showAddress && businessInfo.address.isNotBlank()) estimatedHeight += 12f
        if (ticketConfig.showPhone && businessInfo.phone.isNotBlank()) estimatedHeight += 12f
        if (ticketConfig.showSocialMedia && businessInfo.socialMedia.isNotBlank()) estimatedHeight += 12f

        // Separator + Metadata
        estimatedHeight += 6f + 10f + 14f + 12f + 12f + 12f + 12f
        if (sale.isDteIssued) {
            estimatedHeight += 6f + 10f + 14f + (4 * 12f)
        }
        // Separator + Table header + items
        estimatedHeight += 6f + 10f + 14f + (sale.items.size * 12f)
        // Separator + Totals
        estimatedHeight += 6f + 10f + 14f
        if (sale.invoiceType == InvoiceType.CREDITO_FISCAL) {
            estimatedHeight += 12f + 12f
        }
        if (sale.cashReceived > 0 || sale.paymentMethodName.contains("Efectivo", ignoreCase = true)) {
            estimatedHeight += 12f + 12f
        }
        // Footer
        if (ticketConfig.footerMessage.isNotBlank()) {
            estimatedHeight += 6f + 12f + 14f
        }
        estimatedHeight += 20f

        val pageHeight = estimatedHeight.toInt().coerceAtLeast(200)
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val centerX = pageWidth / 2f
        var yPos = margin + 10f

        logoBitmap?.let { bmp ->
            canvas.drawBitmap(bmp, centerX - (bmp.width / 2f), yPos, null)
            yPos += bmp.height + 6f
        }

        if (ticketConfig.showBusinessName && businessInfo.name.isNotBlank()) {
            canvas.drawText(businessInfo.name, centerX, yPos + 10f, paintTitleCenter)
            yPos += 14f
        }
        if (ticketConfig.showNit && businessInfo.nit.isNotBlank()) {
            canvas.drawText("NIT: ${businessInfo.nit}", centerX, yPos + 9f, paintTextCenter)
            yPos += 12f
        }
        if (ticketConfig.showNrc && businessInfo.nrc.isNotBlank()) {
            canvas.drawText("NRC: ${businessInfo.nrc}", centerX, yPos + 9f, paintTextCenter)
            yPos += 12f
        }
        if (ticketConfig.showAddress && businessInfo.address.isNotBlank()) {
            canvas.drawText(businessInfo.address, centerX, yPos + 9f, paintTextCenter)
            yPos += 12f
        }
        if (ticketConfig.showPhone && businessInfo.phone.isNotBlank()) {
            canvas.drawText("Tel: ${businessInfo.phone}", centerX, yPos + 9f, paintTextCenter)
            yPos += 12f
        }
        if (ticketConfig.showSocialMedia && businessInfo.socialMedia.isNotBlank()) {
            canvas.drawText("Redes: ${businessInfo.socialMedia}", centerX, yPos + 9f, paintTextCenter)
            yPos += 12f
        }

        yPos += 6f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paintLine)
        yPos += 10f

        val invoiceLabel = when (sale.invoiceType) {
            InvoiceType.CONSUMIDOR_FINAL -> "FACTURA CONSUMIDOR FINAL"
            InvoiceType.CREDITO_FISCAL -> "COMPROBANTE CRÉDITO FISCAL"
            InvoiceType.TICKET -> "TICKET DE VENTA"
        }
        canvas.drawText(invoiceLabel, centerX, yPos + 9f, paintHeaderCenter)
        yPos += 14f

        canvas.drawText("Ticket N°: ${sale.id}", margin, yPos + 9f, paintTextLeft)
        yPos += 12f
        canvas.drawText("Fecha: $formattedDate", margin, yPos + 9f, paintTextLeft)
        yPos += 12f
        canvas.drawText("Cliente: ${sale.customerName}", margin, yPos + 9f, paintTextLeft)
        yPos += 12f
        canvas.drawText("Pago: ${sale.paymentMethodName}", margin, yPos + 9f, paintTextLeft)
        yPos += 12f

        if (sale.isDteIssued) {
            yPos += 6f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paintLine)
            yPos += 10f

            canvas.drawText("DOCUMENTO TRIBUTARIO ELECTRÓNICO (DTE)", centerX, yPos + 9f, paintHeaderCenter)
            yPos += 14f

            val dteTypeDesc = if (sale.dteType == "03" || sale.invoiceType == InvoiceType.CREDITO_FISCAL) "Crédito Fiscal (03)" else "Consumidor Final (01)"
            canvas.drawText("Tipo: $dteTypeDesc", margin, yPos + 9f, paintTextLeft)
            yPos += 12f
            canvas.drawText("Código de Generación: ${sale.dteGenerationCode ?: ""}", margin, yPos + 9f, paintTextLeft)
            yPos += 12f
            canvas.drawText("Sello de Recepción: ${sale.dteReceptionSeal ?: ""}", margin, yPos + 9f, paintTextLeft)
            yPos += 12f
            canvas.drawText("Número de Control: ${sale.dteControlNumber ?: ""}", margin, yPos + 9f, paintTextLeft)
            yPos += 12f
        }

        yPos += 6f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paintLine)
        yPos += 10f

        val colQtyX = margin
        val colSubX = pageWidth - margin
        val colPriceX = colSubX - if (is57mm) 35f else 45f
        val colProdX = colQtyX + if (is57mm) 18f else 22f

        canvas.drawText("Cant", colQtyX, yPos + 9f, paintBoldLeft)
        canvas.drawText("Producto", colProdX, yPos + 9f, paintBoldLeft)
        canvas.drawText("P.U.", colPriceX, yPos + 9f, paintBoldRight)
        canvas.drawText("Subtotal", colSubX, yPos + 9f, paintBoldRight)
        yPos += 14f

        sale.items.forEach { item ->
            var prodName = item.productName
            val maxProdWidth = colPriceX - colProdX - 2f
            if (paintTextLeft.measureText(prodName) > maxProdWidth) {
                while (prodName.isNotEmpty() && paintTextLeft.measureText("$prodName..") > maxProdWidth) {
                    prodName = prodName.dropLast(1)
                }
                prodName = "$prodName.."
            }

            canvas.drawText(item.quantity.toString(), colQtyX, yPos + 9f, paintTextLeft)
            canvas.drawText(prodName, colProdX, yPos + 9f, paintTextLeft)
            canvas.drawText(formatCurrency(item.unitPrice), colPriceX, yPos + 9f, paintTextRight)
            canvas.drawText(formatCurrency(item.subtotal), colSubX, yPos + 9f, paintTextRight)
            yPos += 12f
        }

        yPos += 6f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paintLine)
        yPos += 10f

        if (sale.invoiceType == InvoiceType.CREDITO_FISCAL) {
            val calculatedSubtotal = sale.totalAmount / 1.13
            val calculatedTax = sale.totalAmount - calculatedSubtotal

            canvas.drawText("Subtotal (sin IVA):", margin, yPos + 9f, paintTextLeft)
            canvas.drawText(formatCurrency(calculatedSubtotal), pageWidth - margin, yPos + 9f, paintTextRight)
            yPos += 12f

            canvas.drawText("IVA (13%):", margin, yPos + 9f, paintTextLeft)
            canvas.drawText(formatCurrency(calculatedTax), pageWidth - margin, yPos + 9f, paintTextRight)
            yPos += 12f

            canvas.drawText("TOTAL FINAL:", margin, yPos + 10f, paintBoldLeft)
            canvas.drawText(formatCurrency(sale.totalAmount), pageWidth - margin, yPos + 10f, paintBoldRight)
            yPos += 14f
        } else {
            canvas.drawText("TOTAL:", margin, yPos + 10f, paintBoldLeft)
            canvas.drawText(formatCurrency(sale.totalAmount), pageWidth - margin, yPos + 10f, paintBoldRight)
            yPos += 14f
        }

        if (sale.cashReceived > 0 || sale.paymentMethodName.contains("Efectivo", ignoreCase = true)) {
            canvas.drawText("Recibido:", margin, yPos + 9f, paintTextLeft)
            canvas.drawText(formatCurrency(sale.cashReceived), pageWidth - margin, yPos + 9f, paintTextRight)
            yPos += 12f

            canvas.drawText("Cambio:", margin, yPos + 9f, paintTextLeft)
            canvas.drawText(formatCurrency(sale.changeAmount), pageWidth - margin, yPos + 9f, paintTextRight)
            yPos += 12f
        }

        if (ticketConfig.footerMessage.isNotBlank()) {
            yPos += 6f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paintLine)
            yPos += 12f
            canvas.drawText(ticketConfig.footerMessage, centerX, yPos + 9f, paintTextCenter)
        }

        pdfDocument.finishPage(page)

        val outputFile = File(context.cacheDir, "ticket_${sale.id}.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    fun generateSaleInvoicePdf(
        context: Context,
        sale: Sale,
        customer: Customer? = null,
        businessInfo: BusinessInfo
    ): File {
        val pdfDocument = PdfDocument()

        // Standard Letter dimensions in points at 72 DPI: 612 x 792 points
        val pageWidth = 612
        val pageHeight = 792
        val margin = 40f

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        // Paint definitions
        val paintMainTitle = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(24, 43, 73)
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintDocTitle = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val paintHeader = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(24, 43, 73)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintText = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(50, 50, 50)
            textSize = 10f
        }

        val paintTextBold = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(30, 30, 30)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintTableHeader = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintTableText = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 10f
        }

        val paintTableTextRight = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 10f
            textAlign = Paint.Align.RIGHT
        }

        val paintTableHeaderRight = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val paintGrid = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val paintTableBg = Paint().apply {
            color = Color.rgb(235, 240, 245)
            style = Paint.Style.FILL
        }

        val paintHeaderBoxBg = Paint().apply {
            color = Color.rgb(24, 43, 73)
            style = Paint.Style.FILL
        }

        val paintLightBoxBg = Paint().apply {
            color = Color.rgb(248, 249, 250)
            style = Paint.Style.FILL
        }

        var yPos = 40f

        // Try decoding logo if available
        var logoBitmap: Bitmap? = null
        if (!businessInfo.logoUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(businessInfo.logoUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val decoded = BitmapFactory.decodeStream(stream)
                    if (decoded != null) {
                        val maxW = 80f
                        val maxH = 60f
                        val scale = minOf(maxW / decoded.width, maxH / decoded.height)
                        val w = (decoded.width * scale).toInt().coerceAtLeast(1)
                        val h = (decoded.height * scale).toInt().coerceAtLeast(1)
                        logoBitmap = Bitmap.createScaledBitmap(decoded, w, h, true)
                    }
                }
            } catch (_: Exception) {
                logoBitmap = null
            }
        }

        // Header Section: Business Info on Left, Invoice Box on Right
        val leftMargin = margin
        val rightBoxLeft = 362f
        val rightBoxWidth = 210f
        val rightBoxRight = rightBoxLeft + rightBoxWidth // 572f = pageWidth - margin

        var leftY = yPos
        logoBitmap?.let { bmp ->
            canvas.drawBitmap(bmp, leftMargin, leftY, null)
            leftY += bmp.height + 8f
        }

        val businessName = businessInfo.name.ifBlank { "MI NEGOCIO" }
        canvas.drawText(businessName, leftMargin, leftY + 14f, paintMainTitle)
        leftY += 22f

        if (businessInfo.nit.isNotBlank()) {
            canvas.drawText("NIT: ${businessInfo.nit}", leftMargin, leftY + 10f, paintText)
            leftY += 14f
        }
        if (businessInfo.nrc.isNotBlank()) {
            canvas.drawText("NRC: ${businessInfo.nrc}", leftMargin, leftY + 10f, paintText)
            leftY += 14f
        }
        if (businessInfo.address.isNotBlank()) {
            var addr = businessInfo.address
            if (paintText.measureText("Dirección: $addr") > 290f) {
                while (addr.isNotEmpty() && paintText.measureText("Dirección: $addr..") > 290f) {
                    addr = addr.dropLast(1)
                }
                addr = "$addr.."
            }
            canvas.drawText("Dirección: $addr", leftMargin, leftY + 10f, paintText)
            leftY += 14f
        }
        if (businessInfo.phone.isNotBlank()) {
            canvas.drawText("Teléfono: ${businessInfo.phone}", leftMargin, leftY + 10f, paintText)
            leftY += 14f
        }
        if (businessInfo.socialMedia.isNotBlank()) {
            canvas.drawText("Redes: ${businessInfo.socialMedia}", leftMargin, leftY + 10f, paintText)
            leftY += 14f
        }
        if (businessInfo.email.isNotBlank()) {
            canvas.drawText("Correo: ${businessInfo.email}", leftMargin, leftY + 10f, paintText)
            leftY += 14f
        }

        // Invoice Metadata Box on Right
        var rightY = yPos
        val docTitle = when (sale.invoiceType) {
            InvoiceType.CONSUMIDOR_FINAL -> "FACTURA CONSUMIDOR FINAL"
            InvoiceType.CREDITO_FISCAL -> "COMPROBANTE CRÉDITO FISCAL"
            InvoiceType.TICKET -> "TICKET DE VENTA"
        }

        val boxHeaderHeight = 24f
        val boxBodyHeight = 64f
        val totalBoxHeight = boxHeaderHeight + boxBodyHeight

        // Draw header fill
        canvas.drawRect(rightBoxLeft, rightY, rightBoxRight, rightY + boxHeaderHeight, paintHeaderBoxBg)
        val titleCenterX = rightBoxLeft + (rightBoxWidth / 2f)
        canvas.drawText(docTitle, titleCenterX, rightY + 16f, paintDocTitle)

        // Draw body background and border
        canvas.drawRect(rightBoxLeft, rightY + boxHeaderHeight, rightBoxRight, rightY + totalBoxHeight, paintLightBoxBg)
        canvas.drawRect(rightBoxLeft, rightY, rightBoxRight, rightY + totalBoxHeight, paintGrid)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(sale.dateMillis))

        var boxTextY = rightY + boxHeaderHeight + 14f
        canvas.drawText("N° Comprobante: ${sale.id.take(12)}", rightBoxLeft + 8f, boxTextY, paintTextBold)
        boxTextY += 15f
        canvas.drawText("Fecha: $formattedDate", rightBoxLeft + 8f, boxTextY, paintText)
        boxTextY += 15f
        canvas.drawText("Pago: ${sale.paymentMethodName}", rightBoxLeft + 8f, boxTextY, paintText)

        rightY += totalBoxHeight

        yPos = maxOf(leftY, rightY) + 16f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paintGrid)
        yPos += 16f

        // Customer Info Section
        canvas.drawText("INFORMACIÓN DEL CLIENTE", margin, yPos + 12f, paintHeader)
        yPos += 20f

        val custBoxTop = yPos
        val custBoxHeight = 56f
        canvas.drawRect(margin, custBoxTop, pageWidth - margin, custBoxTop + custBoxHeight, paintLightBoxBg)
        canvas.drawRect(margin, custBoxTop, pageWidth - margin, custBoxTop + custBoxHeight, paintGrid)

        val custName = customer?.name ?: sale.customerName
        val custDoc = customer?.let { "${it.documentType}: ${it.documentNumber}" } ?: "-"
        val custNrc = customer?.nrc?.takeIf { it.isNotBlank() } ?: "-"
        val custPhone = customer?.phone?.takeIf { it.isNotBlank() } ?: "-"

        val custAddrParts = listOfNotNull(
            customer?.address?.takeIf { it.isNotBlank() },
            listOfNotNull(customer?.district, customer?.municipality, customer?.department)
                .filter { it.isNotBlank() }
                .joinToString(", ")
                .takeIf { it.isNotBlank() }
        )
        val custAddress = if (custAddrParts.isNotEmpty()) custAddrParts.joinToString(" - ") else "-"

        var custTextY = custBoxTop + 16f
        canvas.drawText("Cliente / Razón Social: $custName", margin + 10f, custTextY, paintTextBold)
        canvas.drawText("Teléfono: $custPhone", margin + 280f, custTextY, paintText)

        custTextY += 16f
        canvas.drawText("Doc. Identidad: $custDoc", margin + 10f, custTextY, paintText)
        canvas.drawText("NRC: $custNrc", margin + 180f, custTextY, paintText)

        var truncatedAddr = custAddress
        if (paintText.measureText("Dirección: $truncatedAddr") > 220f) {
            while (truncatedAddr.isNotEmpty() && paintText.measureText("Dirección: $truncatedAddr..") > 220f) {
                truncatedAddr = truncatedAddr.dropLast(1)
            }
            truncatedAddr = "$truncatedAddr.."
        }
        canvas.drawText("Dirección: $truncatedAddr", margin + 280f, custTextY, paintText)

        yPos += custBoxHeight + 20f

        // Items Table Section
        canvas.drawText("DETALLE DE PRODUCTOS", margin, yPos + 12f, paintHeader)
        yPos += 20f

        // Table setup
        // Columns: N° (30), Descripción (257), Cantidad (55), P. Unitario (95), Subtotal (95) -> Total width: 532
        val colLefts = floatArrayOf(margin, margin + 30f, margin + 287f, margin + 342f, margin + 437f)
        val colWidths = floatArrayOf(30f, 257f, 55f, 95f, 95f)
        val headers = arrayOf("N°", "Descripción del Producto", "Cant.", "P. Unitario", "Subtotal")
        val rowHeight = 22f

        fun drawTableHeader(c: Canvas, topY: Float) {
            c.drawRect(margin, topY, pageWidth - margin, topY + rowHeight, paintTableBg)

            c.drawText(headers[0], colLefts[0] + 6f, topY + 15f, paintTableHeader)
            c.drawText(headers[1], colLefts[1] + 6f, topY + 15f, paintTableHeader)
            c.drawText(headers[2], colLefts[2] + colWidths[2] - 6f, topY + 15f, paintTableHeaderRight)
            c.drawText(headers[3], colLefts[3] + colWidths[3] - 6f, topY + 15f, paintTableHeaderRight)
            c.drawText(headers[4], colLefts[4] + colWidths[4] - 6f, topY + 15f, paintTableHeaderRight)

            c.drawRect(margin, topY, pageWidth - margin, topY + rowHeight, paintGrid)
            for (i in 1 until colLefts.size) {
                c.drawLine(colLefts[i], topY, colLefts[i], topY + rowHeight, paintGrid)
            }
        }

        fun drawFooter(c: Canvas) {
            val footerPaint = Paint().apply {
                isAntiAlias = true
                color = Color.GRAY
                textSize = 9f
                textAlign = Paint.Align.CENTER
            }
            val footerText = "Documento generado por MovilPOS - Gracias por su preferencia"
            c.drawText(footerText, pageWidth / 2f, pageHeight - 30f, footerPaint)
        }

        drawTableHeader(canvas, yPos)
        yPos += rowHeight

        sale.items.forEachIndexed { index, item ->
            if ((yPos + rowHeight) > (pageHeight - 120f)) {
                drawFooter(canvas)
                pdfDocument.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 40f

                drawTableHeader(canvas, yPos)
                yPos += rowHeight
            }

            var prodName = item.productName
            val maxNameWidth = colWidths[1] - 12f
            if (paintTableText.measureText(prodName) > maxNameWidth) {
                while (prodName.isNotEmpty() && (paintTableText.measureText("$prodName..") > maxNameWidth)) {
                    prodName = prodName.dropLast(1)
                }
                prodName = "$prodName.."
            }

            canvas.drawText("${index + 1}", colLefts[0] + 6f, yPos + 15f, paintTableText)
            canvas.drawText(prodName, colLefts[1] + 6f, yPos + 15f, paintTableText)
            canvas.drawText(item.quantity.toString(), colLefts[2] + colWidths[2] - 6f, yPos + 15f, paintTableTextRight)
            canvas.drawText(formatCurrency(item.unitPrice), colLefts[3] + colWidths[3] - 6f, yPos + 15f, paintTableTextRight)
            canvas.drawText(formatCurrency(item.subtotal), colLefts[4] + colWidths[4] - 6f, yPos + 15f, paintTableTextRight)

            canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, paintGrid)
            for (i in 1 until colLefts.size) {
                canvas.drawLine(colLefts[i], yPos, colLefts[i], yPos + rowHeight, paintGrid)
            }

            yPos += rowHeight
        }

        yPos += 16f

        if ((yPos + 80f) > (pageHeight - 60f)) {
            drawFooter(canvas)
            pdfDocument.finishPage(page)

            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPos = 40f
        }

        // Totals Section
        val showTaxBreakdown = (sale.invoiceType == InvoiceType.CREDITO_FISCAL)
        val calculatedSubtotal = if (showTaxBreakdown) sale.totalAmount / 1.13 else 0.0
        val calculatedTax = if (showTaxBreakdown) sale.totalAmount - calculatedSubtotal else 0.0

        val totalsBoxLeft = 332f
        val totalsBoxWidth = 240f
        val totalsBoxRight = totalsBoxLeft + totalsBoxWidth

        val showCashDetails = sale.cashReceived > 0 || sale.paymentMethodName.contains("Efectivo", ignoreCase = true)
        val totalsRowHeight = 18f
        val numTotalsRows = if (showTaxBreakdown) {
            if (showCashDetails) 5 else 3
        } else {
            if (showCashDetails) 3 else 1
        }
        val totalsBoxHeight = (numTotalsRows * totalsRowHeight) + 10f

        canvas.drawRect(totalsBoxLeft, yPos, totalsBoxRight, yPos + totalsBoxHeight, paintLightBoxBg)
        canvas.drawRect(totalsBoxLeft, yPos, totalsBoxRight, yPos + totalsBoxHeight, paintGrid)

        var tY = yPos + 16f
        if (showTaxBreakdown) {
            canvas.drawText("Subtotal (sin IVA):", totalsBoxLeft + 10f, tY, paintText)
            canvas.drawText(formatCurrency(calculatedSubtotal), totalsBoxRight - 10f, tY, paintTableTextRight)
            tY += totalsRowHeight

            canvas.drawText("Impuestos (13% IVA):", totalsBoxLeft + 10f, tY, paintText)
            canvas.drawText(formatCurrency(calculatedTax), totalsBoxRight - 10f, tY, paintTableTextRight)
            tY += totalsRowHeight
        }

        val totalLabel = if (showTaxBreakdown) "TOTAL A PAGAR:" else "TOTAL:"
        canvas.drawText(totalLabel, totalsBoxLeft + 10f, tY, paintTextBold)
        canvas.drawText(formatCurrency(sale.totalAmount), totalsBoxRight - 10f, tY, paintTableHeaderRight)
        tY += totalsRowHeight

        if (showCashDetails) {
            canvas.drawText("Efectivo Recibido:", totalsBoxLeft + 10f, tY, paintText)
            canvas.drawText(formatCurrency(sale.cashReceived), totalsBoxRight - 10f, tY, paintTableTextRight)
            tY += totalsRowHeight

            canvas.drawText("Cambio Devuelto:", totalsBoxLeft + 10f, tY, paintText)
            canvas.drawText(formatCurrency(sale.changeAmount), totalsBoxRight - 10f, tY, paintTableTextRight)
        }

        // Footer
        drawFooter(canvas)

        pdfDocument.finishPage(page)

        val outputFile = File(context.cacheDir, "factura_${sale.id}.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    fun generateQuotationPdf(
        context: Context,
        quotation: Quotation,
        businessInfo: BusinessInfo,
        taxes: List<Tax> = emptyList(),
        taxLabel: String? = null,
        products: List<Product> = emptyList()
    ): File {
        val displayTaxLabel = when {
            !taxLabel.isNullOrBlank() -> if (taxLabel.endsWith(":")) taxLabel else "$taxLabel:"
            taxes.isNotEmpty() -> {
                val label = taxes.getFormattedTaxLabel()
                if (label.endsWith(":")) label else "$label:"
            }
            else -> "Impuestos (Desglose):"
        }

        val pdfDocument = PdfDocument()

        val pageWidth = 612
        val pageHeight = 792
        val margin = 40f

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        val paintMainTitle = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(24, 43, 73)
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintDocTitle = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val paintHeader = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(24, 43, 73)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintText = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(50, 50, 50)
            textSize = 10f
        }

        val paintTextBold = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(30, 30, 30)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintTableHeader = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintTableText = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 10f
        }

        val paintTableTextRight = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 10f
            textAlign = Paint.Align.RIGHT
        }

        val paintTableHeaderRight = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val paintGrid = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val paintTableBg = Paint().apply {
            color = Color.rgb(235, 240, 245)
            style = Paint.Style.FILL
        }

        val paintHeaderBoxBg = Paint().apply {
            color = Color.rgb(24, 43, 73)
            style = Paint.Style.FILL
        }

        val paintLightBoxBg = Paint().apply {
            color = Color.rgb(248, 249, 250)
            style = Paint.Style.FILL
        }

        var yPos = 40f

        var logoBitmap: Bitmap? = null
        if (!businessInfo.logoUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(businessInfo.logoUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val decoded = BitmapFactory.decodeStream(stream)
                    if (decoded != null) {
                        val maxW = 80f
                        val maxH = 60f
                        val scale = minOf(maxW / decoded.width, maxH / decoded.height)
                        val w = (decoded.width * scale).toInt().coerceAtLeast(1)
                        val h = (decoded.height * scale).toInt().coerceAtLeast(1)
                        logoBitmap = Bitmap.createScaledBitmap(decoded, w, h, true)
                    }
                }
            } catch (_: Exception) {
                logoBitmap = null
            }
        }

        val leftMargin = margin
        val rightBoxLeft = 340f
        val rightBoxWidth = 232f
        val rightBoxRight = rightBoxLeft + rightBoxWidth

        var leftY = yPos
        logoBitmap?.let { bmp ->
            canvas.drawBitmap(bmp, leftMargin, leftY, null)
            leftY += bmp.height + 8f
        }

        val businessName = businessInfo.name.ifBlank { "MI NEGOCIO" }
        canvas.drawText(businessName, leftMargin, leftY + 14f, paintMainTitle)
        leftY += 22f

        if (businessInfo.nit.isNotBlank()) {
            canvas.drawText("NIT: ${businessInfo.nit}", leftMargin, leftY + 10f, paintText)
            leftY += 14f
        }
        if (businessInfo.nrc.isNotBlank()) {
            canvas.drawText("NRC: ${businessInfo.nrc}", leftMargin, leftY + 10f, paintText)
            leftY += 14f
        }
        if (businessInfo.address.isNotBlank()) {
            var addr = businessInfo.address
            if (paintText.measureText("Dirección: $addr") > 270f) {
                while (addr.isNotEmpty() && paintText.measureText("Dirección: $addr..") > 270f) {
                    addr = addr.dropLast(1)
                }
                addr = "$addr.."
            }
            canvas.drawText("Dirección: $addr", leftMargin, leftY + 10f, paintText)
            leftY += 14f
        }
        if (businessInfo.phone.isNotBlank()) {
            canvas.drawText("Teléfono: ${businessInfo.phone}", leftMargin, leftY + 10f, paintText)
            leftY += 14f
        }
        if (businessInfo.socialMedia.isNotBlank()) {
            canvas.drawText("Redes: ${businessInfo.socialMedia}", leftMargin, leftY + 10f, paintText)
            leftY += 14f
        }
        if (businessInfo.email.isNotBlank()) {
            canvas.drawText("Correo: ${businessInfo.email}", leftMargin, leftY + 10f, paintText)
            leftY += 14f
        }

        var rightY = yPos
        val docTitle = "COTIZACIÓN DE OFERTA"

        val boxHeaderHeight = 24f
        val boxBodyHeight = 68f
        val totalBoxHeight = boxHeaderHeight + boxBodyHeight

        canvas.drawRect(rightBoxLeft, rightY, rightBoxRight, rightY + boxHeaderHeight, paintHeaderBoxBg)
        val titleCenterX = rightBoxLeft + (rightBoxWidth / 2f)
        canvas.drawText(docTitle, titleCenterX, rightY + 16f, paintDocTitle)

        canvas.drawRect(rightBoxLeft, rightY + boxHeaderHeight, rightBoxRight, rightY + totalBoxHeight, paintLightBoxBg)
        canvas.drawRect(rightBoxLeft, rightY, rightBoxRight, rightY + totalBoxHeight, paintGrid)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedIssueDate = dateFormat.format(Date(quotation.dateMillis))
        val formattedExpirationDate = dateFormat.format(Date(quotation.expirationDateMillis))

        var boxTextY = rightY + boxHeaderHeight + 14f
        canvas.drawText("N° Cotización: ${quotation.id.take(12)}", rightBoxLeft + 8f, boxTextY, paintTextBold)
        boxTextY += 15f
        canvas.drawText("Fecha de Emisión: $formattedIssueDate", rightBoxLeft + 8f, boxTextY, paintText)
        boxTextY += 15f
        canvas.drawText("Fecha de Vencimiento: $formattedExpirationDate", rightBoxLeft + 8f, boxTextY, paintText)

        rightY += totalBoxHeight

        yPos = maxOf(leftY, rightY) + 16f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paintGrid)
        yPos += 16f

        canvas.drawText("INFORMACIÓN DEL CLIENTE", margin, yPos + 12f, paintHeader)
        yPos += 20f

        val custBoxTop = yPos
        val custBoxHeight = 44f
        canvas.drawRect(margin, custBoxTop, pageWidth - margin, custBoxTop + custBoxHeight, paintLightBoxBg)
        canvas.drawRect(margin, custBoxTop, pageWidth - margin, custBoxTop + custBoxHeight, paintGrid)

        val custName = quotation.customerName
        val custEmail = quotation.customerEmail.takeIf { !it.isNullOrBlank() } ?: "-"

        var custTextY = custBoxTop + 16f
        canvas.drawText("Cliente / Razón Social: $custName", margin + 10f, custTextY, paintTextBold)
        custTextY += 16f
        canvas.drawText("Correo Electrónico: $custEmail", margin + 10f, custTextY, paintText)

        yPos += custBoxHeight + 20f

        canvas.drawText("DETALLE DE COTIZACIÓN", margin, yPos + 12f, paintHeader)
        yPos += 20f

        val colLefts = floatArrayOf(margin, margin + 30f, margin + 242f, margin + 292f, margin + 372f, margin + 452f)
        val colWidths = floatArrayOf(30f, 212f, 50f, 80f, 80f, 80f)
        val headerPUnit = "P. Unitario"
        val headers = arrayOf("N°", "Producto", "Cant.", headerPUnit, "Regla/Desc.", "Subtotal")
        val rowHeight = 22f

        fun drawTableHeader(c: Canvas, topY: Float) {
            c.drawRect(margin, topY, pageWidth - margin, topY + rowHeight, paintTableBg)

            c.drawText(headers[0], colLefts[0] + 6f, topY + 15f, paintTableHeader)
            c.drawText(headers[1], colLefts[1] + 6f, topY + 15f, paintTableHeader)
            c.drawText(headers[2], colLefts[2] + colWidths[2] - 6f, topY + 15f, paintTableHeaderRight)
            c.drawText(headers[3], colLefts[3] + colWidths[3] - 6f, topY + 15f, paintTableHeaderRight)
            c.drawText(headers[4], colLefts[4] + 6f, topY + 15f, paintTableHeader)
            c.drawText(headers[5], colLefts[5] + colWidths[5] - 6f, topY + 15f, paintTableHeaderRight)

            c.drawRect(margin, topY, pageWidth - margin, topY + rowHeight, paintGrid)
            for (i in 1 until colLefts.size) {
                c.drawLine(colLefts[i], topY, colLefts[i], topY + rowHeight, paintGrid)
            }
        }

        fun drawFooter(c: Canvas) {
            val paintDisclaimer = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(100, 100, 100)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            }

            val disclaimer1 = "• Los precios y la disponibilidad de los productos expresados en esta cotización están sujetos a cambios sin previo aviso."
            val disclaimer2 = "• Este documento no representa una factura legal ni comprobante fiscal, es únicamente de carácter informativo."

            c.drawText(disclaimer1, margin, pageHeight - 62f, paintDisclaimer)
            c.drawText(disclaimer2, margin, pageHeight - 48f, paintDisclaimer)

            val footerPaint = Paint().apply {
                isAntiAlias = true
                color = Color.GRAY
                textSize = 9f
                textAlign = Paint.Align.CENTER
            }
            val footerText = "Oferta válida hasta $formattedExpirationDate. Documento generado por MovilPOS"
            c.drawText(footerText, pageWidth / 2f, pageHeight - 30f, footerPaint)
        }

        drawTableHeader(canvas, yPos)
        yPos += rowHeight

        quotation.items.forEachIndexed { index, item ->
            if ((yPos + rowHeight) > (pageHeight - 120f)) {
                drawFooter(canvas)
                pdfDocument.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 40f

                drawTableHeader(canvas, yPos)
                yPos += rowHeight
            }

            var prodName = item.productName
            val maxNameWidth = colWidths[1] - 12f
            if (paintTableText.measureText(prodName) > maxNameWidth) {
                while (prodName.isNotEmpty() && (paintTableText.measureText("$prodName..") > maxNameWidth)) {
                    prodName = prodName.dropLast(1)
                }
                prodName = "$prodName.."
            }

            var ruleName = if (item.isDiscounted) (item.appliedRuleName ?: "Descuento") else "-"
            val maxRuleWidth = colWidths[4] - 12f
            if (paintTableText.measureText(ruleName) > maxRuleWidth) {
                while (ruleName.isNotEmpty() && (paintTableText.measureText("$ruleName..") > maxRuleWidth)) {
                    ruleName = ruleName.dropLast(1)
                }
                ruleName = "$ruleName.."
            }

            var itemUnitPrice = item.unitPrice
            var itemSubtotal = item.quantity * item.unitPrice
            
            if (quotation.showTaxBreakdown) {
                val product = products.find { it.id == item.productId }
                val applicableTaxes = if (product != null && product.appliedTaxIds.isNotEmpty()) {
                    taxes.filter { tax -> product.appliedTaxIds.contains(tax.id) }
                } else {
                    taxes
                }
                itemUnitPrice = calculatePriceWithoutTax(item.unitPrice, applicableTaxes)
                itemSubtotal = calculatePriceWithoutTax(itemSubtotal, applicableTaxes)
            }
            
            canvas.drawText("${index + 1}", colLefts[0] + 6f, yPos + 15f, paintTableText)
            canvas.drawText(prodName, colLefts[1] + 6f, yPos + 15f, paintTableText)
            canvas.drawText(item.quantity.toString(), colLefts[2] + colWidths[2] - 6f, yPos + 15f, paintTableTextRight)
            canvas.drawText(formatCurrency(itemUnitPrice), colLefts[3] + colWidths[3] - 6f, yPos + 15f, paintTableTextRight)
            canvas.drawText(ruleName, colLefts[4] + 6f, yPos + 15f, paintTableText)
            canvas.drawText(formatCurrency(itemSubtotal), colLefts[5] + colWidths[5] - 6f, yPos + 15f, paintTableTextRight)

            canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, paintGrid)
            for (i in 1 until colLefts.size) {
                canvas.drawLine(colLefts[i], yPos, colLefts[i], yPos + rowHeight, paintGrid)
            }

            yPos += rowHeight
        }

        yPos += 16f

        if ((yPos + 80f) > (pageHeight - 75f)) {
            drawFooter(canvas)
            pdfDocument.finishPage(page)

            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPos = 40f
        }

        val totalsBoxLeft = 312f
        val totalsBoxWidth = 260f
        val totalsBoxRight = totalsBoxLeft + totalsBoxWidth

        if (quotation.showTaxBreakdown) {
            val subtotal = quotation.totalAmount - quotation.taxAmount
            val totalsBoxHeight = 65f

            if ((yPos + totalsBoxHeight) > (pageHeight - 75f)) {
                drawFooter(canvas)
                pdfDocument.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 40f
            }

            canvas.drawRect(totalsBoxLeft, yPos, totalsBoxRight, yPos + totalsBoxHeight, paintLightBoxBg)
            canvas.drawRect(totalsBoxLeft, yPos, totalsBoxRight, yPos + totalsBoxHeight, paintGrid)

            var tY = yPos + 18f
            canvas.drawText("Subtotal:", totalsBoxLeft + 10f, tY, paintText)
            canvas.drawText(formatCurrency(subtotal), totalsBoxRight - 10f, tY, paintTableTextRight)

            tY += 18f
            canvas.drawText(displayTaxLabel, totalsBoxLeft + 10f, tY, paintText)
            canvas.drawText(formatCurrency(quotation.taxAmount), totalsBoxRight - 10f, tY, paintTableTextRight)

            tY += 18f
            canvas.drawText("Total Final:", totalsBoxLeft + 10f, tY, paintTextBold)
            canvas.drawText(formatCurrency(quotation.totalAmount), totalsBoxRight - 10f, tY, paintTableHeaderRight)
            
            yPos += totalsBoxHeight + 30f
        } else {
            val totalsBoxHeight = 40f

            if ((yPos + totalsBoxHeight) > (pageHeight - 75f)) {
                drawFooter(canvas)
                pdfDocument.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 40f
            }

            canvas.drawRect(totalsBoxLeft, yPos, totalsBoxRight, yPos + totalsBoxHeight, paintLightBoxBg)
            canvas.drawRect(totalsBoxLeft, yPos, totalsBoxRight, yPos + totalsBoxHeight, paintGrid)

            val tY = yPos + 24f
            canvas.drawText("TOTAL COTIZADO:", totalsBoxLeft + 10f, tY, paintTextBold)
            canvas.drawText(formatCurrency(quotation.totalAmount), totalsBoxRight - 10f, tY, paintTableHeaderRight)
            
            yPos += totalsBoxHeight + 30f
        }

        if (quotation.showSignatureBlock || quotation.showStampBlock || quotation.showContactBlock) {
            if ((yPos + 20f) > (pageHeight - 170f)) {
                drawFooter(canvas)
                pdfDocument.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 40f
            }

            val actualYPos = maxOf(yPos + 20f, pageHeight - 170f)

            if (quotation.showSignatureBlock) {
                val sigX = margin
                val sigY = actualYPos + 50f
                canvas.drawLine(sigX, sigY, sigX + 140f, sigY, paintGrid)
                canvas.drawText("Firma del Ofertante", sigX + 25f, sigY + 14f, paintText)
            }

            if (quotation.showStampBlock) {
                val stampX = margin + 206f
                val stampY = actualYPos
                val dashPaint = Paint(paintGrid).apply {
                    pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
                }
                canvas.drawRect(stampX, stampY, stampX + 120f, stampY + 70f, dashPaint)
                canvas.drawText("Sello", stampX + 48f, stampY + 40f, paintText)
            }

            if (quotation.showContactBlock) {
                val contactX = pageWidth - margin - 150f
                var cY = actualYPos + 10f
                canvas.drawText("Información de Contacto:", contactX, cY, paintTextBold)
                cY += 14f
                val nameToPrint = businessInfo.name.ifBlank { "N/A" }
                var truncatedName = nameToPrint
                if (paintText.measureText(truncatedName) > 150f) {
                    while (truncatedName.isNotEmpty() && paintText.measureText("$truncatedName..") > 150f) {
                        truncatedName = truncatedName.dropLast(1)
                    }
                    truncatedName = "$truncatedName.."
                }
                canvas.drawText(truncatedName, contactX, cY, paintText)
                cY += 14f
                
                if (businessInfo.phone.isNotBlank()) {
                    canvas.drawText("Tel: ${businessInfo.phone}", contactX, cY, paintText)
                    cY += 14f
                }
                if (businessInfo.socialMedia.isNotBlank()) {
                    var truncatedSocial = businessInfo.socialMedia
                    if (paintText.measureText(truncatedSocial) > 150f) {
                        while (truncatedSocial.isNotEmpty() && paintText.measureText("$truncatedSocial..") > 150f) {
                            truncatedSocial = truncatedSocial.dropLast(1)
                        }
                        truncatedSocial = "$truncatedSocial.."
                    }
                    canvas.drawText(truncatedSocial, contactX, cY, paintText)
                    cY += 14f
                }
                if (businessInfo.email.isNotBlank()) {
                    var truncatedEmail = businessInfo.email
                    if (paintText.measureText(truncatedEmail) > 150f) {
                        while (truncatedEmail.isNotEmpty() && paintText.measureText("$truncatedEmail..") > 150f) {
                            truncatedEmail = truncatedEmail.dropLast(1)
                        }
                        truncatedEmail = "$truncatedEmail.."
                    }
                    canvas.drawText(truncatedEmail, contactX, cY, paintText)
                }
            }
        }

        drawFooter(canvas)

        pdfDocument.finishPage(page)

        val outputFile = File(context.cacheDir, "cotizacion_${quotation.id}.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    private fun convertNumberToSpanishWords(amount: Double): String {
        val integerPart = amount.toLong()
        val cents = Math.round((amount - integerPart) * 100).toInt()
        
        val units = arrayOf("", "UN", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE", "DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISEIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE", "VEINTE")
        val tens = arrayOf("", "", "VEINTI", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA")
        val hundreds = arrayOf("", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS", "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS")

        fun convertGroup(n: Long): String {
            if (n == 0L) return ""
            if (n == 100L) return "CIEN"
            var res = ""
            val h = (n / 100).toInt()
            val rest = (n % 100).toInt()
            if (h > 0) {
                res += hundreds[h] + " "
            }
            if (rest in 1..20) {
                res += units[rest] + " "
            } else if (rest > 20) {
                val t = rest / 10
                val u = rest % 10
                res += tens[t]
                if (u > 0) res += " Y " + units[u]
                res += " "
            }
            return res.trim()
        }

        val intStr = when {
            integerPart == 0L -> "CERO"
            integerPart == 1L -> "UN"
            integerPart < 1000L -> convertGroup(integerPart)
            else -> {
                val thousands = integerPart / 1000
                val remainder = integerPart % 1000
                val thStr = if (thousands == 1L) "MIL" else "${convertGroup(thousands)} MIL"
                if (remainder > 0L) "$thStr ${convertGroup(remainder)}" else thStr
            }
        }

        val centsStr = String.format(Locale.getDefault(), "%02d/100", cents)
        return "$intStr DÓLARES CON $centsStr".replace("UN DÓLARES", "UN DÓLAR")
    }

    fun generateDteInvoicePdf(
        context: Context,
        sale: Sale,
        businessInfo: BusinessInfo,
        customer: Customer? = null,
        electronicBillingConfig: ElectronicBillingConfig? = null
    ): File {
        val pdfDocument = PdfDocument()

        val pageWidth = 612
        val pageHeight = 792
        val margin = 40f

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        val paintTitle = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(24, 43, 73)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val paintSubtitle = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(60, 60, 60)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val paintHeader = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(24, 43, 73)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintText = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(50, 50, 50)
            textSize = 9f
        }

        val paintTextBold = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(30, 30, 30)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintTableHeader = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintTableText = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 9f
        }

        val paintTableTextRight = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 9f
            textAlign = Paint.Align.RIGHT
        }

        val paintTableHeaderRight = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val paintGrid = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val paintLightBoxBg = Paint().apply {
            color = Color.rgb(248, 249, 250)
            style = Paint.Style.FILL
        }

        val isCcf = (sale.dteType == "03" || sale.invoiceType == InvoiceType.CREDITO_FISCAL)

        fun drawWrappedText(
            c: Canvas,
            text: String,
            x: Float,
            y: Float,
            maxWidth: Float,
            paint: Paint,
            lineHeight: Float = 11f
        ): Float {
            val words = text.split(" ")
            var currentLine = ""
            var currentY = y
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) > maxWidth) {
                    if (currentLine.isNotEmpty()) {
                        c.drawText(currentLine, x, currentY, paint)
                        currentY += lineHeight
                        currentLine = word
                    } else {
                        c.drawText(word, x, currentY, paint)
                        currentY += lineHeight
                        currentLine = ""
                    }
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) {
                c.drawText(currentLine, x, currentY, paint)
                currentY += lineHeight
            }
            return currentY - y
        }

        var yPos = 72f

        // Emisor & Receptor Info Boxes
        val halfWidth = 260f
        val contentWidth = halfWidth - 16f

        val emisorNombre = businessInfo.name.takeIf { it.isNotBlank() } ?: "-"
        val emisorNit = businessInfo.nit.takeIf { it.isNotBlank() } ?: "-"
        val emisorNrc = businessInfo.nrc.takeIf { it.isNotBlank() } ?: "-"
        val emisorAct = electronicBillingConfig?.economicActivity?.takeIf { it.isNotBlank() } ?: "-"
        val emisorDir = businessInfo.address.takeIf { it.isNotBlank() } ?: "-"
        val emisorTel = businessInfo.phone.takeIf { it.isNotBlank() } ?: "-"
        val emisorCorreo = businessInfo.email.takeIf { it.isNotBlank() } ?: "-"
        val emisorComercial = businessInfo.commercialName.takeIf { it.isNotBlank() } ?: "-"

        val custName = customer?.name?.takeIf { it.isNotBlank() } ?: sale.customerName.takeIf { it.isNotBlank() } ?: "-"
        val custNit = customer?.documentNumber?.takeIf { it.isNotBlank() } ?: "-"
        val custNrc = customer?.nrc?.takeIf { it.isNotBlank() } ?: "-"
        val custAct = customer?.commercialActivity?.takeIf { it.isNotBlank() } ?: "-"
        val custDir = customer?.address?.takeIf { it.isNotBlank() } ?: "-"
        val custTel = customer?.phone?.takeIf { it.isNotBlank() } ?: "-"
        val custCorreo = customer?.email?.takeIf { it.isNotBlank() } ?: "-"
        val custComercial = customer?.commercialName?.takeIf { it.isNotBlank() } ?: "-"

        val emisorLines = listOf(
            "Nombre: $emisorNombre",
            "NIT: $emisorNit",
            "NRC: $emisorNrc",
            "Actividad: $emisorAct",
            "Dirección: $emisorDir",
            "Teléfono: $emisorTel",
            "Correo: $emisorCorreo",
            "Nombre Comercial: $emisorComercial"
        )
        val receptorLines = listOf(
            "Nombre: $custName",
            "NIT: $custNit",
            "NRC: $custNrc",
            "Actividad: $custAct",
            "Dirección: $custDir",
            "Teléfono: $custTel",
            "Correo: $custCorreo",
            "Nombre Comercial: $custComercial"
        )

        fun measureLinesHeight(lines: List<String>, maxWidth: Float, paint: Paint, lineHeight: Float = 10f): Float {
            var totalH = 0f
            for (line in lines) {
                val words = line.split(" ")
                var currentLine = ""
                var lineCount = 1
                for (word in words) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    if (paint.measureText(testLine) > maxWidth) {
                        lineCount++
                        currentLine = word
                    } else {
                        currentLine = testLine
                    }
                }
                totalH += lineCount * lineHeight
            }
            return totalH
        }

        val emisorContentH = measureLinesHeight(emisorLines, contentWidth, paintText, 10f)
        val receptorContentH = measureLinesHeight(receptorLines, contentWidth, paintText, 10f)
        val maxContentH = maxOf(emisorContentH, receptorContentH)
        val infoBoxHeight = 30f + maxContentH + 12f

        val rowHeight = 20f
        val numSummaryRows = if (isCcf) 3 else 1
        val summaryHeight = numSummaryRows * rowHeight
        val qrBlockHeight = 150f

        // Simulate pagination to calculate totalPages accurately
        var simY = 72f + 14f + 20f + 68f + 10f + infoBoxHeight + 14f + 36f
        var simulatedPages = 1
        sale.items.forEach { _ ->
            if ((simY + rowHeight + summaryHeight + qrBlockHeight) > (pageHeight - 40f)) {
                simulatedPages++
                simY = 40f + 20f
            }
            simY += rowHeight
        }
        if ((simY + summaryHeight + qrBlockHeight) > (pageHeight - 40f)) {
            simulatedPages++
        }
        val totalPages = simulatedPages

        // Official Header
        canvas.drawText("DOCUMENTO TRIBUTARIO ELECTRÓNICO (DTE)", pageWidth / 2f, yPos, paintSubtitle)
        yPos += 14f
        val docTypeTitle = if (isCcf) "COMPROBANTE DE CRÉDITO FISCAL" else "FACTURA"
        canvas.drawText(docTypeTitle, pageWidth / 2f, yPos, paintSubtitle)
        yPos += 20f

        // Metadata Box
        val metaBoxTop = yPos
        val metaBoxHeight = 68f
        canvas.drawRect(margin, metaBoxTop, pageWidth - margin, metaBoxTop + metaBoxHeight, paintLightBoxBg)
        canvas.drawRect(margin, metaBoxTop, pageWidth - margin, metaBoxTop + metaBoxHeight, paintGrid)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(sale.dateMillis))

        var mY = metaBoxTop + 13f
        val defaultGenCode = if (isCcf) "4757F91E-BA2C-4738-9B95-6BF0FCA24A62" else "01234567-89AB-CDEF-0123-456789ABCDEF"
        val defaultCtrlNum = if (isCcf) "DTE-03-M001P001-000000000000001" else "DTE-01-00000001-000000000000001"
        canvas.drawText("Código de Generación: ${sale.dteGenerationCode ?: defaultGenCode}", margin + 8f, mY, paintTextBold)
        canvas.drawText("Número de Control: ${sale.dteControlNumber ?: defaultCtrlNum}", margin + 300f, mY, paintText)
        mY += 14f
        canvas.drawText("Sello de Recepción: ${sale.dteReceptionSeal ?: "MH-DTE-2025-00000000000001"}", margin + 8f, mY, paintText)
        canvas.drawText("Tipo de Transmisión: Normal", margin + 300f, mY, paintText)
        mY += 14f
        canvas.drawText("Versión: 1", margin + 8f, mY, paintText)
        val typeFormatted = if (isCcf) "(03) Crédito Fiscal" else "(01) Consumidor Final"
        canvas.drawText("Tipo: $typeFormatted", margin + 300f, mY, paintText)
        mY += 14f
        canvas.drawText("Condición de Operación: Contado", margin + 8f, mY, paintText)
        canvas.drawText("Fecha y Hora de Emisión: $formattedDate", margin + 300f, mY, paintText)

        yPos += metaBoxHeight + 10f

        // Emisor Box (Left)
        canvas.drawRect(margin, yPos, margin + halfWidth, yPos + infoBoxHeight, paintLightBoxBg)
        canvas.drawRect(margin, yPos, margin + halfWidth, yPos + infoBoxHeight, paintGrid)
        var eY = yPos + 16f
        canvas.drawText("EMISOR:", margin + 8f, eY, paintHeader)
        eY += 14f

        for (line in emisorLines) {
            val h = drawWrappedText(canvas, line, margin + 8f, eY, contentWidth, paintText, 10f)
            eY += h
        }

        // Receptor Box (Right)
        val rightX = margin + halfWidth + 12f
        canvas.drawRect(rightX, yPos, pageWidth - margin, yPos + infoBoxHeight, paintLightBoxBg)
        canvas.drawRect(rightX, yPos, pageWidth - margin, yPos + infoBoxHeight, paintGrid)
        var rY = yPos + 16f
        canvas.drawText("RECEPTOR:", rightX + 8f, rY, paintHeader)
        rY += 14f

        for (line in receptorLines) {
            val h = drawWrappedText(canvas, line, rightX + 8f, rY, contentWidth, paintText, 10f)
            rY += h
        }

        yPos += infoBoxHeight + 14f

        // Items Table Section
        canvas.drawText("DETALLE DE ITEMS", margin, yPos + 10f, paintHeader)
        yPos += 16f

        val colLefts = floatArrayOf(margin, margin + 24f, margin + 272f, margin + 322f, margin + 422f)
        val colWidths = floatArrayOf(24f, 248f, 50f, 100f, 110f)
        val headers = arrayOf("N°", "Descripción", "Cant.", "Precio Unit.", if (isCcf) "Ventas Gravadas" else "Subtotal")

        fun drawTableHeader(c: Canvas, topY: Float) {
            val tableBg = Paint().apply {
                color = Color.rgb(235, 240, 245)
                style = Paint.Style.FILL
            }
            c.drawRect(margin, topY, pageWidth - margin, topY + rowHeight, tableBg)

            c.drawText(headers[0], colLefts[0] + 6f, topY + 14f, paintTableHeader)
            c.drawText(headers[1], colLefts[1] + 6f, topY + 14f, paintTableHeader)
            c.drawText(headers[2], colLefts[2] + colWidths[2] - 6f, topY + 14f, paintTableHeaderRight)
            c.drawText(headers[3], colLefts[3] + colWidths[3] - 6f, topY + 14f, paintTableHeaderRight)
            c.drawText(headers[4], colLefts[4] + colWidths[4] - 6f, topY + 14f, paintTableHeaderRight)

            c.drawRect(margin, topY, pageWidth - margin, topY + rowHeight, paintGrid)
            for (i in 1 until colLefts.size) {
                c.drawLine(colLefts[i], topY, colLefts[i], topY + rowHeight, paintGrid)
            }
        }

        fun drawFooter(c: Canvas, currentNum: Int, totalNum: Int) {
            val footerPaint = Paint().apply {
                isAntiAlias = true
                color = Color.GRAY
                textSize = 8f
            }
            c.drawText("Documento Tributario Electrónico - Generado por MovilPOS", margin, pageHeight - 20f, footerPaint)

            val pageNumPaint = Paint().apply {
                isAntiAlias = true
                color = Color.GRAY
                textSize = 8f
                textAlign = Paint.Align.RIGHT
            }
            c.drawText("Página $currentNum de $totalNum", pageWidth - margin, pageHeight - 20f, pageNumPaint)
        }

        drawTableHeader(canvas, yPos)
        yPos += rowHeight

        val calculatedSubtotal = if (isCcf) sale.totalAmount / 1.13 else sale.totalAmount
        val calculatedTax = if (isCcf) sale.totalAmount - calculatedSubtotal else 0.0

        fun drawSummaryRow(c: Canvas, topY: Float, label: String, value: String, isBold: Boolean) {
            val summaryBg = Paint().apply {
                color = if (isBold) Color.rgb(240, 244, 248) else Color.rgb(248, 249, 250)
                style = Paint.Style.FILL
            }
            c.drawRect(colLefts[2], topY, pageWidth - margin, topY + rowHeight, summaryBg)

            val labelPaint = Paint(if (isBold) paintTextBold else paintText).apply {
                textAlign = Paint.Align.RIGHT
            }
            val valuePaint = Paint(if (isBold) paintTableHeaderRight else paintTableTextRight).apply {
                textAlign = Paint.Align.RIGHT
            }

            c.drawText(label, 445f, topY + 14f, labelPaint)
            c.drawText(value, 565f, topY + 14f, valuePaint)

            c.drawRect(colLefts[2], topY, pageWidth - margin, topY + rowHeight, paintGrid)
            c.drawLine(colLefts[4], topY, colLefts[4], topY + rowHeight, paintGrid)
        }

        sale.items.forEachIndexed { index, item ->
            if ((yPos + rowHeight + summaryHeight + qrBlockHeight) > (pageHeight - 40f)) {
                drawFooter(canvas, pageNumber, totalPages)
                pdfDocument.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 40f

                drawTableHeader(canvas, yPos)
                yPos += rowHeight
            }

            var prodName = item.productName
            val maxNameWidth = colWidths[1] - 12f
            if (paintTableText.measureText(prodName) > maxNameWidth) {
                while (prodName.isNotEmpty() && (paintTableText.measureText("$prodName..") > maxNameWidth)) {
                    prodName = prodName.dropLast(1)
                }
                prodName = "$prodName.."
            }

            val unitPriceDisplay = if (isCcf) item.unitPrice / 1.13 else item.unitPrice
            val subtotalDisplay = if (isCcf) item.subtotal / 1.13 else item.subtotal

            canvas.drawText("${index + 1}", colLefts[0] + 6f, yPos + 14f, paintTableText)
            canvas.drawText(prodName, colLefts[1] + 6f, yPos + 14f, paintTableText)
            canvas.drawText(item.quantity.toString(), colLefts[2] + colWidths[2] - 6f, yPos + 14f, paintTableTextRight)
            canvas.drawText(formatCurrency(unitPriceDisplay), colLefts[3] + colWidths[3] - 6f, yPos + 14f, paintTableTextRight)
            canvas.drawText(formatCurrency(subtotalDisplay), colLefts[4] + colWidths[4] - 6f, yPos + 14f, paintTableTextRight)

            canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, paintGrid)
            for (i in 1 until colLefts.size) {
                canvas.drawLine(colLefts[i], yPos, colLefts[i], yPos + rowHeight, paintGrid)
            }

            yPos += rowHeight
        }

        if ((yPos + summaryHeight + qrBlockHeight) > (pageHeight - 40f)) {
            drawFooter(canvas, pageNumber, totalPages)
            pdfDocument.finishPage(page)

            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPos = 40f

            drawTableHeader(canvas, yPos)
            yPos += rowHeight
        }

        if (isCcf) {
            drawSummaryRow(canvas, yPos, "Suma de Ventas:", formatCurrency(calculatedSubtotal), false)
            yPos += rowHeight
            drawSummaryRow(canvas, yPos, "IVA (13%):", formatCurrency(calculatedTax), false)
            yPos += rowHeight
            drawSummaryRow(canvas, yPos, "Total a Pagar:", formatCurrency(sale.totalAmount), true)
            yPos += rowHeight
        } else {
            drawSummaryRow(canvas, yPos, "Total a Pagar:", formatCurrency(sale.totalAmount), true)
            yPos += rowHeight
        }

        yPos += 28f

        // QR Code & Footer Info Block (below table summary area)
        val qrSize = 90f
        val qrX = margin + 12f
        val qrY = yPos

        val dteUrl = "https://dte.mh.gob.sv/consultaDTE?cu=${sale.dteGenerationCode ?: defaultGenCode}"
        val qrBitmap = QrCodeGenerator.generateQrCode(dteUrl, qrSize.toInt(), qrSize.toInt())

        val qrBoxLeft = qrX - 6f
        val qrBoxTop = qrY - 4f
        val qrBoxWidth = qrSize + 12f
        val qrBoxHeight = qrSize + 22f
        canvas.drawRect(qrBoxLeft, qrBoxTop, qrBoxLeft + qrBoxWidth, qrBoxTop + qrBoxHeight, paintLightBoxBg)
        canvas.drawRect(qrBoxLeft, qrBoxTop, qrBoxLeft + qrBoxWidth, qrBoxTop + qrBoxHeight, paintGrid)

        if (qrBitmap != null) {
            canvas.drawBitmap(qrBitmap, qrX, qrY, null)
        }

        val qrTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.DKGRAY
            textSize = 8f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Sello", qrX + (qrSize / 2f), qrY + qrSize + 11f, qrTextPaint)

        val infoLeft = qrX + qrSize + 16f
        val infoRight = pageWidth - margin - 12f
        val infoWidth = infoRight - infoLeft

        val wordsInSpanish = convertNumberToSpanishWords(sale.totalAmount)
        val textLetras = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(50, 50, 50)
            textSize = 8.5f
        }

        var infoY = qrY + 4f
        val wordsH = drawWrappedText(canvas, "Valor en Letras: $wordsInSpanish", infoLeft, infoY, infoWidth, textLetras, 11f)
        infoY += wordsH + 4f

        canvas.drawText("Observaciones: -", infoLeft, infoY, textLetras)
        infoY += 12f

        if (!isCcf) {
            canvas.drawText("Valores expresados en Dólares de los Estados Unidos de América. IVA incluido.", infoLeft, infoY, textLetras)
            infoY += 12f
        }

        infoY += 8f
        val noticePaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(40, 40, 40)
            textSize = 8.5f
        }
        canvas.drawText("Sello de Recepción MH: ${sale.dteReceptionSeal ?: "MH-DTE-2025-00000000000001"}", infoLeft, infoY, noticePaint)
        infoY += 12f
        canvas.drawText("Este documento es una representación gráfica de un DTE (Documento Tributario Electrónico).", infoLeft, infoY, noticePaint)

        drawFooter(canvas, pageNumber, totalPages)

        pdfDocument.finishPage(page)

        val outputFile = File(context.cacheDir, "dte_invoice_${sale.id}.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }
}
