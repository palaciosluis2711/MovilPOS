package com.lopezapp.movilpos.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {

    /**
     * Copies a selected image URI into [context].filesDir/app_images/[categoryFolder]/img_xxx.jpg
     * and returns the internal file:// URI.
     */
    fun copyUriToInternalStorage(context: Context, sourceUri: Uri, categoryFolder: String): Uri? {
        return try {
            val targetDir = File(context.filesDir, "app_images/$categoryFolder")
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            // If sourceUri is already inside app_images, return as is
            if (sourceUri.scheme == "file") {
                val path = sourceUri.path ?: ""
                val existingFile = File(path)
                val baseAppImagesDir = File(context.filesDir, "app_images").canonicalPath
                if (existingFile.exists() && existingFile.canonicalPath.startsWith(baseAppImagesDir)) {
                    return sourceUri
                }
            }

            val fileName = "img_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg"
            val targetFile = File(targetDir, fileName)

            val inputStream = openInputStream(context, sourceUri) ?: return null
            inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            Uri.fromFile(targetFile)
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Resizes the bitmap from [uriString] to max [maxDimension] x [maxDimension] px,
     * compresses to 75% JPEG, and returns a Base64 data string.
     */
    fun uriToBase64(context: Context, uriString: String, maxDimension: Int = 400): String? {
        if (uriString.isBlank()) return null
        return try {
            val uri = Uri.parse(uriString)

            // Step 1: Decode bounds
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            openInputStream(context, uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return null

            val origW = options.outWidth
            val origH = options.outHeight
            if (origW <= 0 || origH <= 0) return null

            // Step 2: Calculate sample size
            var sampleSize = 1
            val maxOrig = maxOf(origW, origH)
            while (maxOrig / (sampleSize * 2) >= maxDimension) {
                sampleSize *= 2
            }

            // Step 3: Decode sampled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val bitmap = openInputStream(context, uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            // Step 4: Scale to exact max dimension keeping aspect ratio
            val w = bitmap.width
            val h = bitmap.height
            val max = maxOf(w, h)
            val scaledBitmap = if (max > maxDimension) {
                val scale = maxDimension.toFloat() / max
                val targetW = (w * scale).toInt().coerceAtLeast(1)
                val targetH = (h * scale).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            } else {
                bitmap
            }

            // Step 5: Compress to JPEG 75%
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)

            if (scaledBitmap != bitmap) {
                bitmap.recycle()
            }
            scaledBitmap.recycle()

            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Converts [base64Data] string back into a JPEG file inside [context].filesDir/app_images/
     * and returns the local file:// URI string.
     */
    fun base64ToInternalFileUri(context: Context, base64Data: String, fileName: String): String? {
        if (base64Data.isBlank()) return null
        return try {
            val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
            val targetDir = File(context.filesDir, "app_images")
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            val safeFileName = if (fileName.endsWith(".jpg", ignoreCase = true)) fileName else "$fileName.jpg"
            val targetFile = File(targetDir, safeFileName)
            targetFile.writeBytes(imageBytes)
            Uri.fromFile(targetFile).toString()
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    private fun openInputStream(context: Context, uri: Uri): InputStream? {
        return try {
            context.contentResolver.openInputStream(uri)
        } catch (e: Throwable) {
            try {
                if (uri.scheme == "file" || uri.scheme == null) {
                    val filePath = uri.path ?: uri.toString()
                    val file = File(filePath)
                    if (file.exists()) FileInputStream(file) else null
                } else null
            } catch (e2: Throwable) {
                null
            }
        }
    }

    /**
     * Returns a safe model object (`File` for file:// URIs or local paths, `Uri` for content:// or web URLs)
     * for Coil's AsyncImage to ensure local files load reliably across app restarts.
     */
    fun getModel(uriString: String?): Any? {
        if (uriString.isNullOrBlank()) return null
        return if (uriString.startsWith("file://")) {
            File(uriString.removePrefix("file://"))
        } else {
            try {
                Uri.parse(uriString)
            } catch (e: Throwable) {
                uriString
            }
        }
    }
}
