package com.ridervoice.utils
import android.util.Log
object Logger {
    private const val TAG = "RiderVoice"
    fun debug(msg: String) = Log.d(TAG, msg)
    fun error(msg: String) = Log.e(TAG, msg)
}
