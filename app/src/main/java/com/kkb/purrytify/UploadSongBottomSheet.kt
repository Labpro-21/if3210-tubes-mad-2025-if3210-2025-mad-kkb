package com.kkb.purrytify

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.*
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadSongBottomSheet(
    sheetState: SheetState,
    coroutineScope: CoroutineScope,
    onDismiss: () -> Unit
) {
    val overlayColor = colorResource(id = R.color.background_overlay)
    val fieldBgColor = colorResource(id = R.color.text_field_background)
    val loginButtonColor = colorResource(id = R.color.spotify_green)
    val white = colorResource(id = R.color.purritify_white)
    val grey = colorResource(id = R.color.text_grey)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Upload Song", color = Color.White, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            BoxWithConstraints {
                val boxSize = (maxWidth - 100.dp) / 2
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(boxSize)
                            .border(BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(8.dp))
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Upload Photo", color = Color.White)
                    }

                    Box(
                        modifier = Modifier
                            .size(boxSize)
                            .border(BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(8.dp))
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Upload File", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = "title",
                    onValueChange = {},
                    label = { Text("Title") },
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
                    value = "artist",
                    onValueChange = {},
                    label = { Text("Artist") },
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
                Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                    Text("Cancel")
                }

                Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = loginButtonColor)) {
                    Text("Save")
                }
            }
        }
    }
}
