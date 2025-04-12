package com.kkb.purrytify

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.kkb.purrytify.data.model.Song
import com.kkb.purrytify.ui.components.SongView
import com.kkb.purrytify.ui.components.SongViewBig
import com.kkb.purrytify.util.MediaPlayerManager
import com.kkb.purrytify.viewmodel.SongViewModel
import kotlinx.coroutines.launch

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController = navController, currentRoute = "home")
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, currentRoute: String) {
    val viewModel = hiltViewModel<SongViewModel>()
    val songs by viewModel.songs.collectAsState()
    val context = LocalContext.current
    val currentSong by MediaPlayerManager.currentSong.collectAsState()
    val isPlaying by MediaPlayerManager.isPlaying.collectAsState()

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
                    items(songs) { song ->
                        SongViewBig(song = song, onClick = {
                            viewModel.selectSong(song)
                            navController.navigate("track/${song.id}")
                        })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Recently played", color = Color.White, fontSize = 20.sp)

                LazyColumn {
                    items(songs) { song ->
                        SongView(song = song, onClick = {
                            viewModel.selectSong(song)
                            navController.navigate("track/${song.id}")
                        })
                    }
                }
            }
        }
    }
}
