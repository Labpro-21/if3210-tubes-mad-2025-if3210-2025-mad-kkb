package com.kkb.purrytify

import android.content.Context
import android.media.session.MediaSession.Token
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TokenStorage {
    fun saveToken(context: Context, accessToken: String, refreshToken: String) {
        val sharedPrefs = EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPrefs.edit().putString("accessToken", accessToken).apply()
        sharedPrefs.edit().putString("refreshToken", refreshToken).apply()
    }

    fun getToken(context: Context): String? {
        val sharedPrefs = EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPrefs.getString("accessToken", null)
    }

    fun clearToken(context: Context) {
        val sharedPrefs = EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPrefs.edit().remove("accessToken").apply()
        sharedPrefs.edit().remove("refreshToken").apply()
    }

    suspend fun refreshAccessTokenIfNeeded(context: Context): Boolean {
        val accessToken = TokenStorage.getToken(context)
        if (accessToken != null && isTokenValid(accessToken)) {
            return true // Masih valid, tidak perlu refresh
        }

        val sharedPrefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val refreshToken = sharedPrefs.getString("refreshToken", null)

        if (refreshToken == null) return false // Tidak bisa refresh

        return try {
            val retrofit = Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url)) // ← gunakan context untuk ambil baseUrl
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(LoginApiService::class.java)
            val response = service.refreshToken(LoginApiService.RefreshTokenRequest(refreshToken))

            if (response.isSuccessful && response.body() != null) {
                val newAccessToken = response.body()!!.accessToken
                val newRefreshToken = response.body()!!.refreshToken
                TokenStorage.saveToken(context, newAccessToken, newRefreshToken)
                true
            } else {
                false // Refresh gagal
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}