package com.lopezapp.movilpos.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.lopezapp.movilpos.data.model.Purchase
import com.lopezapp.movilpos.data.model.Supplier
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
}
