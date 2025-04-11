package com.kkb.purrytify

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kkb.purrytify.TokenStorage.refreshAccessTokenIfNeeded

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
                }
            }
        }
    }
}
