package com.kkb.purrytify.data.remote

import com.kkb.purrytify.ProfileResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


interface ApiService {
    @GET("/api/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): ProfileResponse
}

object RetrofitInstance {
    private const val BASE_URL = "http://34.101.226.132:3000"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}