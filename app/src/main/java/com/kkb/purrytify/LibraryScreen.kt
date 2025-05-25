package com.kkb.purrytify

import SongAdapter
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kkb.purrytify.data.model.Song
import com.kkb.purrytify.util.MediaPlayerManager
import com.kkb.purrytify.viewmodel.ChartViewModel
import com.kkb.purrytify.viewmodel.LikeViewModel
import com.kkb.purrytify.viewmodel.SongViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController, currentRoute: String) {
    val viewModel = hiltViewModel<SongViewModel>()
    val chartViewModel: ChartViewModel = hiltViewModel()
    val likeviewModel = hiltViewModel<LikeViewModel>()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val songs by viewModel.userSongList.collectAsState()
    val likes by likeviewModel.likes.collectAsState()
    var selectedTab by remember { mutableStateOf("All") }
    val context = LocalContext.current
    val user_id = TokenStorage.getUserId(context)?.toIntOrNull()
    val currentSong by MediaPlayerManager.currentSong.collectAsState()
    val isPlaying by MediaPlayerManager.isPlaying.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val displayedSongs = if (selectedTab == "Liked") {
        songs.filter { it.isLiked }
    } else {
        songs
    }

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

            // Main Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Library", color = Color.White, fontSize = 20.sp)
                            IconButton(
                                onClick = {
                                    Log.d("Library", "Add button clicked")
                                    coroutineScope.launch {
                                        showBottomSheet = true
                                        sheetState.show()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    if (songs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No songs yet", color = Color.Gray, fontSize = 16.sp)
                            }
                        }
                    } else {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                FilterButton("All", selectedTab == "All") { selectedTab = "All" }
                                Spacer(modifier = Modifier.width(10.dp))
                                FilterButton("Liked", selectedTab == "Liked") { selectedTab = "Liked" }
                            }

                            AndroidView(
                                modifier = Modifier.fillParentMaxHeight(),
                                factory = { ctx ->
                                    RecyclerView(ctx).apply {
                                        layoutManager = LinearLayoutManager(ctx)
                                        adapter = SongAdapter(displayedSongs) { song ->
                                            Log.d("Library", "Navigating to track/${song.songId}")
                                            navController.navigate("track/${song.songId}")
                                        }
                                    }
                                },
                                update = { recyclerView ->
                                    (recyclerView.adapter as? SongAdapter)?.updateList(displayedSongs)
                                }
                            )
                        }
                    }
                }

//                // MiniPlayer at bottom
//                if (currentSong != null) {
//                    Box(
//                        modifier = Modifier
//                            .align(Alignment.BottomEnd)
//                            .fillMaxWidth(0.85f)
//                    ) {
//                        MiniPlayer(
//                            currentSong = currentSong!!,
//                            isPlaying = isPlaying,
//                            onPlayPause = {
//                                if (isPlaying) {
//                                    MediaPlayerManager.pause()
//                                    Log.d("Library", "Player paused")
//                                } else {
//                                    currentSong?.let { song ->
//                                        MediaPlayerManager.play(
//                                            song = song,
//                                            uri = Uri.parse(song.filePath),
//                                            contentResolver = context.contentResolver,
//                                            context = context
//                                        )
//                                        Log.d("Library", "Playing song: ${song.title}")
//                                    }
//                                }
//                            },
//                            onNext = { /* Implement next song logic */ },
//                            onClick = {
//                                val song = currentSong!!
//                                if (song.userId == 0) {
//                                    val chartSongs = chartViewModel.chartSongs.value
//                                    val currentChartType = chartViewModel.currentChartType.value.lowercase()
//                                    if (chartSongs.isEmpty()) {
//                                        chartViewModel.fetchChart(currentChartType)
//                                    }
//                                    val index = chartSongs.indexOfFirst { it.id == song.songId }
//                                    if (index != -1) {
//                                        val route = "track_chart/${currentChartType}/$index"
//                                        Log.d("Library", "Navigating to $route")
//                                        navController.navigate(route)
//                                    }
//                                } else {
//                                    val route = "track/${song.songId}"
//                                    Log.d("Library", "Navigating to $route")
//                                    navController.navigate(route)
//                                }
//                            }
//                        )
//                    }
//                }
            }
        }
    } else {
        // Portrait mode
        Scaffold(
            containerColor = Color.Black,
            bottomBar = {
                Column {
                    if (currentSong != null) {
                        MiniPlayer(
                            currentSong = currentSong!!,
                            isPlaying = isPlaying,
                            onPlayPause = {
                                if (isPlaying) {
                                    MediaPlayerManager.pause()
                                    Log.d("Library", "Player paused")
                                } else {
                                    currentSong?.let { song ->
                                        MediaPlayerManager.play(
                                            song = song,
                                            uri = Uri.parse(song.filePath),
                                            contentResolver = context.contentResolver,
                                            context = context
                                        )
                                        Log.d("Library", "Playing song: ${song.title}")
                                    }
                                }
                            },
                            onNext = { /* Implement next song logic */ },
                            onClick = {
                                val song = currentSong!!
                                if (song.userId == 0) {
                                    val chartSongs = chartViewModel.chartSongs.value
                                    val currentChartType = chartViewModel.currentChartType.value.lowercase()
                                    if (chartSongs.isEmpty()) {
                                        chartViewModel.fetchChart(currentChartType)
                                    }
                                    val index = chartSongs.indexOfFirst { it.id == song.songId }
                                    if (index != -1) {
                                        val route = "track_chart/${currentChartType}/$index"
                                        Log.d("Library", "Navigating to $route")
                                        navController.navigate(route)
                                    }
                                } else {
                                    val route = "track/${song.songId}"
                                    Log.d("Library", "Navigating to $route")
                                    navController.navigate(route)
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
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(Color.Black)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Library", color = Color.White, fontSize = 20.sp)
                        IconButton(
                            onClick = {
                                Log.d("Library", "Add button clicked")
                                coroutineScope.launch {
                                    showBottomSheet = true
                                    sheetState.show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White
                            )
                        }
                    }
                }

                if (songs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No songs yet", color = Color.Gray, fontSize = 16.sp)
                        }
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            FilterButton("All", selectedTab == "All") { selectedTab = "All" }
                            Spacer(modifier = Modifier.width(10.dp))
                            FilterButton("Liked", selectedTab == "Liked") { selectedTab = "Liked" }
                        }

                        AndroidView(
                            modifier = Modifier.fillParentMaxHeight(),
                            factory = { ctx ->
                                RecyclerView(ctx).apply {
                                    layoutManager = LinearLayoutManager(ctx)
                                    adapter = SongAdapter(displayedSongs) { song ->
                                        Log.d("Library", "Navigating to track/${song.songId}")
                                        navController.navigate("track/${song.songId}")
                                    }
                                }
                            },
                            update = { recyclerView ->
                                (recyclerView.adapter as? SongAdapter)?.updateList(displayedSongs)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        UploadSongBottomSheet(
            sheetState = sheetState,
            onDismiss = { showBottomSheet = false },
        ) { title, artist, fileUri, coverPath ->
            viewModel.insertSong(
                context,
                Song(title = title, artist = artist, filePath = fileUri, coverPath = coverPath)
            )
            Log.d("Library", "Uploaded new song: $title by $artist")
            showBottomSheet = false
        }
    }
}

@Composable
fun FilterButton(text: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) Color(0xFF1DB954) else Color(0xFF1E1E1E)
    val textColor = if (selected) Color.Black else Color.White

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}