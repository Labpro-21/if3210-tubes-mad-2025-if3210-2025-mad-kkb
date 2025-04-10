package com.kkb.purrytify

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApiService {
    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>
}

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val accessToken: String, val refreshToken: String)