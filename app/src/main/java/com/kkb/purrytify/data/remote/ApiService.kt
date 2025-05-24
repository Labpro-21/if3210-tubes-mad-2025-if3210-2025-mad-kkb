package com.kkb.purrytify.data.remote

import com.kkb.purrytify.data.model.ChartResponse
import com.kkb.purrytify.data.model.ChartSong
import com.kkb.purrytify.data.model.LoginRequest
import com.kkb.purrytify.data.model.LoginResponse
import com.kkb.purrytify.data.model.ProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("/api/profile")
    suspend fun getProfile(@Header("Authorization") token: String): ProfileResponse
    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("api/refresh-token")
    suspend fun refreshToken(@Body body: RefreshTokenRequest): Response<LoginResponse>

    @GET("api/top-songs/global")
    suspend fun getTopGlobal(): Response<ChartResponse>

    @GET("api/song/{id}")
    suspend fun getSongById(@Path("id") songId: Int): Response<ChartSong>

    data class RefreshTokenRequest(val refreshToken: String)
}