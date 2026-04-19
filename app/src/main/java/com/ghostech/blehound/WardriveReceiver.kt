package com.ghostech.blehound

import android.os.Environment
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import org.json.JSONArray
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WardriveReceiver : WearableListenerService() {

    override fun onMessageReceived(event: MessageEvent) {
        if (event.path != PATH_WARDRIVE_BATCH) return
        val hits = decodeBatch(event.data)
        if (hits.isEmpty()) return
        appendWigleCsv(hits)
        Log.i(TAG, "Appended ${hits.size} wardrive hits to CSV")
    }

    private fun appendWigleCsv(hits: List<HitRecord>) {
        val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: filesDir
        dir.mkdirs()
        val file = File(dir, CSV_FILENAME)
        val isNew = !file.exists()
        file.appendText(buildString {
            if (isNew) appendLine(WIGLE_HEADER)
            hits.forEach { h ->
                val time = DATE_FMT.format(Date(h.timestampMs))
                append(h.mac).append(',')
                append(h.name.replace(',', '_')).append(',')
                append("").append(',')
                append(time).append(',')
                append(-1).append(',')
                append(h.rssi).append(',')
                append(h.lat).append(',')
                append(h.lon).append(',')
                append(h.altMeters).append(',')
                append(h.accuracyMeters).append(',')
                appendLine(if (h.deviceClass.isNotBlank()) h.deviceClass else "BLE")
            }
        })
        Log.d(TAG, "CSV path: ${file.absolutePath}")
    }

    private data class HitRecord(
        val mac: String,
        val name: String,
        val rssi: Int,
        val lat: Double,
        val lon: Double,
        val altMeters: Double,
        val accuracyMeters: Float,
        val timestampMs: Long,
        val deviceClass: String
    )

    private fun decodeBatch(bytes: ByteArray): List<HitRecord> {
        if (bytes.isEmpty()) return emptyList()
        return try {
            val arr = JSONArray(String(bytes, Charsets.UTF_8))
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.getJSONObject(i)
                val mac = o.optString("mac", "")
                if (mac.isBlank()) return@mapNotNull null
                HitRecord(
                    mac           = mac,
                    name          = o.optString("name", ""),
                    rssi          = o.optInt("rssi", -99).coerceIn(-120, 0),
                    lat           = o.optDouble("lat", 0.0),
                    lon           = o.optDouble("lon", 0.0),
                    altMeters     = o.optDouble("alt", 0.0),
                    accuracyMeters = o.optDouble("acc", 0.0).toFloat(),
                    timestampMs   = o.optLong("ts", 0L),
                    deviceClass   = o.optString("cls", "")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Batch decode failed: ${e.message}")
            emptyList()
        }
    }

    companion object {
        private const val TAG                = "WardriveReceiver"
        private const val PATH_WARDRIVE_BATCH = "/ble-hound/wardrive-batch"
        private const val CSV_FILENAME       = "blehound-wardrive.csv"
        private const val WIGLE_HEADER       =
            "MAC,SSID,AuthMode,FirstSeen,Channel,RSSI,CurrentLatitude,CurrentLongitude,AltitudeMeters,AccuracyMeters,Type"
        private val DATE_FMT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }
}
