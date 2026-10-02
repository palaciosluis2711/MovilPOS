package com.lopezapp.movilpos.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.model.Purchase
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.Supplier
import com.lopezapp.movilpos.data.model.TicketConfig
import com.lopezapp.movilpos.data.model.TicketPaperSize
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

        // Standard A4 dimensions in points at 72 DPI: 595 x 842 points
        val pageWidth = 595
        val pageHeight = 842
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
        // Columns: Producto (225), Cantidad (70), Costo Unitario (110), Subtotal (110) -> Total width: 515
        val colLefts = floatArrayOf(margin, margin + 225f, margin + 295f, margin + 405f)
        val colWidths = floatArrayOf(225f, 70f, 110f, 110f)
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
        // Separator + Table header + items
        estimatedHeight += 6f + 10f + 14f + (sale.items.size * 12f)
        // Separator + Totals
        estimatedHeight += 6f + 10f + 14f
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

        canvas.drawText("TOTAL:", margin, yPos + 10f, paintBoldLeft)
        canvas.drawText(formatCurrency(sale.totalAmount), pageWidth - margin, yPos + 10f, paintBoldRight)
        yPos += 14f

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
}
