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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kkb.purrytify.TokenStorage.refreshAccessTokenIfNeeded
import com.kkb.purrytify.viewmodel.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.layout.Column

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = this
        lifecycleScope.launch {
            val loggedIn = refreshAccessTokenIfNeeded(context)
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
                        composable("track") {
                            val backStackEntry = remember(navController) {
                                navController.getBackStackEntry("home")
                            }

                        val viewModel: SongViewModel = hiltViewModel(backStackEntry)
                        val songs by viewModel.songs.collectAsState()
                        val selectedSong = viewModel.getSelectedSong()
                        selectedSong?.let { song ->
                            val index = songs.indexOfFirst { it.id == song.id }
                            Log.d("idsong", "id: $index")
                            if (index != -1) {
                                TrackScreen(
                                    songs = songs,
                                    initialIndex = index
                                )
                            } else {
                                Log.e("idsong", "Song not found in the list")
                            }
                        }
//                        selectedSong?.let {
//                            TrackScreen(song = it)
//                        }
                        }
                    }
                }
            }
        }
    }
}
