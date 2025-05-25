package com.kkb.purrytify

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kkb.purrytify.viewmodel.ProfileStatsUiState
import com.kkb.purrytify.viewmodel.ProfileUiState
import com.kkb.purrytify.components.SoundCapsule
import com.kkb.purrytify.viewmodel.ProfileViewModel
import com.kkb.purrytify.viewmodel.SongViewModel
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.util.*

@Composable
fun ProfileScreen(
    navController: NavController = rememberNavController(),
    currentRoute: String = "profile",
    context: Context = LocalContext.current
) {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val songViewModel: SongViewModel = hiltViewModel()
    val uiState by profileViewModel.uiState.collectAsState()
    val statsState by profileViewModel.statsState.collectAsState()
    val totalSongs by songViewModel.totalSongsCount.collectAsState()
    val likedSongs by songViewModel.likedSongsCount.collectAsState()
    val listenedSongs by songViewModel.listenedSongsCount.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val ctx = LocalContext.current

    // Fetch profile on first composition
    LaunchedEffect(Unit) {
        val token = TokenStorage.getAccessToken(ctx)
        if (token != null) {
            profileViewModel.fetchProfile(token)
        }
    }

    LaunchedEffect(uiState.profile) {
        uiState.profile?.let { profile ->
            profileViewModel.fetchProfileStats(profile.id)
        }
    }

    if (isLandscape) {
        Row(Modifier.fillMaxSize()) {
            // Sidebar Navigation
            BottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute,
                context = context
            )

            Divider(
                color = Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
            )

            // Main profile content
            ProfileContent(
                navController = navController,
                uiState = uiState,
                statsState = statsState,
                totalSongs = totalSongs,
                likedSongs = likedSongs,
                listenedSongs = listenedSongs
            )
        }
    }
    else {
        Scaffold(
            containerColor = Color.Black,
            bottomBar = {
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    context = context
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF003838), Color.Black)
                        )
                    )
                    .padding(top = 32.dp, bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Avatar
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(96.dp),
                        tint = Color.White
                    )
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier
                            .offset(x = 40.dp, y = (35).dp)
                            .size(20.dp)
                            .background(Color.White, shape = CircleShape)
                            .padding(4.dp),
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    uiState.error != null -> {
                        Text(
                            text = uiState.error ?: "Unknown error",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    else -> {
                        Text(
                            text = uiState.profile?.username ?: "Loading...",
                            color = Color.White,
                            fontSize = 20.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = uiState.profile?.location ?: "Loading...",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { navController.navigate("edit_profile") },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Edit Profile")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStat(totalSongs.toString(), "SONGS")
                    ProfileStat(likedSongs.toString(), "LIKED")
                    ProfileStat(listenedSongs.toString(), "LISTENED")
                }

                Spacer(modifier = Modifier.height(40.dp))

            if (statsState.monthlyCapsules.isEmpty()) {
                // No capsules to show
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No listening history yet",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Sound Capsule",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Download",
                            tint = Color.White
                        )
                    }
                }

                statsState.monthlyCapsules.forEachIndexed { index, capsule->
                    SoundCapsule(
                        monthYear = capsule.month,
                        minutesListened = capsule.totalTimeListened,
                        topArtist = capsule.topArtist,
                        topSong = capsule.topSong,
                        dayStreakSong = capsule.dayStreakSong,
                        onShare = { /* Implement sharing functionality */ },
                        monthIndex = index,
                        navController = navController
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

        }
    }
}
}
@Composable
private fun ProfileContent(
    navController: NavController,
    modifier: Modifier = Modifier,
    uiState: ProfileUiState,
    statsState: ProfileStatsUiState,
    totalSongs: Int,
    likedSongs: Int,
    listenedSongs: Int
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF003838), Color.Black)
                )
            )
            .padding(top = 32.dp, bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            // Avatar
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Profile",
                modifier = Modifier.size(96.dp),
                tint = Color.White
            )
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit",
                modifier = Modifier
                    .offset(x = 40.dp, y = (35).dp)
                    .size(20.dp)
                    .background(Color.White, shape = CircleShape)
                    .padding(4.dp),
                tint = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            uiState.error != null -> {
                Text(
                    text = uiState.error ?: "Unknown error",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            else -> {
                Text(
                    text = uiState.profile?.username ?: "Loading...",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = uiState.profile?.location ?: "Loading...",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { navController.navigate("edit_profile") },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Edit Profile")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStat(totalSongs.toString(), "SONGS")
            ProfileStat(likedSongs.toString(), "LIKED")
            ProfileStat(listenedSongs.toString(), "LISTENED")
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Your Sound Capsule",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Time Listened
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Time Listened",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatTime(statsState.monthlyCapsules[0].totalTimeListened),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Top Artist & Top Song
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Artist
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Top Artist",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = statsState.monthlyCapsules[0].topArtist?.artist ?: "-",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                // Top Song
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Top Song",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = statsState.monthlyCapsules[0].topSong?.title ?: "-",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Day Streak",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    val dayStreakSong = statsState.monthlyCapsules[0].dayStreakSong
                    Text(
                        text = "Day Streak: ${dayStreakSong?.dayStreak ?: 0} (${dayStreakSong?.title ?: "-"})",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileStat(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    ProfileScreen()
}

fun formatTime(seconds: Long): String {
    if (seconds <= 0) return "0 min"
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        hours > 0 -> "$hours hr ${minutes} min"
        else -> "$minutes min"
    }
}