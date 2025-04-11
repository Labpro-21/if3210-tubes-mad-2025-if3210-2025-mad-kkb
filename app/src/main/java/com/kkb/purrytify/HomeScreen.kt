package com.kkb.purrytify

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.kkb.purrytify.data.model.Song
import com.kkb.purrytify.ui.components.SongView
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
    val sheetState = rememberModalBottomSheetState( skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val songs by viewModel.songs.collectAsState()
    if (showBottomSheet) {
        UploadSongBottomSheet(
            sheetState = sheetState,
            onDismiss = { showBottomSheet = false },
        ) { title, artist, fileUri ->
            viewModel.insertSong(Song(title = title, artist = artist, filePath = fileUri))
//            Log.d(viewModel.getSongs())
            showBottomSheet = false
        }
    }
    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
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
                Text("New songs", color = Color.White, fontSize = 20.sp)
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
                        SongView(song = song)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Recently played", color = Color.White, fontSize = 20.sp)

                LazyColumn {
                    items(songs) { song ->
                        SongView(song = song)
                    }
                }
            }

        }
    }
}
