package com.kkb.purrytify

import android.content.Context
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.kkb.purrytify.data.remote.RetrofitInstance
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@Composable
fun ProfileScreen(    navController: NavController = rememberNavController(), // aman untuk preview
                      currentRoute: String = "profile",                         // default route
                      context: Context = LocalContext.current
) {
    var profile by remember { mutableStateOf<ProfileResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // ✅ LaunchedEffect = coroutine-safe zone untuk suspend function, semacam async kayaknya
    LaunchedEffect(Unit) {
        val token = TokenStorage.getAccessToken(context)
        if (token != null) {
            try {
                val result = RetrofitInstance.api.getProfile("Bearer $token")
                profile = result
                Log.d("ProfileScreen", "Profile: $profile")
            } catch (e: Exception) {
                error = "Gagal mengambil data profil"
                Log.e("ProfileScreen", "Error: ${e.message}")
            }
        } else {
            error = "Token tidak tersedia"
        }
    }



    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
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
            ){
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

            Text(
                text = profile?.username ?: "Loading...",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                text = "Indonesia",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* Edit profile */ },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Edit Profile")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat("135", "SONGS")
                ProfileStat("32", "LIKED")
                ProfileStat("50", "LISTENED")
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Placeholder content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFD700),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("B", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
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
