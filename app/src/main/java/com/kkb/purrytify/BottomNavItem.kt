package com.kkb.purrytify

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    object Home : BottomNavItem("Home", Icons.Default.Home, "home")
    object QrScanner : BottomNavItem("Scan QR", Icons.Default.QrCodeScanner, "qr_scanner")
    object Library : BottomNavItem("Library", Icons.Default.List, "library")
    object Profile : BottomNavItem("Profile", Icons.Default.Person, "profile")
}
