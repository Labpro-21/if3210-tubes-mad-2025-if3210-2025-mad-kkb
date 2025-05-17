package com.kkb.purrytify

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kkb.purrytify.viewmodel.ChartViewModel
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow

@Composable
fun ChartScreen(
    navController: NavController,
    viewModel: ChartViewModel = hiltViewModel()
) {
    val chartSongs by viewModel.chartSongs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchGlobalChart()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
            error != null -> {
                Text(
                    text = error ?: "Unknown error",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Play All Button
                    Button(
                        onClick = {
                            if (chartSongs.isNotEmpty()) {
                                // Pass all song ids as comma-separated string
                                val ids = chartSongs.joinToString(",") { it.id.toString() }
                                Log.d("ids",ids)
                                navController.navigate("track_chart/0")
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play All")
                        Text("Play All", color = Color.White)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        items(chartSongs) { song ->
                            Log.d("chart5", chartSongs.toString())
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${song.rank}",
                                    color = Color.White,
                                    modifier = Modifier.width(32.dp)
                                )
                                AsyncImage(
                                    model = song.artwork,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .padding(end = 12.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        color = Color.White
                                    )
                                    Text(
                                        text = song.artist,
                                        color = Color.Gray
                                    )
                                }
                                Text(
                                    text = song.duration,
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}