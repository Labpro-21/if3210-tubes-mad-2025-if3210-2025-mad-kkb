package com.kkb.purrytify

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kkb.purrytify.data.model.Song
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
    val viewModel: SongViewModel = viewModel()
    val sheetState = rememberModalBottomSheetState( skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    if (showBottomSheet) {
        UploadSongBottomSheet(
            sheetState = sheetState,
            onDismiss = { showBottomSheet = false },
        ) { title, artist ->
            viewModel.insertSong(Song(title = title, artist = artist, filePath = ""))
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
                        imageVector = Icons.Default.List,
                        contentDescription = "Add",
                        tint = Color.White
                    )
                }
            }

            LazyRow {
                items(5) {
                    Column(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .width(120.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.album_placeholder),
                            contentDescription = null,
                            modifier = Modifier
                                .height(120.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Text("Starboy", color = Color.White, fontSize = 14.sp)
                        Text("The Weeknd", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Recently played", color = Color.White, fontSize = 20.sp)

            LazyColumn {
                items(5) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.album_placeholder),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text("Nights", color = Color.White)
                            Text("Frank Ocean", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
