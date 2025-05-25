package com.kkb.purrytify

import android.net.Uri
import android.util.Log
import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kkb.purrytify.ui.components.SongView
import com.kkb.purrytify.ui.components.SongViewBig
import com.kkb.purrytify.util.MediaPlayerManager
import com.kkb.purrytify.viewmodel.ChartViewModel
import com.kkb.purrytify.viewmodel.SongViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(navController: NavController, currentRoute: String) {
    val viewModel = hiltViewModel<SongViewModel>()
    val chartViewModel: ChartViewModel = hiltViewModel()
    val songs by viewModel.userSongList.collectAsState()
    val context = LocalContext.current
    val currentSong by MediaPlayerManager.currentSong.collectAsState()
    val isPlaying by MediaPlayerManager.isPlaying.collectAsState()
    val newSongs = songs.sortedByDescending { it.createdAt }
    val recentlyPlayedSongs = songs.filter { it.lastPlayed != null }.sortedByDescending { it.lastPlayed }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(Modifier.fillMaxSize()) {
        if (isLandscape) {
            Row(Modifier.fillMaxSize()) {
                // --- Sidebar Navigation ---
                Column(
                    modifier = Modifier
                        .width(130.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF181818))
                        .padding(vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    BottomNavigationBar(
                        navController = navController,
                        currentRoute = currentRoute,
                        context = context
                    )
                }
                Divider(
                    color = Color.Gray.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )
                // --- Main Content Area ---
                LazyColumn(
                    modifier = Modifier
                        .background(Color.Black)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 24.dp)
                ) {
                    item {
                        Text("Charts", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        val chartTypes = listOf(
                            ChartType("my", "Malaysia Top 10", "charts/MY"),
                            ChartType("us", "United States Top 10", "charts/US"),
                            ChartType("uk", "United Kingdom Top 10", "charts/UK"),
                            ChartType("ch", "Switzerland Top 10", "charts/CH"),
                            ChartType("de", "Germany Top 10", "charts/DE"),
                            ChartType("br", "Brazil Top 10", "charts/BR")
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            items(chartTypes.size) { index ->
                                val chart = chartTypes[index]
                                Column(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .clickable { navController.navigate(chart.route) }
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = "",
                                        contentDescription = chart.title,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        placeholder = painterResource(R.drawable.album_placeholder),
                                        error = painterResource(R.drawable.album_placeholder)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = chart.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("New songs", color = Color.White, fontWeight = FontWeight.Bold)
                        LazyRow {
                            items(newSongs.size) { index ->
                                SongViewBig(song = newSongs[index], onClick = {
                                    navController.navigate("track/${newSongs[index].songId}")
                                })
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Recently played", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    items(recentlyPlayedSongs.size) { index ->
                        SongView(song = recentlyPlayedSongs[index], onClick = {
                            navController.navigate("track/${recentlyPlayedSongs[index].songId}")
                        })
                    }

                    item {
                        Spacer(Modifier.height(56.dp)) // Space for NowPlayingBar
                    }
                }
            }
        }else {
            // Portrait
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Charts", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                val chartTypes = listOf(
                    ChartType("my", "Malaysia Top 10", "charts/MY"),
                    ChartType("us", "United States Top 10", "charts/US"),
                    ChartType("uk", "United Kingdom Top 10", "charts/UK"),
                    ChartType("ch", "Switzerland Top 10", "charts/CH"),
                    ChartType("de", "Germany Top 10", "charts/DE"),
                    ChartType("br", "Brazil Top 10", "charts/BR")
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(chartTypes.size) { index ->
                        val chart = chartTypes[index]
                        Column(
                            modifier = Modifier
                                .width(120.dp)
                                .clickable { navController.navigate(chart.route) }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = "",
                                contentDescription = chart.title,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                placeholder = painterResource(R.drawable.album_placeholder),
                                error = painterResource(R.drawable.album_placeholder)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = chart.title,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("New songs", color = Color.White, fontWeight = FontWeight.Bold)
                LazyRow {
                    items(newSongs.size) { index ->
                        val song = newSongs[index]
                        SongViewBig(song = song, onClick = {
                            navController.navigate("track/${song.songId}")
                        })
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Recently played", color = Color.White, fontWeight = FontWeight.Bold)
                LazyColumn {
                    items(recentlyPlayedSongs.size) { index ->
                        val song = recentlyPlayedSongs[index]
                        SongView(song = song, onClick = {
                            navController.navigate("track/${song.songId}")
                        })
                    }
                }
                Spacer(Modifier.height(56.dp)) // Space for NowPlayingBar
            }
        }
        // --- Now Playing Bar and NavBar always on screen, both orientations ---
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            if (currentSong != null && !isLandscape) {
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
                            Log.d("chartype",currentChartType)
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
            if (!isLandscape) {
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    context = context
                )
            }
        }
    }
}


@Composable
private fun NavigationItem(
    @DrawableRes icon: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = if (selected) Color(0xFF1DB954) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            color = if (selected) Color(0xFF1DB954) else Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
private data class ChartType(
    val id: String,
    val title: String,
    val route: String
)

