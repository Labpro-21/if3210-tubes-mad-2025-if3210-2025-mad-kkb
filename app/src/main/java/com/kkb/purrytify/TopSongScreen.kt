package com.kkb.purrytify

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.kkb.purrytify.data.dao.TopArtistTimeListened
import com.kkb.purrytify.data.dao.TopSongTimeListened
import com.kkb.purrytify.util.MediaPlayerManager
import com.kkb.purrytify.viewmodel.ChartViewModel
import com.kkb.purrytify.viewmodel.ProfileViewModel

@Composable
fun TopSongScreen(
    navController: NavController,
    currentRoute: String,
    monthIndex: Int = 0
) {
    val viewModel = hiltViewModel<ProfileViewModel>()
    val context = LocalContext.current
    val userId = TokenStorage.getUserId(context)?.toIntOrNull() ?: return
    val statsState by viewModel.statsState.collectAsState()
    val chartViewModel: ChartViewModel = hiltViewModel()

    val currentSong by MediaPlayerManager.currentSong.collectAsState()
    val isPlaying by MediaPlayerManager.isPlaying.collectAsState()


    LaunchedEffect(key1 = userId) {
        viewModel.fetchProfileStats(userId)
    }

    val capsule = if (statsState.monthlyCapsules.isNotEmpty() &&
        monthIndex < statsState.monthlyCapsules.size) {
        statsState.monthlyCapsules[monthIndex]
    } else null

    val topSongs = capsule?.topSongs ?: emptyList()
    val monthYear = capsule?.month ?: viewModel.currentMonthYear
    val totalSongsListened = capsule?.totalSongsListened ?: 0

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Top songs",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        bottomBar = {
            Column {
                if (currentSong != null) {
                    MiniPlayer(
                        currentSong = currentSong!!,
                        isPlaying = isPlaying,
                        onPlayPause = {
                            if (isPlaying) MediaPlayerManager.pause()
                            else currentSong?.let { song ->
                                MediaPlayerManager.play(
                                    song = song,
                                    uri = Uri.parse(song.filePath),
                                    contentResolver = context.contentResolver,
                                    context = context
                                )
                            }
                        },
                        onNext = { /* Implement next song logic */ },
                        onClick = {
                            val song = currentSong!!
                            if (song.userId == 0) {
                                val chartSongs = chartViewModel.chartSongs.value
                                val currentChartType = chartViewModel.currentChartType.value.lowercase()
                                Log.d("chartype", currentChartType)
                                if (chartSongs.isEmpty()) {
                                    chartViewModel.fetchChart(currentChartType)
                                }

                                val index = chartSongs.indexOfFirst { it.id == song.songId }
                                if (index != -1) {
                                    navController.navigate("track_chart/${currentChartType}/$index")
                                }
                            } else {
                                navController.navigate("track/${song.songId}")
                            }
                        }
                    )
                }
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    context = context
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .background(Color.Black)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = monthYear,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                val annotatedText = buildAnnotatedString {
                    append("You listened to ")
                    withStyle(style = SpanStyle(color = Color.Yellow, fontWeight = FontWeight.Bold)) {
                        append("$totalSongsListened different songs")
                    }
                    append(" this month.")
                }
                Text(
                    text = annotatedText,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (topSongs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No listening data available for this month.",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn {
                    itemsIndexed(topSongs) { index, song ->
                        SongItem(index = index + 1, song = song)
                        if (index < topSongs.size - 1) {
                            Divider(
                                color = Color.DarkGray.copy(alpha = 0.5f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SongItem(index: Int, song: TopSongTimeListened) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = String.format("%02d", index),
            color = Color.Gray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(30.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
        ) {
            if (!song.coverPath.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(song.coverPath)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = "${song.artist} cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = formatListeningTime(song.timeListened),
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

private fun formatListeningTime(timeInSeconds: Long): String {
    val hours = timeInSeconds / 3600
    val minutes = (timeInSeconds % 3600) / 60

    return when {
        hours > 0 -> "$hours hr ${minutes} min"
        else -> "$minutes min"
    }
}