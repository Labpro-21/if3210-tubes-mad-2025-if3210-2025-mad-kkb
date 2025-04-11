package com.kkb.purrytify


import android.util.Base64
import org.json.JSONObject

fun isTokenValid(token: String): Boolean {
    return try {
        val parts = token.split(".")
        if (parts.size != 3) return false

        val payloadJson = String(Base64.decode(parts[1], Base64.URL_SAFE))
        val payload = JSONObject(payloadJson)
        val exp = payload.getLong("exp")

        val now = System.currentTimeMillis() / 1000
        now < exp
    } catch (e: Exception) {
        false
    }
}
