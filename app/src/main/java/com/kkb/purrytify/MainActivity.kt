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
                        val song = viewModel.getSongById(songId) // You implement this
                        song?.let { TrackScreen(song = it) }
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
////                        selectedSong?.let {
////                            TrackScreen(song = it)
////                        }
//                    }
                }
            }
        }
    }
}
