package com.ghostech.blehound.wear

data class WearBleDevice(
    val mac: String,
    val name: String,
    val rssi: Int,
    val deviceClass: String = "",
    val firstSeenMs: Long = System.currentTimeMillis(),
    val lastSeenMs: Long = System.currentTimeMillis()
) {
    val rssiLabel: String get() = when {
        rssi >= -60 -> "Excellent"
        rssi >= -75 -> "Good"
        rssi >= -90 -> "Fair"
        else        -> "Weak"
    }

    val rssiDot: String get() = when {
        rssi >= -60 -> "\u25CF"  // filled circle — strong
        rssi >= -75 -> "\u25CF"
        rssi >= -90 -> "\u25D4"  // circle with lower-right quarter black — medium
        else        -> "\u25CB"  // empty circle — weak
    }

    val displayName: String get() = name.ifBlank { mac }
}
