package com.kkb.purrytify

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Preview
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit = {}) {
    val overlayColor = colorResource(id = R.color.background_overlay)
    val fieldBgColor = colorResource(id = R.color.text_field_background)
    val loginButtonColor = colorResource(id = R.color.spotify_green)
    val white = colorResource(id = R.color.purritify_white)
    val grey = colorResource(id = R.color.text_grey)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val baseUrl = context.getString(R.string.base_url)

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background overlay
            Image(
                painter = painterResource(id = R.drawable.login_bg),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(overlayColor)
            )

            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_music_logo),
                    contentDescription = "Logo Purrytify",
                    modifier = Modifier.size(75.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Millions of Songs.\nOnly on Purritify.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var passwordVisible by remember { mutableStateOf(false) }

                val textFieldColors = TextFieldDefaults.colors(
                    focusedTextColor = white,
                    unfocusedTextColor = white,
                    disabledTextColor = grey,
                    focusedContainerColor = fieldBgColor,
                    unfocusedContainerColor = fieldBgColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLabelColor = grey,
                    unfocusedLabelColor = grey,
                    cursorColor = white
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    placeholder = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = null, tint = Color.Gray)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val retrofit = Retrofit.Builder()
                                    .baseUrl(baseUrl)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build()

                                val service = retrofit.create(LoginApiService::class.java)
                                val response = service.login(LoginRequest(email, password))

                                if (response.isSuccessful && response.body() != null) {
                                    val accessToken = response.body()!!.accessToken
                                    val refreshToken = response.body()!!.refreshToken
                                    TokenStorage.saveToken(context, accessToken, refreshToken)
                                    snackbarHostState.showSnackbar("Login berhasil 🎉")
                                    onLoginSuccess()
                                } else {
                                    snackbarHostState.showSnackbar("Login gagal. Email atau password salah.")
                                }
                            } catch (e: Exception) {
                                val errorMessage = e.message ?: "Terjadi kesalahan tidak diketahui"
                                snackbarHostState.showSnackbar("Error: $errorMessage")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = loginButtonColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Log In", color = Color.White)
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
