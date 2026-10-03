@file:OptIn(
    ExperimentalPermissionsApi::class
)

package com.lopezapp.movilpos.ui.components

import android.Manifest
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Dialog composable that presents a live camera feed for scanning barcodes and QR codes.
 * Uses CameraX and ML Kit Barcode Scanning.
 */
@Composable
fun BarcodeScannerDialog(
    onDismissRequest: () -> Unit,
    onBarcodeScanned: (scannedCode: String) -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(580.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Escanear Código",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (cameraPermissionState.status.isGranted) {
                    CameraScanContent(
                        onBarcodeScanned = { code ->
                            onBarcodeScanned(code)
                            onDismissRequest()
                        },
                        onDismissRequest = onDismissRequest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clipToBounds()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        CameraPermissionRationale(
                            shouldShowRationale = cameraPermissionState.status.shouldShowRationale,
                            onRequestPermission = {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        )
                    }

                    // Dedicated bottom controls surface when permission is not granted yet
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Soporta Códigos y QR",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedButton(onClick = onDismissRequest) {
                                Text("Cancelar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraScanContent(
    onBarcodeScanned: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera by remember { mutableStateOf<Camera?>(null) }
    var isTorchEnabled by remember { mutableStateOf(false) }
    var hasFlashUnit by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    val hasScanned = remember { AtomicBoolean(false) }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner) {
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val selector = CameraSelector.DEFAULT_BACK_CAMERA

                val barcodeScannerOptions = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    .build()

                val barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions)

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    try {
                        @androidx.annotation.OptIn(ExperimentalGetImage::class)
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !hasScanned.get()) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            barcodeScanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    if (hasScanned.get()) return@addOnSuccessListener
                                    for (barcode in barcodes) {
                                        val codeValue = barcode.rawValue ?: barcode.displayValue ?: continue
                                        if (codeValue.isNotBlank()) {
                                            if (hasScanned.compareAndSet(false, true)) {
                                                mainExecutor.execute {
                                                    onBarcodeScanned(codeValue)
                                                }
                                                break
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("BarcodeScanner", "Barcode scanning error", e)
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    } catch (e: Exception) {
                        Log.e("BarcodeScanner", "Error analyzing frame", e)
                        imageProxy.close()
                    }
                }

                cameraProvider.unbindAll()

                val boundCamera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    imageAnalysis
                )

                camera = boundCamera
                hasFlashUnit = boundCamera.cameraInfo.hasFlashUnit()
            } catch (e: Exception) {
                Log.e("BarcodeScanner", "Failed to initialize camera", e)
                cameraError = "Error al iniciar la cámara: ${e.localizedMessage}"
            }
        }, mainExecutor)

        onDispose {
            cameraExecutor.shutdown()
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                Log.e("BarcodeScanner", "Error cleaning up camera resources", e)
            }
        }
    }

    Column(modifier = modifier) {
        // Camera View & Scan Overlay Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (cameraError != null) {
                Text(
                    text = cameraError ?: "",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )

                // Scanning Box Overlay with Corner Brackets & Animation
                ScanOverlay(modifier = Modifier.fillMaxSize())
            }
        }

        // Dedicated, clean, top-z-index Surface for bottom controls
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasFlashUnit) {
                    OutlinedButton(
                        onClick = {
                            val newTorchState = !isTorchEnabled
                            camera?.cameraControl?.enableTorch(newTorchState)
                            isTorchEnabled = newTorchState
                        }
                    ) {
                        Icon(
                            imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = if (isTorchEnabled) "Apagar Linterna" else "Encender Linterna",
                            tint = if (isTorchEnabled) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTorchEnabled) "Linterna ON" else "Linterna",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    Text(
                        text = "Soporta Códigos y QR",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = onDismissRequest
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
private fun ScanOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "ScanLineTransition")
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScanLineProgress"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val boxWidth = size.width * 0.72f
            val boxHeight = boxWidth * 0.72f
            val left = (size.width - boxWidth) / 2f
            val top = (size.height - boxHeight) / 2f

            val darkOverlayColor = Color.Black.copy(alpha = 0.6f)

            // 1. Top dark panel
            drawRect(
                color = darkOverlayColor,
                topLeft = Offset(0f, 0f),
                size = Size(size.width, top)
            )

            // 2. Bottom dark panel
            drawRect(
                color = darkOverlayColor,
                topLeft = Offset(0f, top + boxHeight),
                size = Size(size.width, (size.height - (top + boxHeight)).coerceAtLeast(0f))
            )

            // 3. Left dark panel
            drawRect(
                color = darkOverlayColor,
                topLeft = Offset(0f, top),
                size = Size(left, boxHeight)
            )

            // 4. Right dark panel
            drawRect(
                color = darkOverlayColor,
                topLeft = Offset(left + boxWidth, top),
                size = Size((size.width - (left + boxWidth)).coerceAtLeast(0f), boxHeight)
            )

            // Frame border around scan box target
            drawRoundRect(
                color = primaryColor.copy(alpha = 0.8f),
                topLeft = Offset(left, top),
                size = Size(boxWidth, boxHeight),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // Corner L-brackets
            val cornerLength = 24.dp.toPx()
            val strokeWidth = 4.dp.toPx()

            // Top-Left
            drawLine(primaryColor, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
            drawLine(primaryColor, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)

            // Top-Right
            drawLine(primaryColor, Offset(left + boxWidth, top), Offset(left + boxWidth - cornerLength, top), strokeWidth)
            drawLine(primaryColor, Offset(left + boxWidth, top), Offset(left + boxWidth, top + cornerLength), strokeWidth)

            // Bottom-Left
            drawLine(primaryColor, Offset(left, top + boxHeight), Offset(left + cornerLength, top + boxHeight), strokeWidth)
            drawLine(primaryColor, Offset(left, top + boxHeight), Offset(left, top + boxHeight - cornerLength), strokeWidth)

            // Bottom-Right
            drawLine(primaryColor, Offset(left + boxWidth, top + boxHeight), Offset(left + boxWidth - cornerLength, top + boxHeight), strokeWidth)
            drawLine(primaryColor, Offset(left + boxWidth, top + boxHeight), Offset(left + boxWidth, top + boxHeight - cornerLength), strokeWidth)

            // Animated scanning line
            val currentLineY = top + (boxHeight * scanLineProgress)
            drawLine(
                color = primaryColor,
                start = Offset(left + 12.dp.toPx(), currentLineY),
                end = Offset(left + boxWidth - 12.dp.toPx(), currentLineY),
                strokeWidth = 3.dp.toPx()
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Alinea el código dentro del marco",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun CameraPermissionRationale(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Permiso de Cámara Requerido",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (shouldShowRationale) {
                "Se requiere el permiso de la cámara para escanear códigos de barras y QR de tus productos."
            } else {
                "Concede acceso a la cámara para utilizar el escáner de códigos de barras."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRequestPermission
        ) {
            Text("Conceder Permiso")
        }
    }
}
