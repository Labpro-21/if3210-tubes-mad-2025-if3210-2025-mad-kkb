package com.kkb.purrytify.data.remote

import com.kkb.purrytify.data.model.ChartResponse
import com.kkb.purrytify.data.model.ChartSong
import com.kkb.purrytify.data.model.LoginRequest
import com.kkb.purrytify.data.model.LoginResponse
import com.kkb.purrytify.data.model.ProfileResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

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

    @GET("/api/top-songs/ID")
    suspend fun getTopID(): Response<ChartResponse>

    @GET("/api/top-songs/MY")
    suspend fun getTopMY(): Response<ChartResponse>

    @GET("/api/top-songs/US")
    suspend fun getTopUS(): Response<ChartResponse>

    @GET("/api/top-songs/GB")
    suspend fun getTopUK(): Response<ChartResponse>

    @GET("/api/top-songs/CH")
    suspend fun getTopCH(): Response<ChartResponse>

    @GET("/api/top-songs/DE")
    suspend fun getTopDE(): Response<ChartResponse>

    @GET("/api/top-songs/BR")
    suspend fun getTopBR(): Response<ChartResponse>

    @Multipart
    @PATCH("api/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Part parts: List<MultipartBody.Part>
    ): ProfileResponse

    data class RefreshTokenRequest(val refreshToken: String)
}