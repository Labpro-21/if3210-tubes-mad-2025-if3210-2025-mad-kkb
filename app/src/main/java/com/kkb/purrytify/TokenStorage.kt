package com.kkb.purrytify

import android.content.Context
import android.media.session.MediaSession.Token
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.kkb.purrytify.data.remote.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TokenStorage {

    private const val PREF_NAME = "secure_token"
    private const val ACCESS_TOKEN_KEY = "accessToken"
    private const val REFRESH_TOKEN_KEY = "refreshToken"
    private const val USER_ID = "user_id"

    private fun getPrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(context: Context, accessToken: String, refreshToken: String) {
        val sharedPrefs = getPrefs(context)
        sharedPrefs.edit()
            .putString(ACCESS_TOKEN_KEY, accessToken)
            .putString(REFRESH_TOKEN_KEY, refreshToken)
            .apply()
    }

    fun saveUserId(context: Context, user_id: String) {
        val sharedPrefs = getPrefs(context)
        sharedPrefs.edit()
            .putString(USER_ID, user_id)
            .apply()
    }
//    fun getToken(context: Context): String? {
//        val sharedPrefs = EncryptedSharedPreferences.create(
//            context,
//            "secure_prefs",
//            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
//            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
//            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
//        )
//        return sharedPrefs.getString("accessToken", null)
//    }

    fun clearToken(context: Context) {
        Log.d("TokenStorage", "clearToken called")
        Log.d("TokenStorage", "accessToken: ${getAccessToken(context)}")
        Log.d("TokenStorage", "refreshToken: ${getRefreshToken(context)}")
        getPrefs(context).edit().clear().apply()
        Log.d("TokenStorage", "Token cleared")
        Log.d("TokenStorage", "accessToken: ${getAccessToken(context)}")
        Log.d("TokenStorage", "refreshToken: ${getRefreshToken(context)}")
    }

    fun getAccessToken(context: Context): String? {
        return getPrefs(context).getString(ACCESS_TOKEN_KEY, null)
    }

    fun getRefreshToken(context: Context): String? {
        return getPrefs(context).getString(REFRESH_TOKEN_KEY, null)
    }

    fun getUserId(context: Context): String? {
        return getPrefs(context).getString(USER_ID, null)
    }

    suspend fun refreshAccessTokenIfNeeded(context: Context,apiService: ApiService): Boolean {
        val accessToken = getAccessToken(context)
        //cetak accessToken
        Log.d("TokenStorage", "accessToken: $accessToken")
        if (accessToken != null && isTokenValid(accessToken)) {
            Log.d("TokenStorage", "Token masih valid")
            return true // Masih valid, tidak perlu refresh
        }

        // token tidak valid atau null
        Log.d("TokenStorage", "Token tidak valid atau null")

//        val sharedPrefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
//      val refreshToken = sharedPrefs.getString("refreshToken", null)
        val refreshToken = getRefreshToken(context)

        //cetak refreshToken
        Log.d("TokenStorage", "refreshToken: $refreshToken")

        if (refreshToken == null) return false // Tidak bisa refresh

        return try {
            val response = apiService.refreshToken(ApiService.RefreshTokenRequest(refreshToken))

            if (response.isSuccessful && response.body() != null) {
                val newAccessToken = response.body()!!.accessToken
                val newRefreshToken = response.body()!!.refreshToken
                saveToken(context, newAccessToken, newRefreshToken)
                getUserId(context)?.let { saveUserId(context, it) }
                Log.d("TokenStorage", "Token berhasil di-refresh")
                true
            } else {
                Log.e("TokenStorage", "Refresh gagal: ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e("TokenStorage", "Exception saat refresh token", e)
            false
        }
    }
}