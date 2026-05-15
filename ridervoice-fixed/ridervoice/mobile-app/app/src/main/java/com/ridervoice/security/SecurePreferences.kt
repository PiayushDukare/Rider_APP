package com.ridervoice.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePreferences(context: Context) {

    // FIX: use EncryptedSharedPreferences — was plain SharedPreferences before
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        prefs.edit().putString("token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("token", null)
    }

    fun saveRoomName(roomName: String) {
        prefs.edit().putString("last_room", roomName).apply()
    }

    fun getLastRoomName(): String? {
        return prefs.getString("last_room", null)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
