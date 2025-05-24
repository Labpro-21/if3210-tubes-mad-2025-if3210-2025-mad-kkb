package com.kkb.purrytify

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kkb.purrytify.ui.components.SongView
import com.kkb.purrytify.ui.components.SongViewBig
import com.kkb.purrytify.util.MediaPlayerManager
import com.kkb.purrytify.viewmodel.ChartViewModel
import com.kkb.purrytify.viewmodel.SongViewModel
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController = navController, currentRoute = "home")
}

private data class ChartType(
    val id: String,
    val title: String,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, currentRoute: String) {
    val viewModel = hiltViewModel<SongViewModel>()
    val chartViewModel: ChartViewModel = hiltViewModel()
    val songs by viewModel.userSongList.collectAsState()
    val context = LocalContext.current
    val currentSong by MediaPlayerManager.currentSong.collectAsState()
    val isPlaying by MediaPlayerManager.isPlaying.collectAsState()
    val newSongs = songs.sortedByDescending { it.createdAt }
    val recentlyPlayedSongs = songs
        .filter { it.lastPlayed != null }
        .sortedByDescending { it.lastPlayed }
    Scaffold(
        containerColor = Color.Black,
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Charts", color = Color.White, fontSize = 20.sp)

            Spacer(modifier = Modifier.height(8.dp))

            val chartTypes = listOf(
                ChartType("global", "Global Top 50", "charts/global"),
                ChartType("id", "Indonesia Top 10", "charts/ID"),
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
                items(chartTypes) { chart ->
                    Column(
                        modifier = Modifier
                            .width(100.dp)
                            .clickable { navController.navigate(chart.route) }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = "",
                            contentDescription = chart.title,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            placeholder = painterResource(R.drawable.album_placeholder),
                            error = painterResource(R.drawable.album_placeholder)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = chart.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text("New songs", color = Color.White, fontSize = 20.sp)
                }
            }
            if(songs.isEmpty()){
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No songs yet", color = Color.Gray, fontSize = 16.sp)
                }
            }else{
                LazyRow {
                    items(newSongs) { song ->
                        SongViewBig(song = song, onClick = {
                            navController.navigate("track/${song.songId}")
                        })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Recently played", color = Color.White, fontSize = 20.sp)

                LazyColumn {
                    items(recentlyPlayedSongs) { song ->
                        SongView(song = song, onClick = {
                            navController.navigate("track/${song.songId}")
                        })
                    }
                }
            }
        }
    }
}