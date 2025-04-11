package com.kkb.purrytify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = this
        val accessToken = TokenStorage.getToken(context)
        val initialRoute = if (accessToken != null && isTokenValid(accessToken)) "home" else "login"

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
