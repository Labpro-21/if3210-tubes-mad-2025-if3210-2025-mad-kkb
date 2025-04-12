package com.kkb.purrytify

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.kkb.purrytify.data.model.Song
import com.kkb.purrytify.R
import com.kkb.purrytify.viewmodel.SongViewModel

@Composable
fun MiniPlayer(
    currentSong: UserSong,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit
) {
    val viewModel = hiltViewModel<SongViewModel>()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xFF1E1E1E))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Album Art
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(currentSong.coverPath)
                        .placeholder(R.drawable.album_placeholder)
                        .error(R.drawable.album_placeholder)
                        .build()
                    ),
                contentDescription = "Album Art",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Song Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = currentSong.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Text(
                    text = currentSong.artist,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Favorite Button
            val songState by viewModel.userSongList.collectAsState()
            val song = songState.find { it.songId == currentSong.songId }
            val isLiked = song?.isLiked ?: false
            IconButton(onClick = {
                viewModel.toggleLike(currentSong.songId)
            }) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isLiked) "Unlike" else "Like",
                    tint = if (isLiked) Color.Red else Color.White
                )
            }

            // Play/Pause Button
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White
                )
            }
        }
    }
}