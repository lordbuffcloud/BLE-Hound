package com.ghostech.blehound.wear

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WearViewModel(app: Application) : AndroidViewModel(app) {

    private val appContext: Application = app

    val summary: StateFlow<BleHoundSummary> = WearRepository.summary
    val phoneConnected: StateFlow<Boolean> = WearRepository.phoneConnected

    private val _activeAlert = MutableStateFlow<TrackerAlert?>(null)
    val activeAlert: StateFlow<TrackerAlert?> = _activeAlert.asStateFlow()

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

    private fun pruneAlertMap(nowMs: Long) {
        if (lastAlertMs.size <= MAX_ALERT_ENTRIES) return
        val cutoff = nowMs - WearConstants.ALERT_THROTTLE_MS * 4
        lastAlertMs.entries.removeIf { it.value < cutoff }
    }

    fun dismissAlert() {
        _activeAlert.value = null
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
            // Haptic is non-critical; swallow silently
        }
    }

    companion object {
        private val VIBRATION_PATTERN = longArrayOf(0, 150, 100, 150)
        private const val MAX_ALERT_ENTRIES = 512
    }
}
