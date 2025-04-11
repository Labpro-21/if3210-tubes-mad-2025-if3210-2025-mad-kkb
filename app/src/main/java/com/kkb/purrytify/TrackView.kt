package com.kkb.purrytify

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkb.purrytify.data.model.Song

//@Preview
@Composable
fun TrackScreen(song: Song) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFB31217), // Top gradient
                        Color(0xFF000000)  // Bottom gradient
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            // Top icons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Back",
                    tint = Color.White
                )
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Album Art
            Image(
                painter = painterResource(id = R.drawable.ic_music), // Replace with your actual drawable
                contentDescription = "Album Art",
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // title
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            // artist
            Text(
                text = song.artist,
                color = Color.LightGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Playback progress
            Slider(
                value = 0.45f,
                onValueChange = {},
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.Gray
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1:44", color = Color.White, fontSize = 12.sp)
                Text("3:50", color = Color.White, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                IconButton(
                    onClick = { /* TODO: handle play/pause */ },
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

