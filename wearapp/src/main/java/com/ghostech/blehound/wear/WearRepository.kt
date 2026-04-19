package com.ghostech.blehound.wear

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal object WearRepository {

    private const val MAX_DEVICES = 200

    // --- Phone-sync state ---
    private val _summary = MutableStateFlow(BleHoundSummary.EMPTY)
    val summary: StateFlow<BleHoundSummary> = _summary.asStateFlow()

    private val _alerts = MutableSharedFlow<TrackerAlert>(replay = 1, extraBufferCapacity = 8)
    val alerts: SharedFlow<TrackerAlert> = _alerts.asSharedFlow()

    private val _phoneConnected = MutableStateFlow(false)
    val phoneConnected: StateFlow<Boolean> = _phoneConnected.asStateFlow()

    // --- Wardrive state ---
    private val _wardriveActive = MutableStateFlow(false)
    val wardriveActive: StateFlow<Boolean> = _wardriveActive.asStateFlow()

    private val _wardriveHits = MutableStateFlow(0)
    val wardriveHits: StateFlow<Int> = _wardriveHits.asStateFlow()

    private val _wardriveGpsAvailable = MutableStateFlow(false)
    val wardriveGpsAvailable: StateFlow<Boolean> = _wardriveGpsAvailable.asStateFlow()

    // --- Standalone scan state ---
    private val _scanActive = MutableStateFlow(false)
    val scanActive: StateFlow<Boolean> = _scanActive.asStateFlow()

    private val _nearbyDevices = MutableStateFlow<List<WearBleDevice>>(emptyList())
    val nearbyDevices: StateFlow<List<WearBleDevice>> = _nearbyDevices.asStateFlow()

    // --- Phone-sync writers ---
    fun updateSummary(value: BleHoundSummary) { _summary.value = value }
    suspend fun emitAlert(alert: TrackerAlert) { _alerts.emit(alert) }
    fun updatePhoneConnected(connected: Boolean) { _phoneConnected.value = connected }

    // --- Wardrive writers ---
    fun updateWardriveActive(active: Boolean) {
        _wardriveActive.value = active
        if (!active) {
            _wardriveHits.value = 0
            _wardriveGpsAvailable.value = false
        }
    }
    fun incrementWardriveHits(total: Int) { _wardriveHits.value = total }
    fun updateWardriveGpsAvailable(available: Boolean) { _wardriveGpsAvailable.value = available }

    // --- Standalone scan writers ---
    fun updateScanActive(active: Boolean) { _scanActive.value = active }

    fun upsertDevice(device: WearBleDevice) {
        _nearbyDevices.update { current ->
            val list = current.toMutableList()
            val idx = list.indexOfFirst { it.mac == device.mac }
            if (idx >= 0) {
                list[idx] = device
            } else {
                if (list.size >= MAX_DEVICES) {
                    val evict = list.minByOrNull { it.lastSeenMs }
                    list.remove(evict)
                }
                list.add(device)
            }
            list.sortedByDescending { it.rssi }
        }
    }

    fun deviceFirstSeen(mac: String): Long =
        _nearbyDevices.value.firstOrNull { it.mac == mac }?.firstSeenMs
            ?: System.currentTimeMillis()

    fun clearDevices() {
        _nearbyDevices.value = emptyList()
        _scanActive.value = false
    }

    fun resetForTest() {
        _summary.value = BleHoundSummary.EMPTY
        _phoneConnected.value = false
        _wardriveActive.value = false
        _wardriveHits.value = 0
        _wardriveGpsAvailable.value = false
        _scanActive.value = false
        _nearbyDevices.value = emptyList()
        _alerts.resetReplayCache()
    }
}
