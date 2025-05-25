package com.kkb.purrytify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun ResponsiveScaffold(
    topBar: @Composable (() -> Unit)? = null,
    navigation: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable (Modifier) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(Modifier.fillMaxSize()) {
            // Sidebar Navigation
            if (navigation != null) {
                Box(
                    Modifier
                        .width(120.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    navigation()
                }
            }
            Divider(Modifier.fillMaxHeight().width(1.dp))
            // Main content area
            Column(Modifier.weight(1f)) {
                if (topBar != null) {
                    Box(Modifier.fillMaxWidth()) { topBar() }
                }
                Box(Modifier.weight(1f)) {
                    content(Modifier.fillMaxSize())
                }
                if (bottomBar != null) {
                    Box(Modifier.fillMaxWidth()) { bottomBar() }
                }
            }
        }
    } else {
        // Portrait: TopBar, Content, BottomBar
        Scaffold(
            topBar = { topBar?.invoke() },
            bottomBar = { bottomBar?.invoke() }
        ) { innerPadding ->
            Row(Modifier.fillMaxSize().padding(innerPadding)) {
                if (navigation != null) {
                    navigation()
                }
                Box(Modifier.weight(1f)) {
                    content(Modifier.fillMaxSize())
                }
            }
        }
    }
}