package com.kkb.purrytify


import android.util.Base64
import android.util.Log
import org.json.JSONObject

fun isTokenValid(token: String): Boolean {
    return try {
        val parts = token.split(".")
        //cetak parts di log cat
        Log.d("TokenStorage", "parts: $parts")
        if (parts.size != 3) return false

        val payloadJson = String(Base64.decode(parts[1], Base64.URL_SAFE))
        val payload = JSONObject(payloadJson)

        val exp = payload.getLong("exp")
        //cetak exp di log cat
        Log.d("TokenStorage", "exp: $exp")

        val now = System.currentTimeMillis() / 1000
        now < exp
    } catch (e: Exception) {
        false
    }
}
