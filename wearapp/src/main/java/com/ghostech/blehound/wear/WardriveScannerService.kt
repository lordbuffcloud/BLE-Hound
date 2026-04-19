package com.ghostech.blehound.wear

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class WardriveScannerService : Service() {

    private lateinit var fusedLocation: FusedLocationProviderClient
    @Volatile private var currentLocation: Location? = null
    private var bleScanner: android.bluetooth.le.BluetoothLeScanner? = null
    private val locationThread = HandlerThread("wardrive-location")

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                currentLocation = loc
                WearRepository.updateWardriveGpsAvailable(true)
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val loc = currentLocation ?: return
            val hit = WardriveHit(
                mac            = result.device.address,
                name           = result.device.name ?: "",
                rssi           = result.rssi,
                lat            = loc.latitude,
                lon            = loc.longitude,
                altMeters      = loc.altitude,
                accuracyMeters = loc.accuracy,
                timestampMs    = System.currentTimeMillis()
            )
            WardriveSyncManager.addHit(hit)
        }

        override fun onScanFailed(errorCode: Int) {
            Log.w(TAG, "BLE scan failed: $errorCode")
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationThread.start()
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification())
        startLocationUpdates()
        startBleScan()
        WardriveSyncManager.start(this)
        WearRepository.updateWardriveActive(true)
        Log.d(TAG, "Wardrive started")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBleScan()
        fusedLocation.removeLocationUpdates(locationCallback)
        locationThread.quitSafely()
        WardriveSyncManager.stop()
        WearRepository.updateWardriveActive(false)
        WearRepository.updateWardriveGpsAvailable(false)
        Log.d(TAG, "Wardrive stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startLocationUpdates() {
        val hasFine = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine) {
            Log.w(TAG, "Missing ACCESS_FINE_LOCATION — stopping")
            stopSelf()
            return
        }

        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3_000L)
            .setMinUpdateIntervalMillis(1_000L)
            .build()

        fusedLocation.requestLocationUpdates(req, locationCallback, locationThread.looper)
    }

    private fun startBleScan() {
        val bm = getSystemService(BluetoothManager::class.java)
        val adapter: BluetoothAdapter? = bm?.adapter
        if (adapter == null || !adapter.isEnabled) {
            Log.w(TAG, "Bluetooth unavailable")
            return
        }

        val hasScan = ContextCompat.checkSelfPermission(
            this, Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasScan) {
            Log.w(TAG, "Missing BLUETOOTH_SCAN — stopping")
            stopSelf()
            return
        }

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

        if (hasScan) {
            bleScanner?.stopScan(scanCallback)
        }
        bleScanner = null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Wardrive",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Active wardrive session" }

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("houndBEE Wardrive")
            .setContentText("Scanning BLE + GPS…")
            .setOngoing(true)
            .build()

    companion object {
        private const val TAG        = "WardriveScannerSvc"
        private const val NOTIF_ID   = 7421
        private const val CHANNEL_ID = "wardrive"
    }
}
