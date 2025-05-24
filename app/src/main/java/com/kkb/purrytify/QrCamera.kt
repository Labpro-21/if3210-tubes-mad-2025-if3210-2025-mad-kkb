package com.kkb.purrytify

import android.Manifest
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.camera.core.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@Composable
fun ScanQrMenuButton(onScanResult: (String) -> Unit) {
    var showScanner by remember { mutableStateOf(false) }

    IconButton(onClick = { showScanner = true }) {
        Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan QR", tint = Color.Gray)
    }

    if (showScanner) {
        QrScannerScreen(
            onResult = { code ->
                showScanner = false
                onScanResult(code)
            },
            onCancel = { showScanner = false }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrScannerScreen(
    onResult: (String) -> Unit,
    onCancel: () -> Unit
) {
    BackHandler {
        onCancel()
    }
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(cameraPermissionState.status) {
        if (cameraPermissionState.status is PermissionStatus.Denied) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    when (cameraPermissionState.status) {
        is PermissionStatus.Granted -> {
            Box(Modifier.fillMaxSize()) {
                QrCameraPreview(
                    onQrCodeScanned = { code ->
                        // Validasi: Pastikan QR valid untuk aplikasi Purrytify
                        if (isValidPurrytifyQr(context, code)) {
                            onResult(code)
                        } else {
                            errorMessage = "QR tidak valid untuk aplikasi Purrytify."
                        }
                    }
                )
                if (errorMessage != null) {
                    AlertDialog(
                        onDismissRequest = { errorMessage = null; onCancel() },
                        title = { Text("Error") },
                        text = { Text(errorMessage!!) },
                        confirmButton = {
                            TextButton(onClick = { errorMessage = null; onCancel() }) {
                                Text("OK")
                            }
                        }
                    )
                }
                // Back/cancel button
                IconButton(
                    onClick = { onCancel() },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        }
        is PermissionStatus.Denied -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Izinkan akses kamera untuk scan QR.")
            }
        }
    }
}

@Composable
fun QrCameraPreview(
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    var processing by remember { mutableStateOf(false) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analyzer = ImageAnalysis.Builder().build().also { imageAnalysis ->
                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx), { imageProxy ->
                        if (!processing) {
                            processing = true
                            processImageProxy(imageProxy, onQrCodeScanned) { processing = false }
                        } else {
                            imageProxy.close()
                        }
                    })
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    ctx as androidx.lifecycle.LifecycleOwner,
                    cameraSelector,
                    preview,
                    analyzer
                )
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier
            .fillMaxSize()
    )
}

fun processImageProxy(
    imageProxy: ImageProxy,
    onQrCodeScanned: (String) -> Unit,
    onFinish: () -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE
                ).build()
        )
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val value = barcode.rawValue
                    if (value != null) {
                        onQrCodeScanned(value)
                        break // This works because it's not inside a lambda
                    }
                }
                onFinish()
            }
            .addOnFailureListener {
                onFinish()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
        onFinish()
    }
}

fun isValidPurrytifyQr(context: Context, code: String): Boolean {
    // Example: QR harus mengandung domain/link Purrytify, misal "https://purrytify.com/song/"
    val domain_custom = context.getString(R.string.deeplink_custom)
    return code.contains("$domain_custom://song/") // Ubah sesuai domain kalian
}

