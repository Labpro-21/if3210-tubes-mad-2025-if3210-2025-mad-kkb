package com.kkb.purrytify

import SongAdapter
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.kkb.purrytify.viewmodel.SongViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController, currentRoute: String){
    val viewModel = hiltViewModel<SongViewModel>()
    val sheetState = rememberModalBottomSheetState( skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val songs by viewModel.songs.collectAsState()
    var selectedTab by remember { mutableStateOf("All") }
    val context = LocalContext.current
    val currentSong by MediaPlayerManager.currentSong.collectAsState()
    val isPlaying by MediaPlayerManager.isPlaying.collectAsState()

    val displayedSongs = remember(selectedTab, songs) {
        if (selectedTab == "Liked") songs.filter { it.isLiked } else songs
    }
    if (showBottomSheet) {
        UploadSongBottomSheet(
            sheetState = sheetState,
            onDismiss = { showBottomSheet = false },
        ) { title, artist, fileUri, coverPath ->
            viewModel.insertSong(Song(title = title, artist = artist, filePath = fileUri, coverPath = coverPath))
//            Log.d(viewModel.getSongs())
            showBottomSheet = false
        }
    }
    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            Column { // Changed from Box to Column
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
                                    contentResolver = context.contentResolver
                                )
                            }
                        },
                        onNext = { /* Implement next song logic */ },
                        onClick = {
                            navController.navigate("track/${currentSong!!.id}")
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
            Spacer(modifier = Modifier.height(16.dp))
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
                    Text("Library", color = Color.White, fontSize = 20.sp)
                    IconButton(onClick = {
                        // TODO: Tambahkan aksi saat tombol diklik
                        coroutineScope.launch {
                            showBottomSheet = true
                            sheetState.show()
                        }

                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White
                        )
                    }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ){
                    FilterButton("All", selectedTab == "All") { selectedTab = "All" }
                    Spacer(modifier = Modifier.width(10.dp))
                    FilterButton("Liked", selectedTab == "Liked") { selectedTab = "Liked" }
                }

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        RecyclerView(ctx).apply {
                            layoutManager = LinearLayoutManager(ctx)
                            adapter = SongAdapter(displayedSongs) { song ->
                                viewModel.selectSong(song)
                                navController.navigate("track/${song.id}")
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