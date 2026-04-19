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
        rssi >= -60 -> "\u25CF"  // filled circle — excellent
        rssi >= -75 -> "\u25D4"  // 3/4 filled circle — good
        rssi >= -90 -> "\u25D1"  // half-filled circle — fair
        else        -> "\u25CB"  // empty circle — weak
    }

    val displayName: String get() = name.ifBlank { mac }
}
