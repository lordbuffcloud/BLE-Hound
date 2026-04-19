package com.ghostech.blehound.wear

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WearViewModel(app: Application) : AndroidViewModel(app) {

    private val appContext: Application = app

    // Phone-sync
    val summary: StateFlow<BleHoundSummary>          = WearRepository.summary
    val phoneConnected: StateFlow<Boolean>            = WearRepository.phoneConnected

    // Wardrive
    val wardriveActive: StateFlow<Boolean>            = WearRepository.wardriveActive
    val wardriveHits: StateFlow<Int>                  = WearRepository.wardriveHits
    val wardriveGpsAvailable: StateFlow<Boolean>      = WearRepository.wardriveGpsAvailable

    // Standalone scan
    val scanActive: StateFlow<Boolean>                = WearRepository.scanActive
    val nearbyDevices: StateFlow<List<WearBleDevice>> = WearRepository.nearbyDevices

    // Alert
    private val _activeAlert = MutableStateFlow<TrackerAlert?>(null)
    val activeAlert: StateFlow<TrackerAlert?> = _activeAlert.asStateFlow()

    // Selected device for detail view
    private val _selectedDevice = MutableStateFlow<WearBleDevice?>(null)
    val selectedDevice: StateFlow<WearBleDevice?> = _selectedDevice.asStateFlow()

    private val lastAlertMs = mutableMapOf<String, Long>()

    init {
        viewModelScope.launch {
            WearRepository.alerts.collect { alert ->
                val now = System.currentTimeMillis()
                val last = lastAlertMs[alert.address] ?: 0L
                if (now - last >= WearConstants.ALERT_THROTTLE_MS) {
                    lastAlertMs[alert.address] = now
                    pruneAlertMap(now)
                    _activeAlert.value = alert
                    vibrateAlert()
                }
            }
        }
    }

    // --- Alert ---
    fun dismissAlert() { _activeAlert.value = null }

    // --- Permissions ---
    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun hasBlePermission(): Boolean =
        ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED

    // --- Standalone scan ---
    fun startScan() {
        if (!hasBlePermission()) return
        appContext.startForegroundService(
            Intent(appContext, WearScanService::class.java)
        )
    }

    fun stopScan() {
        appContext.stopService(Intent(appContext, WearScanService::class.java))
    }

    // --- Device selection ---
    fun selectDevice(device: WearBleDevice) { _selectedDevice.value = device }
    fun clearSelection() { _selectedDevice.value = null }

    // --- Wardrive ---
    fun startWardrive() {
        if (!hasLocationPermission()) return
        appContext.startForegroundService(
            Intent(appContext, WardriveScannerService::class.java)
        )
    }

    fun stopWardrive() {
        appContext.stopService(Intent(appContext, WardriveScannerService::class.java))
    }

    // --- Internals ---
    private fun pruneAlertMap(nowMs: Long) {
        if (lastAlertMs.size <= MAX_ALERT_ENTRIES) return
        val cutoff = nowMs - WearConstants.ALERT_THROTTLE_MS * 4
        lastAlertMs.entries.removeIf { it.value < cutoff }
    }

    private fun vibrateAlert() {
        try {
            val effect = VibrationEffect.createWaveform(VIBRATION_PATTERN, -1)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                appContext.getSystemService(VibratorManager::class.java)
                    ?.defaultVibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Vibrator::class.java)?.vibrate(effect)
            }
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Vibration failed: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "WearViewModel"
        private val VIBRATION_PATTERN = longArrayOf(0, 150, 100, 150)
        private const val MAX_ALERT_ENTRIES = 512
    }
}
