package com.ridervoice.network
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
class NetworkObserver(private val context: Context) {
    fun isConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
