package com.ghostech.blehound.wear

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class WearScanService : Service() {

    private var bleScanner: android.bluetooth.le.BluetoothLeScanner? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = WearBleDevice(
                mac         = result.device.address,
                name        = result.device.name ?: "",
                rssi        = result.rssi.coerceIn(-120, 0),
                deviceClass = inferClass(result),
                firstSeenMs = WearRepository.deviceFirstSeen(result.device.address),
                lastSeenMs  = System.currentTimeMillis()
            )
            WearRepository.upsertDevice(device)
        }

        override fun onScanFailed(errorCode: Int) {
            Log.w(TAG, "BLE scan failed: $errorCode")
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification())
        startBleScan()
        WearRepository.updateScanActive(true)
        Log.d(TAG, "Standalone scan started")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBleScan()
        WearRepository.updateScanActive(false)
        Log.d(TAG, "Standalone scan stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startBleScan() {
        val hasScan = ContextCompat.checkSelfPermission(
            this, Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasScan) { Log.w(TAG, "Missing BLUETOOTH_SCAN"); stopSelf(); return }

        val adapter = getSystemService(BluetoothManager::class.java)?.adapter
        if (adapter == null || !adapter.isEnabled) { Log.w(TAG, "BT unavailable"); stopSelf(); return }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bleScanner = adapter.bluetoothLeScanner
        bleScanner?.startScan(null, settings, scanCallback)
        Log.d(TAG, "BLE scan started")
    }

    private fun stopBleScan() {
        val hasScan = ContextCompat.checkSelfPermission(
            this, Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
        if (hasScan) bleScanner?.stopScan(scanCallback)
        bleScanner = null
    }

    private fun inferClass(result: ScanResult): String {
        val name = result.device.name?.uppercase() ?: return ""
        return when {
            "FLIPPER"   in name                          -> "Flipper"
            "DJI"       in name || "DRONE" in name       -> "Drone"
            "PARROT"    in name || "SKYDIO" in name      -> "Drone"
            "AIRTAG"    in name || "FIND MY" in name     -> "Tracker"
            "TILE"      in name                          -> "Tracker"
            "AXON"      in name || "TASER" in name       -> "Fed"
            "PWNAGOTCHI" in name                         -> "Gadget"
            "ESP32"     in name || "ARDUINO" in name     -> "Dev Board"
            else                                         -> ""
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "houndBEE Scan", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Active BLE scan session" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setContentTitle("houndBEE")
            .setContentText("Scanning nearby BLE devices\u2026")
            .setOngoing(true)
            .build()

    companion object {
        private const val TAG        = "WearScanService"
        private const val NOTIF_ID   = 7422
        private const val CHANNEL_ID = "houndbee_scan"
    }
}
