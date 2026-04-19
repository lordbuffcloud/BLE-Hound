package com.ghostech.blehound.wear

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

internal object WearRepository {

    private val _summary = MutableStateFlow(BleHoundSummary.EMPTY)
    val summary: StateFlow<BleHoundSummary> = _summary.asStateFlow()

    private val _alerts = MutableSharedFlow<TrackerAlert>(replay = 1, extraBufferCapacity = 8)
    val alerts: SharedFlow<TrackerAlert> = _alerts.asSharedFlow()

    private val _phoneConnected = MutableStateFlow(false)
    val phoneConnected: StateFlow<Boolean> = _phoneConnected.asStateFlow()

    private val _wardriveActive = MutableStateFlow(false)
    val wardriveActive: StateFlow<Boolean> = _wardriveActive.asStateFlow()

    private val _wardriveHits = MutableStateFlow(0)
    val wardriveHits: StateFlow<Int> = _wardriveHits.asStateFlow()

    private val _wardriveGpsAvailable = MutableStateFlow(false)
    val wardriveGpsAvailable: StateFlow<Boolean> = _wardriveGpsAvailable.asStateFlow()

    fun updateSummary(value: BleHoundSummary) { _summary.value = value }

    suspend fun emitAlert(alert: TrackerAlert) { _alerts.emit(alert) }

    fun updatePhoneConnected(connected: Boolean) { _phoneConnected.value = connected }

    fun updateWardriveActive(active: Boolean) {
        _wardriveActive.value = active
        if (!active) {
            _wardriveHits.value = 0
            _wardriveGpsAvailable.value = false
        }
    }

    fun incrementWardriveHits(total: Int) { _wardriveHits.value = total }

    fun updateWardriveGpsAvailable(available: Boolean) { _wardriveGpsAvailable.value = available }

    fun resetForTest() {
        _summary.value = BleHoundSummary.EMPTY
        _phoneConnected.value = false
        _wardriveActive.value = false
        _wardriveHits.value = 0
        _wardriveGpsAvailable.value = false
        _alerts.resetReplayCache()
    }
}
