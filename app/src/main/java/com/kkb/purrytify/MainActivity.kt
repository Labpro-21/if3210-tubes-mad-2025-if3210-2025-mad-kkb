package com.kkb.purrytify

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kkb.purrytify.TokenStorage.refreshAccessTokenIfNeeded
import com.kkb.purrytify.viewmodel.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.kkb.purrytify.data.remote.ApiService
import com.kkb.purrytify.viewmodel.ChartViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = this
        lifecycleScope.launch {
            val loggedIn = TokenStorage.refreshAccessTokenIfNeeded(context, apiService)
            Log.d("TokenStorage", "loggedIn: $loggedIn")
            val initialRoute = if (loggedIn) "home" else "login"

            setContent {
                val navController = rememberNavController()
                val connectivityObserver = remember { ConnectivityObserver(context) }
                val isConnected by connectivityObserver.observe().collectAsState(initial = true)

                Column {
                    if (!isConnected) {
                        NoInternetPopup()
                    }
                    NavHost(navController = navController, startDestination = initialRoute) {
                        composable("login") {
                            LoginScreen(onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            })
                        }
                        composable("home") {
                            HomeScreen(navController = navController, currentRoute = "home")
                        }
                        composable("profile") {
                            ProfileScreen(navController = navController, currentRoute = "profile")
                        }
                        composable("library") {
                            LibraryScreen(navController = navController, currentRoute = "library")
                        }

                    composable(
                        "track/{songId}",
                        arguments = listOf(navArgument("songId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val songId = backStackEntry.arguments?.getInt("songId")
                        val viewModel: SongViewModel = hiltViewModel()
                        val selectedSong = viewModel.getSongById(songId) // You implement this
                        val songs by viewModel.userSongList.collectAsState()
                        selectedSong?.let { song ->
                            val index = songs.indexOfFirst { it.songId == song.id }
                            Log.d("idsong", "id: $index")
                            Log.d("songids", "$songs")
                            if (index != -1) {
                                TrackScreen(
                                    songs = songs,
                                    initialIndex = index,
                                    navController = navController,
                                    viewModel = viewModel
                                )
                            } else {
                                Log.e("idsong", "Song not found in the list")
                            }
                        }
                    }

                    composable(
                        "track_chart/{index}",
                        arguments = listOf(
                            navArgument("index") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val index = backStackEntry.arguments?.getInt("index") ?: 0
                        val chartViewModel: ChartViewModel = hiltViewModel()
                        val chartSongs by chartViewModel.chartSongs.collectAsState()
                        val isLoading by chartViewModel.isLoading.collectAsState()

                        // Fetch if empty
                        LaunchedEffect(chartSongs) {
                            if (chartSongs.isEmpty()) {
                                chartViewModel.fetchGlobalChart()
                            }
                        }
                        val idsArg = chartSongs.joinToString(",") { it.id.toString() }
                        val ids = idsArg.split(",").mapNotNull { it.toIntOrNull() }
                        val selectedSongs = chartSongs.filter { it.id in ids }
                        val userSongs = selectedSongs.map {
                            UserSong(
                                userId = 0,
                                songId = it.id,
                                title = it.title,
                                artist = it.artist,
                                filePath = it.url,
                                coverPath = it.artwork,
                                isLiked = false,
                                createdAt = java.time.LocalDateTime.now(),
                                lastPlayed = null
                            )
                        }
                        Log.d("charttrack", userSongs.toString())
                        Log.d("charttrackidx", index.toString())
                        if (userSongs.isNotEmpty()) {
                            TrackScreen(
                                songs = userSongs,
                                initialIndex = index.coerceIn(userSongs.indices),
                                navController = navController
                            )
                        }

                    }

                    composable(
                        "charts/global",
                    ) { backStackEntry ->
                        ChartScreen(navController = navController)
                    }


//                    composable("track") {
//                        val backStackEntry = remember {
//                            navController.getBackStackEntry("home")
//                        }
//
//                        val viewModel: SongViewModel = hiltViewModel(backStackEntry)
//                        val selectedSong = viewModel.getSelectedSong()
//                        selectedSong?.let {
//                            TrackScreen(song = it)
//                        }
                    }
                }
            }
        }
    }
}
