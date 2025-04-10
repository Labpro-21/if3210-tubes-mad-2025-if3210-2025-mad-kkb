//package com.kkb.purrytify
//import android.content.Context
//import android.content.Intent
//import android.net.Uri
//import android.provider.OpenableColumns
//import android.media.MediaMetadataRetriever
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.*
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.painter.Painter
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.kkb.purrytify.R
//import kotlinx.coroutines.launch
//
//@Composable
//fun UploadSongScreen(
//    viewModel: UploadSongViewModel = hiltViewModel(),
//    onSave: () -> Unit,
//    onCancel: () -> Unit
//) {
//    val context = LocalContext.current
//    val songState by viewModel.songState.collectAsState()
//
//    val launcherAudio = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.OpenDocument(),
//        onResult = { uri ->
//            uri?.let {
//                context.contentResolver.takePersistableUriPermission(
//                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
//                )
//                viewModel.setAudioUri(it, context)
//            }
//        }
//    )
//
//    val launcherImage = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent(),
//        onResult = { uri -> uri?.let { viewModel.setArtworkUri(it) } }
//    )
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Black)
//            .padding(24.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("Upload Song", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
//
//        Spacer(Modifier.height(16.dp))
//
//        // Upload Photo
//        UploadBox(
//            label = "Upload Photo",
//            onClick = { launcherImage.launch("image/*") },
//            icon = painterResource(id = R.drawable.ic_image)
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//        // Upload File
//        UploadBox(
//            label = "Upload File",
//            onClick = { launcherAudio.launch(arrayOf("audio/*")) },
//            icon = painterResource(id = R.drawable.ic_music_note)
//        )
//
//        Spacer(Modifier.height(24.dp))
//
//        OutlinedTextField(
//            value = songState.title,
//            onValueChange = viewModel::setTitle,
//            label = { Text("Title") },
//            modifier = Modifier.fillMaxWidth(),
//            colors = TextFieldDefaults.outlinedTextFieldColors(textColor = Color.White)
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//        OutlinedTextField(
//            value = songState.artist,
//            onValueChange = viewModel::setArtist,
//            label = { Text("Artist") },
//            modifier = Modifier.fillMaxWidth(),
//            colors = TextFieldDefaults.outlinedTextFieldColors(textColor = Color.White)
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//        Text(
//            text = "Duration: ${songState.duration}",
//            color = Color.White,
//            fontSize = 14.sp
//        )
//
//        Spacer(Modifier.height(32.dp))
//
//        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//            Button(
//                onClick = onCancel,
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
//                shape = RoundedCornerShape(50),
//                modifier = Modifier.weight(1f)
//            ) {
//                Text("Cancel")
//            }
//
//            Spacer(Modifier.width(16.dp))
//
//            Button(
//                onClick = {
//                    viewModel.saveSong()
//                    onSave()
//                },
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
//                shape = RoundedCornerShape(50),
//                modifier = Modifier.weight(1f)
//            ) {
//                Text("Save", color = Color.White)
//            }
//        }
//    }
//}
//
//@Composable
//fun UploadBox(label: String, onClick: () -> Unit, icon: Painter) {
//    Box(
//        modifier = Modifier
//            .size(100.dp)
//            .background(Color.DarkGray, RoundedCornerShape(12.dp))
//            .clickable { onClick() },
//        contentAlignment = Alignment.Center
//    ) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            Icon(painter = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
//            Spacer(Modifier.height(8.dp))
//            Text(label, color = Color.White)
//        }
//    }
//}
