package com.kkb.purrytify

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text("New songs", color = Color.White, fontSize = 20.sp)

        LazyRow {
            items(5) {
                Column(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .width(120.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.album_placeholder),
                        contentDescription = null,
                        modifier = Modifier
                            .height(120.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Text("Starboy", color = Color.White, fontSize = 14.sp)
                    Text("The Weeknd", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Recently played", color = Color.White, fontSize = 20.sp)

        LazyColumn {
            items(5) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.album_placeholder),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text("Nights", color = Color.White)
                        Text("Frank Ocean", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
