package com.kkb.purrytify

import android.content.Context
import android.media.session.MediaSession.Token
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

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
}