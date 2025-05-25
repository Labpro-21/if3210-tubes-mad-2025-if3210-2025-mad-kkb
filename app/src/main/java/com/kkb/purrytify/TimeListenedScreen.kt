package com.kkb.purrytify

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
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
import com.kkb.purrytify.data.dao.DailyTimeListened
import com.kkb.purrytify.util.MediaPlayerManager
import com.kkb.purrytify.viewmodel.ChartViewModel
import com.kkb.purrytify.viewmodel.ProfileViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

@Composable
fun TimeListenedScreen(
    navController: NavController,
    currentRoute: String,
    monthIndex: Int = 0
) {
    val viewModel = hiltViewModel<ProfileViewModel>()
    val context = LocalContext.current
    val userId = TokenStorage.getUserId(context)?.toIntOrNull() ?: return
    val statsState by viewModel.statsState.collectAsState()
    val chartViewModel: ChartViewModel = hiltViewModel()
    val monthlyCapsules = statsState.monthlyCapsules

    val currentSong by MediaPlayerManager.currentSong.collectAsState()
    val isPlaying by MediaPlayerManager.isPlaying.collectAsState()

    LaunchedEffect(key1 = userId) {
        viewModel.fetchProfileStats(userId)
    }

    val capsule = if (statsState.monthlyCapsules.isNotEmpty() &&
        monthIndex < statsState.monthlyCapsules.size) {
        statsState.monthlyCapsules[monthIndex]
    } else null

    val monthYear = capsule?.month ?: viewModel.currentMonthYear

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
                    text = "Time Listened",
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
                // Month display
                Text(
                    text = monthYear,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Total time listened text
                val totalMinutes = capsule?.totalTimeListened?.div(60) ?: 0
                Text(
                    text = buildAnnotatedString {
                        append("You listened to music for\n")
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF1DB954),
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("$totalMinutes minutes")
                        }
                        append(" this month.")
                    },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Daily average
                val dailyAverage = if (capsule != null && capsule.dailyTime.isNotEmpty()) {
                    totalMinutes / capsule.dailyTime.size
                } else 0

                Text(
                    text = "Daily average: $dailyAverage min",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFF1A1A1A))
                        .padding(16.dp)
                ) {
                    if (capsule != null && capsule.dailyTime.isNotEmpty()) {
                        DailyListeningChart(capsule.dailyTime)
                    } else {
                        Text(
                            text = "No listening data available",
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

//                    Text(
//                        text = "Daily Chart",
//                        color = Color.White,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold,
//                        modifier = Modifier.align(Alignment.Center)
//                    )
                }
            }
        }
    }
}

@Composable
fun DailyListeningChart(dailyData: List<DailyTimeListened>) {
    val maxTimeListened = dailyData.maxOfOrNull { it.totalTimeListened / 60 } ?: 0
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val width = size.width
        val height = size.height
        val yAxisEndX = 40f
        val xAxisStartY = height - 40f

        // Draw y-axis
        drawLine(
            color = Color.White,
            start = Offset(yAxisEndX, 0f),
            end = Offset(yAxisEndX, xAxisStartY),
            strokeWidth = 2f
        )

        // Draw x-axis
        drawLine(
            color = Color.White,
            start = Offset(yAxisEndX, xAxisStartY),
            end = Offset(width, xAxisStartY),
            strokeWidth = 2f
        )

        // Draw chart data if available
        if (dailyData.isNotEmpty() && maxTimeListened > 0) {
            val availableWidth = width - yAxisEndX - 10f
            val barWidth = availableWidth / dailyData.size
            val barSpacing = barWidth * 0.2f
            val actualBarWidth = barWidth - barSpacing

            dailyData.forEachIndexed { index, data ->
                val minutes = data.totalTimeListened / 60
                val barHeight = (minutes.toFloat() / maxTimeListened) * (xAxisStartY - 20f)
                val startX = yAxisEndX + (barWidth * index) + (barSpacing / 2)

                // Draw bar
                drawRect(
                    color = Color(0xFF1DB954),
                    topLeft = Offset(startX, xAxisStartY - barHeight),
                    size = androidx.compose.ui.geometry.Size(actualBarWidth, barHeight)
                )
            }
        }

        // Y-axis label
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 10.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
        }

        // Save canvas state before rotating
        drawContext.canvas.nativeCanvas.save()
        // Rotate for vertical text
        drawContext.canvas.nativeCanvas.rotate(-90f, 10f, height / 2)
        // Draw the text
        drawContext.canvas.nativeCanvas.drawText(
            "minutes",
            10f,
            height / 2,
            paint
        )
        // Restore canvas to original state
        drawContext.canvas.nativeCanvas.restore()

        // X-axis label - no rotation needed
        drawContext.canvas.nativeCanvas.drawText(
            "day",
            width - 15f,
            height - 10f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 10.sp.toPx()
                textAlign = android.graphics.Paint.Align.RIGHT
            }
        )
    }
}