package com.ghostech.blehound.wear

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class WardriveHit(
    val mac: String,
    val name: String,
    val rssi: Int,
    val lat: Double,
    val lon: Double,
    val altMeters: Double,
    val accuracyMeters: Float,
    val timestampMs: Long,
    val deviceClass: String = ""
)

internal object WardriveCodec {

    private val dateFmt: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC)

    fun encodeHits(hits: List<WardriveHit>): ByteArray {
        val arr = JSONArray()
        hits.forEach { h ->
            arr.put(JSONObject().apply {
                put("mac",      h.mac)
                put("name",     h.name)
                put("rssi",     h.rssi.coerceIn(-120, 0))
                put("lat",      h.lat)
                put("lon",      h.lon)
                put("alt",      h.altMeters)
                put("acc",      h.accuracyMeters.toDouble())
                put("ts",       h.timestampMs)
                put("cls",      h.deviceClass)
            })
        }
        return arr.toString().toByteArray(Charsets.UTF_8)
    }

    fun decodeHits(bytes: ByteArray): List<WardriveHit> {
        if (bytes.isEmpty()) return emptyList()
        return try {
            val arr = JSONArray(String(bytes, Charsets.UTF_8))
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                WardriveHit(
                    mac           = o.optString("mac",  ""),
                    name          = o.optString("name", ""),
                    rssi          = o.optInt("rssi",   -99).coerceIn(-120, 0),
                    lat           = o.optDouble("lat",   0.0),
                    lon           = o.optDouble("lon",   0.0),
                    altMeters     = o.optDouble("alt",   0.0),
                    accuracyMeters = o.optDouble("acc",  0.0).toFloat(),
                    timestampMs   = o.optLong("ts",     0L),
                    deviceClass   = o.optString("cls",  "")
                )
            }.filter { it.mac.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun toWigleCsvRows(hits: List<WardriveHit>): String = buildString {
        hits.forEach { h ->
            val time = dateFmt.format(Instant.ofEpochMilli(h.timestampMs))
            append(h.mac).append(',')
            append(h.name.replace(',', '_')).append(',')
            append("").append(',')                      // AuthMode — not applicable for BLE
            append(time).append(',')
            append(-1).append(',')                      // Channel — not applicable for BLE
            append(h.rssi).append(',')
            append(h.lat).append(',')
            append(h.lon).append(',')
            append(h.altMeters).append(',')
            append(h.accuracyMeters).append(',')
            append(if (h.deviceClass.isNotBlank()) h.deviceClass else "BLE")
            append('\n')
        }
    }
}
