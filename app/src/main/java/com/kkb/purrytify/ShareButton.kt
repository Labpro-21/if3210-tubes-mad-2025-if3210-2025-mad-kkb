package com.kkb.purrytify

// Compose icons and color
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Compose image
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer

// Android intent and context
import android.content.Context
import android.content.Intent

// ZXing QR code
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

// Android bitmap and file
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream
import android.graphics.Color as AndroidColor

// FileProvider for sharing files
import androidx.core.content.FileProvider

// For Icons.Default.Share, etc.
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share

// If you use ExpandMore or other icons, import as needed:
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
fun ShareSongButton(currentSong: UserSong) {
    var showDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val domain_url = context.getString(R.string.deeplink_domain)
    val domain_custom = context.getString(R.string.deeplink_custom)
    val songUrl = "https://$domain_url/song/${currentSong.songId}"
    val songUrlCustom = "$domain_custom://song/${currentSong.songId}"

    // Create and remember QR bitmap OUTSIDE the dialog composable scope
    val qrBitmap = remember(songUrlCustom) { generateQrBitmap(songUrlCustom) }

    IconButton(onClick = { showDialog = true }) {
        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White)
    }

    // Popup: Choose Share Method
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Bagikan Lagu") },
            text = {
                Column {
                    Button(onClick = {
                        showDialog = false
                        // Share URL
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Check out this song!")
                            putExtra(Intent.EXTRA_TEXT, songUrl)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share song via"))
                    }) {
                        Text("Bagikan URL")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        showDialog = false
                        showQrDialog = true
                    }) {
                        Text("Bagikan QR Code")
                    }
                }
            },
            confirmButton = {}
        )
    }

    // QR Preview & Share Dialog
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("QR Lagu") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    qrBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(200.dp)
                        )
                    }
                    Text(currentSong.title, style = MaterialTheme.typography.bodySmall)
                    Text(currentSong.artist, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {

                // Share QR as image
                qrBitmap?.let { bitmap ->
                    //this is the error
                    shareQrBitmap(bitmap)
                }
                TextButton(onClick = {
                    showQrDialog = false
                }) {
                    Text("Share QR via Apps")
                }

            }
        )
    }
}

// Helper: Generate QR Bitmap
fun generateQrBitmap(data: String): Bitmap? {
    val size = 512
    return try {
        val bits = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size)
        Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).apply {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    setPixel(x, y, if (bits[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
                }
            }
        }
    } catch (e: Exception) {
        null
    }
}

// Helper: Share QR Bitmap (using FileProvider)
@Composable
fun shareQrBitmap(qrBitmap: Bitmap, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Prepare launcher for the share intent
    val shareQrLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* no-op for share */ }

    Button(
        onClick = {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "qr.png")
            FileOutputStream(file).use { out -> qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            shareQrLauncher.launch(Intent.createChooser(shareIntent, "Share QR via"))
        },
        modifier = modifier
    ) {
        Text("Share QR via Apps")
    }
}