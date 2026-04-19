package com.ghostech.blehound.wear

internal object WearConstants {
    const val PATH_SUMMARY        = "/ble-hound/summary"
    const val PATH_ALERT          = "/ble-hound/alert"
    const val PATH_WARDRIVE_BATCH = "/ble-hound/wardrive-batch"
    const val ALERT_THROTTLE_MS   = 15_000L
    const val SYNC_INTERVAL_MS    = 5_000L
    const val MAX_RETRIES         = 3
}
