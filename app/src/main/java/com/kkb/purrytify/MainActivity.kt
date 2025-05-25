package com.kkb.purrytify

import android.net.Uri
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
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.kkb.purrytify.data.remote.ApiService
import com.kkb.purrytify.util.MediaPlayerManager
import com.kkb.purrytify.viewmodel.ChartViewModel
import javax.inject.Inject
import com.kkb.purrytify.util.CheckNotificationPermission

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

            // Handle deep link
            val data: Uri? = intent?.data
            val deepLinkSongId = if (data?.scheme == "purrytify" && data.host == "song") {
                data.lastPathSegment?.toIntOrNull()
            } else null

            val initialRoute = when {
                deepLinkSongId != null -> "track-link/$deepLinkSongId"
                loggedIn -> "home"
                else -> "login"
            }

            setContent {
                CheckNotificationPermission()
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
                            "top-artist/{monthIndex}",
                            arguments = listOf(navArgument("monthIndex") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val monthIndex = backStackEntry.arguments?.getInt("monthIndex") ?: 0
                            TopArtistScreen(
                                navController = navController,
                                currentRoute = "top-artist",
                                monthIndex = monthIndex
                            )
                        }

                        composable(
                            "top-song/{monthIndex}",
                            arguments = listOf(navArgument("monthIndex") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val monthIndex = backStackEntry.arguments?.getInt("monthIndex") ?: 0
                            TopSongScreen(
                                navController = navController,
                                currentRoute = "top-song",
                                monthIndex = monthIndex
                            )
                        }

                        composable(
                            "time-listened/{monthIndex}",
                            arguments = listOf(navArgument("monthIndex") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val monthIndex = backStackEntry.arguments?.getInt("monthIndex") ?: 0
                            TimeListenedScreen(
                                navController = navController,
                                currentRoute = "time-listened",
                                monthIndex = monthIndex
                            )
                        }
                        composable("qr_scanner"){
                            QrScannerScreen(
                                onResult = { code ->
                                    // Handle result, e.g. extract songId and navigate
                                    val songId = code.substringAfterLast("/").toIntOrNull()
                                    if (songId != null) {
                                        navController.navigate("track-link/$songId")
                                    } else {
                                        navController.popBackStack() // or show an error
                                    }
                                },
                                onCancel = { navController.popBackStack() }
                            )
                        }

                        composable(
                            "track-link/{songId}",
                            arguments = listOf(navArgument("songId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val songId = backStackEntry.arguments?.getInt("songId")
                            val viewModel: SongViewModel = hiltViewModel()
                            val songs by viewModel.userSongList.collectAsState()
                            val remoteSong by viewModel.remoteSong.collectAsState()
                            Log.d("songId", "link: $songId")

                            // Find the song object in the downloaded songs list
                            val selectedSong = songs.find { it.songId == songId }

                            // If not found, trigger remote fetch
                            if (selectedSong == null && songId != null) {
                                LaunchedEffect(songId) {
                                    viewModel.getRemoteSong(songId)
                                }
                            }

                            // Use selectedSong if available, otherwise use remoteSong
                            val userSong = selectedSong ?: remoteSong

                            // Loading state if remoteSong is still null and song not found locally
                            if (userSong == null) {
                                // Show loading or error UI here if needed
                                Log.e("idsong", "Song not found in the database")
                                // You may want to show a ProgressBar or a placeholder here
                                return@composable
                            }

                            // Reorder the song list: linked song first, then the rest (no duplicates)
                            val reorderedSongs = listOf(userSong) + songs.filter { it.songId != userSong.songId }
                            LaunchedEffect(reorderedSongs, 0) {
                                MediaPlayerManager.setPlaylist(reorderedSongs, 0)
                            }
                            TrackScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable(
                        "track/{songId}",
                        arguments = listOf(navArgument("songId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val songId = backStackEntry.arguments?.getInt("songId")
                        val viewModel: SongViewModel = hiltViewModel()
                        val selectedSong = viewModel.getSongById(songId)
                        val songs by viewModel.userSongList.collectAsState()
                        selectedSong?.let { song ->
                            val index = songs.indexOfFirst { it.songId == song.id }
                            Log.d("idsong", "id: $index")
                            Log.d("songids", "$songs")
                            if (index != -1) {
                                // Set the playlist in MediaPlayerManager
                                LaunchedEffect(songs, index) {
                                    MediaPlayerManager.setPlaylist(songs, index)
                                }
                                TrackScreen(
                                    navController = navController,
                                    viewModel = viewModel
                                )
                            } else {
                                Log.e("idsong", "Song not found in the list")
                                }
                            }
                        }

                    composable(
                        "track_chart/{chartType}/{index}",
                        arguments = listOf(
                            navArgument("chartType") { type = NavType.StringType },
                            navArgument("index") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val chartType = backStackEntry.arguments?.getString("chartType") ?: "global"
                        val index = backStackEntry.arguments?.getInt("index") ?: 0
                        val chartViewModel: ChartViewModel = hiltViewModel()

                        val chartSongs by chartViewModel.chartSongs.collectAsState()
                        val isLoading by chartViewModel.isLoading.collectAsState()

                        LaunchedEffect(chartType) {
                            chartViewModel.fetchChart(chartType)
                        }

                        val userSongs = chartSongs.map { chartSong ->
                            UserSong(
                                userId = 0,
                                songId = chartSong.id,
                                title = chartSong.title,
                                artist = chartSong.artist,
                                filePath = chartSong.url,
                                coverPath = chartSong.artwork,
                                isLiked = false,
                                createdAt = java.time.LocalDateTime.now(),
                                lastPlayed = null
                            )
                        }

                        if (userSongs.isNotEmpty()) {
                            val safeIndex = index.coerceIn(userSongs.indices)
                            LaunchedEffect(userSongs, safeIndex) {
                                MediaPlayerManager.setPlaylist(userSongs, safeIndex)
                            }
                            TrackScreen(
                                navController = navController
                            )
                        }
                    }

                    composable(
                        "charts/global",
                    ) { backStackEntry ->
                        ChartScreen(navController = navController)
                    }

                    composable(
                        "charts/ID",
                    ) { backStackEntry ->
                        ChartScreen(navController = navController, chartType = "ID")
                    }

                    composable(
                        "charts/MY",
                    ) { backStackEntry ->
                        ChartScreen(navController = navController, chartType = "MY")
                    }

                    composable(
                        "charts/US",
                    ) { backStackEntry ->
                        ChartScreen(navController = navController, chartType = "US")
                    }

                    composable(
                        "charts/UK",
                    ) { backStackEntry ->
                        ChartScreen(navController = navController, chartType = "UK")
                    }

                    composable(
                        "charts/CH",
                    ) { backStackEntry ->
                        ChartScreen(navController = navController, chartType = "CH")
                    }

                    composable(
                        "charts/DE",
                    ) { backStackEntry ->
                        ChartScreen(navController = navController, chartType = "DE")
                    }

                    composable(
                        "charts/BR",
                    ) { backStackEntry ->
                        ChartScreen(navController = navController, chartType = "BR")
                    }

                    composable("edit_profile") {
                        EditProfileScreen(navController = navController)
                    }

                    }
                }
            }
        }
    }
}
