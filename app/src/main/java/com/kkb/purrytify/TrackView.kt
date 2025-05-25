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
import androidx.compose.ui.layout.ContentScale
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
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalConfiguration


//@Preview
@Composable

fun TrackScreen(

    navController: NavController,
    viewModel: SongViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val contentResolver = context.contentResolver
    val currentSong by MediaPlayerManager.currentSong.collectAsState()
    val isPlayerPlaying by MediaPlayerManager.isPlaying.collectAsState()
    if (currentSong == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No song currently playing",
                color = Color.White
            )
        }
        return
    }

    val song = currentSong!!
    
    LaunchedEffect(Unit) {
        val currentPlayingSong = MediaPlayerManager.getCurrentSong()
        if (currentPlayingSong?.songId != song.songId || !isPlayerPlaying) {
            Log.d("tess", song.toString())
            val uri = Uri.parse(song.filePath)
            val isRemote = song.filePath.startsWith("http://") || song.filePath.startsWith("https://")
            MediaPlayerManager.play(
                song = song,
                uri = if (isRemote) null else uri,
                contentResolver = if (isRemote) null else contentResolver,
                isRemote = isRemote,
                context = context,
                onSongStarted = { songId ->
                    viewModel.updateLastPlayed(songId)
                }
            )
        }
    }

    var playbackProgress by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(1f) }
    var lastReportedSeconds by remember { mutableStateOf(0L) }
    var showMenu by remember { mutableStateOf(false) }

    BackHandler {
        val popped = navController.popBackStack()
        if (!popped) {
            navController.navigate("home") {
                popUpTo("home") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(isPlayerPlaying, song.songId) {
        lastReportedSeconds = 0L
        while (isPlayerPlaying) {
            val player = MediaPlayerManager.getPlayer()
            if (player != null && player.isPlaying) {
                val current = player.currentPosition
                val total = player.duration.takeIf { it > 0 } ?: 1
                playbackProgress = current.toFloat() / total
                duration = total.toFloat()
                val seconds = (player.currentPosition / 1000L)
                if (seconds > lastReportedSeconds) {
                    val delta = seconds - lastReportedSeconds
                    if (delta > 0) {
//                        viewModel.updateTimeListened(song.songId, delta)
                        lastReportedSeconds = seconds
                    }
                }
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
        if (isLandscape) {
            // Landscape layout
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side - Album art and basic info
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = {
                            val popped = navController.popBackStack()
                            if (!popped) {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Album Art (smaller)
                    val painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(song.coverPath)
                            .placeholder(R.drawable.album_placeholder)
                            .error(R.drawable.album_placeholder)
                            .size(150) // <<-- smaller size
                            .build()
                    )

                    Image(
                        painter = painter,
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(150.dp) // <<-- smaller size
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title and artist beneath the cover
                    Text(
                        text = song.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2, // allow wrapping
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Text(
                        text = song.artist,
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                // Right side - Controls and additional info
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 32.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
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
                                    viewModel.deleteSong(currentSong!!)
                                    val popped = navController.popBackStack()
                                    if (!popped) {
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Progress slider
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
                        // Time display
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

                        // Playback controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { MediaPlayerManager.previous(context) }) {
                                Icon(
                                    Icons.Default.SkipPrevious,
                                    contentDescription = "Previous",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (isPlayerPlaying) {
                                        MediaPlayerManager.pause(context)
                                    } else {
                                        MediaPlayerManager.resume(context)
                                    }
                                },
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color.White, shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isPlayerPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.Black,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            IconButton(onClick = { MediaPlayerManager.next(context) }) {
                                Icon(
                                    Icons.Default.SkipNext,
                                    contentDescription = "Next",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    // Share + Like button at the bottom right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val songState by viewModel.userSongList.collectAsState()
                        val userSong = songState.find { it.songId == song.songId }
                        val isLiked = userSong?.isLiked ?: false
                        // Like button
                        IconButton(onClick = { viewModel.toggleLike(song.songId) }) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isLiked) "Unlike" else "Like",
                                tint = if (isLiked) Color.Red else Color.White
                            )
                        }
                        // Share button
                        ShareSongButton(currentSong!!)
                    }
                }
            }
        }else {
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
                        IconButton(onClick = {
                            // Try to pop from back stack, if not possible go to home
                            val popped = navController.popBackStack()
                            if (!popped) {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }) {
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
                                        viewModel.deleteSong(currentSong!!)
                                        // Try to pop from back stack, if not possible go to home
                                        val popped = navController.popBackStack()
                                        if (!popped) {
                                            navController.navigate("home") {
                                                popUpTo("home") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                )
                            }
                        }

                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    val painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(song.coverPath)
                            .placeholder(R.drawable.album_placeholder)
                            .error(R.drawable.album_placeholder)
                            .size(280)
                            .build()
                    )

                    Image(
                        painter = painter,
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = song.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = song.artist,
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    val songState by viewModel.userSongList.collectAsState()
                    val userSong = songState.find { it.songId == song.songId }
                    val isLiked = userSong?.isLiked ?: false
                    IconButton(onClick = {
                        viewModel.toggleLike(song.songId)
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
                            MediaPlayerManager.previous(context)
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
                                if (isPlayerPlaying) {
                                    MediaPlayerManager.pause(context)
                                } else {
                                    MediaPlayerManager.resume(context)
                                }
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.White, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlayerPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(onClick = {
                            MediaPlayerManager.next(context)
                        }) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShareSongButton(currentSong!!)
//                val domain = context.getString(R.string.deeplink_domain)
//                IconButton(onClick = {
//                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
//                        type = "text/plain"
//                        putExtra(Intent.EXTRA_SUBJECT, "Check out this song!")
//                        putExtra(
//                            Intent.EXTRA_TEXT,
//                            "https://$domain/song/${currentSong.songId}"
//                        )
//                    }
//                    context.startActivity(Intent.createChooser(shareIntent, "Share song via"))
//                }) {
//                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White)
//                }
                    }
                }
        }
    }
}
