package com.ridervoice.utils

import android.content.Context
import android.os.Build

object OEMBatteryWarning {
    
    // ROMs notorious for aggressively killing foreground services
    private val AGGRESSIVE_OEMS = listOf(
        "xiaomi", "redmi", "poco",
        "oppo", "vivo", "realme",
        "huawei", "honor",
        "samsung"
    )

    fun isAggressiveOEM(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()
        return AGGRESSIVE_OEMS.any { manufacturer.contains(it) || brand.contains(it) }
    }

    // In a real app, this would open the specific Settings intent for the manufacturer
    // (e.g. Xiaomi's autostart and battery saver restrictions).
    fun getBatteryOptimizationWarningText(): String {
        return "Your device (${Build.MANUFACTURER}) is known to aggressively kill background apps. " +
               "If you experience random convoy disconnects while your phone is locked, please disable Battery Restrictions for this app in your device Settings."
    }
}
