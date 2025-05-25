package com.kkb.purrytify

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.hilt.navigation.compose.hiltViewModel
import com.kkb.purrytify.util.MediaPlayerManager
import com.kkb.purrytify.viewmodel.ChartViewModel

@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRoute: String,
    context: Context
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.QrScanner,
        BottomNavItem.Library,
        BottomNavItem.Profile
    )
    val chartViewModel: ChartViewModel = hiltViewModel()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val currentSong by MediaPlayerManager.currentSong.collectAsState()
    val isPlaying by MediaPlayerManager.isPlaying.collectAsState()
    var showProfileMenu by remember { mutableStateOf(false) }
    var profileIconOffset by remember { mutableStateOf(Offset.Zero) }

    val density = LocalDensity.current

    if (isLandscape) {
        // Vertical Navigation for Landscape
        Column(
            modifier = Modifier
                .width(120.dp)
                .fillMaxHeight()
                .background(Color(0xFF181818))
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            items.forEachIndexed { index, item ->
                val selected = currentRoute == item.route


                if (item is BottomNavItem.Profile) {
                    Box {
                        NavigationRow(
                            icon = item.icon,
                            label = item.title,
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.route)
                                    Log.d("Navigation", "Clicked route: ${item.route}")
                                }
                            },
                            onLongPress = { showProfileMenu = true }
                        )

                        // Profile Dropdown Menu
                        ProfileDropdownMenu(
                            showMenu = showProfileMenu,
                            onDismiss = { showProfileMenu = false },
                            onLogout = {
                                showProfileMenu = false
                                MediaPlayerManager.stop(context)
                                TokenStorage.clearToken(context)
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            density = density,
                            offset = profileIconOffset
                        )
                    }
                } else {
                    NavigationRow(
                        icon = item.icon,
                        label = item.title,
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(item.route)
                                Log.d("Navigation", "Clicked route: ${item.route}")
                            }
                        }
                    )
                }
            }
            // MiniPlayer at bottom of sidebar
            if (currentSong != null) {
                Divider(
                    color = Color.Gray.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
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
                                        contentResolver = context.contentResolver,
                                        context = context
                                    )
                                }
                            },
                            onNext = { /* Implement next song logic */ },
                            onClick = {
                                val song = currentSong!!
                                if (song.userId == 0) {
                                    val chartSongs = chartViewModel.chartSongs.value
                                    val currentChartType =
                                        chartViewModel.currentChartType.value.lowercase()
                                    Log.d("chartype", currentChartType)
                                    if (chartSongs.isEmpty()) {
                                        chartViewModel.fetchChart(currentChartType)
                                    }

                                    val index = chartSongs.indexOfFirst { it.id == song.songId }
                                    if (index != -1) {
                                        navController.navigate("track_chart/${currentChartType}/$index")
                                    }
                                } else {
                                    navController.navigate("track/${song.songId}")
                                }
                            }
                        )
                    }
                }
            }
        }

    } else {
        NavigationBar(
            containerColor = Color.Black,
            tonalElevation = 8.dp
        ) {
            items.forEachIndexed { index, item ->
                val selected = currentRoute == item.route
                if (item is BottomNavItem.Profile) {
                    NavigationBarItem(
                        icon = {
                            Box(
                                modifier = Modifier
                                    .pointerInput(Unit) {
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
                                    .onGloballyPositioned { coordinates ->
                                        val position = coordinates.localToWindow(Offset.Zero)
                                        profileIconOffset = position
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

                    // Popup Logout tepat di atas ikon profile
                    DropdownMenu(
                        expanded = showProfileMenu,
                        onDismissRequest = { showProfileMenu = false },
                        offset = with(density) {
                            DpOffset(
                                x = profileIconOffset.x.toDp(),
                                y = (profileIconOffset.y - 60.dp.toPx()).toDp()
                            )
                        },
                        modifier = Modifier
                            .background(Color.Transparent)
                            .padding(0.dp),
                        containerColor = Color.Transparent
                    ) {
                        DropdownMenuItem(
                            text = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFF3B30), RoundedCornerShape(8.dp))
                                        .padding(vertical = 8.dp, horizontal = 12.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Start,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = "Logout",
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Logout",
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            },
                            onClick = {
                                showProfileMenu = false
                                TokenStorage.clearToken(context)
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
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
}

@Composable
private fun NavigationRow(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongPress: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .then(
                if (onLongPress != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onClick() },
                            onLongPress = { onLongPress() }
                        )
                    }
                } else {
                    Modifier.clickable(onClick = onClick)
                }
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color(0xFF1DB954) else Color.Gray
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = if (selected) Color(0xFF1DB954) else Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ProfileDropdownMenu(
    showMenu: Boolean,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    density: Density,
    offset: Offset
) {
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = onDismiss,
        offset = with(density) {
            DpOffset(
                x = offset.x.toDp(),
                y = (offset.y - 60.dp.toPx()).toDp()
            )
        },
        modifier = Modifier
            .background(Color.Transparent)
            .padding(0.dp),
        containerColor = Color.Transparent
    ) {
        DropdownMenuItem(
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFF3B30), RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Logout",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}


