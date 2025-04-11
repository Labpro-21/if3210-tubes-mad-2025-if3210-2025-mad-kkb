package com.kkb.purrytify

import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.LocalContext
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File import java.io.FileOutputStream

//import kotlin.coroutines.jvm.internal.CompletedContinuation.context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadSongBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSaveClick: (title: String, artist: String, filePath: String, coverPath: String) -> Unit,
) {
    val fieldBgColor = colorResource(id = R.color.text_field_background)
    val loginButtonColor = colorResource(id = R.color.spotify_green)
    val white = colorResource(id = R.color.purritify_white)
    val grey = colorResource(id = R.color.text_grey)

    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var coverPath by remember { mutableStateOf<String>("null") }


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Black,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Ensures scroll on small screens
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Upload Song", color = Color.White, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            BoxWithConstraints {
                val boxSize = (maxWidth - 100.dp) / 2
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    UploadPhotoBox(){
                        uri -> coverPath = uri.toString()
                    }
                    UploadFileBox { uri ->
                        fileUri = uri

                        uri?.let {
                            val retriever = MediaMetadataRetriever()
                            retriever.setDataSource(context, it)

                            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "Untitled"
                            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unnamed Artist"

                            // Retrieve embedded album art
                            val coverBytes = retriever.embeddedPicture
                            if (coverBytes != null) {
                                // Example: Convert to Bitmap if you want to show it
                                val bitmap = BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.size)

                                // If you're saving to file, do this:
                                val coverFile = File(context.cacheDir, "cover_${System.currentTimeMillis()}.jpg")
                                FileOutputStream(coverFile).use { out ->
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                                }

                                // Save path or uri if needed
                                coverPath = coverFile.absolutePath
                            }
                            retriever.release()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("Title") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = white,
                        unfocusedTextColor = white,
                        disabledTextColor = grey,
                        focusedContainerColor = fieldBgColor,
                        unfocusedContainerColor = fieldBgColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = grey,
                        unfocusedLabelColor = grey,
                        cursorColor = white
                    ),
                    modifier = Modifier.width(320.dp)
                )

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist") },
                    placeholder = { Text("Artist") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = white,
                        unfocusedTextColor = white,
                        disabledTextColor = grey,
                        focusedContainerColor = fieldBgColor,
                        unfocusedContainerColor = fieldBgColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = grey,
                        unfocusedLabelColor = grey,
                        cursorColor = white
                    ),
                    modifier = Modifier.width(320.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        onSaveClick(title, artist, fileUri.toString(), coverPath)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = loginButtonColor)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun UploadPhotoBox(
    selectedFileName: String? = null,
    onPhotoSelected: (Uri?) -> Unit = {}) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> onPhotoSelected(uri) }

    Box(
        modifier = Modifier
            .size(100.dp)
            .border(BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(8.dp))
            .clickable { launcher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        Text(selectedFileName ?: "Upload Photo", color = Color.White)
    }
}

@Composable
fun UploadFileBox(
    selectedFileName: String? = null,
    onFileSelected: (Uri?) -> Unit = {}) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> onFileSelected(uri) }

    Box(
        modifier = Modifier
            .size(100.dp)
            .border(BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(8.dp))
            .clickable { launcher.launch("audio/*") },
        contentAlignment = Alignment.Center
    ) {
        Text(selectedFileName ?: "Upload File", color = Color.White)
    }
}
