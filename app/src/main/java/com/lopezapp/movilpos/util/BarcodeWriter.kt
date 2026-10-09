package com.lopezapp.movilpos.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.oned.Code128Writer

object BarcodeWriter {
    fun generateBarcodeBitmap(contents: String, width: Int = 300, height: Int = 100): Bitmap? {
        if (contents.isBlank()) return null
        return try {
            val writer = Code128Writer()
            val bitMatrix: BitMatrix = writer.encode(contents, BarcodeFormat.CODE_128, width, height)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
