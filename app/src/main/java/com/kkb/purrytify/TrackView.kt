package com.kkb.purrytify

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.kkb.purrytify.data.model.Song
import com.kkb.purrytify.util.MediaPlayerManager
import com.kkb.purrytify.viewmodel.SongViewModel
import kotlinx.coroutines.delay

//@Preview
@Composable

fun TrackScreen(
    songs: List<Song>,
    initialIndex: Int,
    navController: NavController,
    viewModel: SongViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val viewModel = hiltViewModel<SongViewModel>()
    var currentIndex by remember { mutableStateOf(initialIndex) }
    val currentSong = songs.getOrNull(currentIndex) ?: return
    viewModel.updateLastPlayed(currentSong.songId)
    var isPlaying by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(1f) }

    var showMenu by remember { mutableStateOf(false) }

    val uri = Uri.parse(currentSong.filePath)

    LaunchedEffect(currentIndex) {
        MediaPlayerManager.play(
            song = currentSong,
            uri = uri,
            contentResolver = contentResolver,
            onError = { Log.e("TrackScreen", "Playback error: ${it.message}") }
        )
        isPlaying = true
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val player = MediaPlayerManager.getPlayer()
            if (player != null && player.isPlaying) {
                val current = player.currentPosition
                val total = player.duration.takeIf { it > 0 } ?: 1
                playbackProgress = current.toFloat() / total
                duration = total.toFloat()
            }
            delay(500)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFB31217),
                        Color(0xFF000000)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Hapus Lagu") },
                            onClick = {
                                showMenu = false
                                viewModel.deleteSong(currentSong)
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(currentSong.coverPath)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .build()
            )

            Image(
                painter = painter,
                contentDescription = "Album Art",
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = currentSong.title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = currentSong.artist,
                color = Color.LightGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
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


            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = playbackProgress,
                onValueChange = { playbackProgress = it },
                onValueChangeFinished = {
                    val player = MediaPlayerManager.getPlayer()
                    player?.seekTo((duration * playbackProgress).toInt())
                },
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.Gray
                )
            )

            val currentSeconds = ((playbackProgress * duration) / 1000).toInt()
            val totalSeconds = (duration / 1000).toInt()

            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    String.format("%d:%02d", currentSeconds / 60, currentSeconds % 60),
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (currentIndex > 0) currentIndex--
                    else currentIndex = songs.lastIndex
                }) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = {
                        if (!isPlaying) {
                            MediaPlayerManager.play(
                                song = currentSong,
                                uri = uri,
                                contentResolver = contentResolver,
                                onError = { e -> Log.e("TrackScreen", "Error: ${e.message}") }
                            )
                        } else {
                            MediaPlayerManager.pause()
                        }
                        isPlaying = MediaPlayerManager.isPlaying.value
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(onClick = {
                    if (currentIndex < songs.lastIndex) currentIndex++
                    else currentIndex = 0
                }) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
