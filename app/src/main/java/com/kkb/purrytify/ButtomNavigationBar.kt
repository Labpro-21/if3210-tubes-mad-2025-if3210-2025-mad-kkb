package com.kkb.purrytify

import android.content.Context
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRoute: String,
    context: Context
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Library,
        BottomNavItem.Profile
    )

    var showProfileMenu by remember { mutableStateOf(false) }

    NavigationBar(
        containerColor = Color.Black,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route

            if (item is BottomNavItem.Profile) {
                NavigationBarItem(
                    icon = {
                        Box(
                            modifier = Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        showProfileMenu = true
                                    },
                                    onTap = {
                                        if (!selected) {
                                            navController.navigate(item.route)
                                        }
                                    }
                                )
                            }
                        ) {
                            Icon(item.icon, contentDescription = item.title)
                        }
                    },
                    label = { Text(item.title) },
                    selected = selected,
                    onClick = {}, // handled inside gestures
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1DB954),
                        selectedTextColor = Color(0xFF1DB954),
                        indicatorColor = Color.Black,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // DropdownMenu di luar row scope
                DropdownMenu(
                    expanded = showProfileMenu,
                    onDismissRequest = { showProfileMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = {
                            showProfileMenu = false
                            showProfileMenu = false
                            TokenStorage.clearToken(context)
                            navController.navigate("login") {
                                popUpTo("home") {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }
            } else {
                NavigationBarItem(
                    icon = { Icon(item.icon, contentDescription = item.title) },
                    label = { Text(item.title) },
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            navController.navigate(item.route)
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1DB954),
                        selectedTextColor = Color(0xFF1DB954),
                        indicatorColor = Color.Black,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    }
}
