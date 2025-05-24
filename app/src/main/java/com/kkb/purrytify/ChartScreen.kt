package com.kkb.purrytify

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kkb.purrytify.data.model.ChartSong
import com.kkb.purrytify.viewmodel.ChartViewModel
import kotlinx.coroutines.launch

@Composable
fun ChartScreen(
    navController: NavController,
    viewModel: ChartViewModel = hiltViewModel()
) {
    val chartSongs by viewModel.chartSongs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val userId = TokenStorage.getUserId(context)?.toIntOrNull() ?: 0

    LaunchedEffect(chartType) {
        viewModel.fetchChart(chartType.lowercase())
    }

    val chartSongs by viewModel.chartSongs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentChartType by viewModel.currentChartType.collectAsState()

    val chartTitle = getChartTitle(currentChartType)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
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
                       Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (chartSongs.isNotEmpty()) {
                                        coroutineScope.launch {
                                            viewModel.downloadChartToLocal(userId)
                                            snackbarHostState.showSnackbar("Chart downloaded to library")
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1DB954),
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Download")
                                Spacer(Modifier.width(8.dp))
                                Text("Download")
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))

                            Button(
                                onClick = {
                                    if (chartSongs.isNotEmpty()) {
                                        navController.navigate("track_chart/${currentChartType.lowercase()}/0")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1DB954),
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play All")
                                Spacer(Modifier.width(8.dp))
                                Text("Play All")
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            items(chartSongs) { song ->
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
}