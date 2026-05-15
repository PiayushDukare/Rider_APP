package com.ridervoice.recovery

import android.content.Context
import com.ridervoice.security.SecurePreferences

class SessionRestore(context: Context) {

    private val prefs = SecurePreferences(context)

    // FIX: was hardcoded to "ROOM_001" — now reads persisted value
    fun restoreRoom(): String? {
        return prefs.getLastRoomName()
    }

    fun saveRoom(roomName: String) {
        prefs.saveRoomName(roomName)
    }

    fun hasSession(): Boolean {
        return restoreRoom() != null
    }
}
