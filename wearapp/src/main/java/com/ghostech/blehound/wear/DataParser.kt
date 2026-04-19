package com.ghostech.blehound.wear

import org.json.JSONException
import org.json.JSONObject

data class BleHoundSummary(
    val total: Int,
    val trackers: Int,
    val drones: Int,
    val gadgets: Int,
    val feds: Int,
    val isScanning: Boolean,
    val receivedAtMs: Long
) {
    companion object {
        val EMPTY = BleHoundSummary(0, 0, 0, 0, 0, false, 0L)
    }
}

data class TrackerAlert(
    val deviceClass: String,
    val address: String,
    val rssi: Int,
    val receivedAtMs: Long
)

sealed class ParseResult<out T> {
    data class Success<T>(val value: T) : ParseResult<T>()
    data class Failure(val reason: String) : ParseResult<Nothing>()
}

internal object DataParser {

    private inline fun <T> parseBytes(
        bytes: ByteArray,
        block: (JSONObject) -> ParseResult<T>
    ): ParseResult<T> {
        if (bytes.isEmpty()) return ParseResult.Failure("Empty payload")
        return try {
            block(JSONObject(String(bytes, Charsets.UTF_8)))
        } catch (e: JSONException) {
            ParseResult.Failure("JSON parse error: ${e.message}")
        }
    }

    fun parseSummary(bytes: ByteArray): ParseResult<BleHoundSummary> = parseBytes(bytes) { json ->
        ParseResult.Success(
            BleHoundSummary(
                total = json.optInt("total", 0).coerceAtLeast(0),
                trackers = json.optInt("trackers", 0).coerceAtLeast(0),
                drones = json.optInt("drones", 0).coerceAtLeast(0),
                gadgets = json.optInt("gadgets", 0).coerceAtLeast(0),
                feds = json.optInt("feds", 0).coerceAtLeast(0),
                isScanning = json.optBoolean("scanning", false),
                receivedAtMs = System.currentTimeMillis()
            )
        )
    }

    fun parseAlert(bytes: ByteArray): ParseResult<TrackerAlert> = parseBytes(bytes) { json ->
        val deviceClass = json.optString("class", "").trim()
        if (deviceClass.isBlank()) return@parseBytes ParseResult.Failure("Missing device class")
        val address = json.optString("address", "").trim()
        if (address.isBlank()) return@parseBytes ParseResult.Failure("Missing address")
        ParseResult.Success(
            TrackerAlert(
                deviceClass = deviceClass,
                address = address,
                rssi = json.optInt("rssi", -99).coerceIn(-120, 0),
                receivedAtMs = System.currentTimeMillis()
            )
        )
    }

    fun encodeSummary(
        total: Int,
        trackers: Int,
        drones: Int,
        gadgets: Int,
        feds: Int,
        scanning: Boolean
    ): ByteArray {
        val json = JSONObject().apply {
            put("total", total.coerceAtLeast(0))
            put("trackers", trackers.coerceAtLeast(0))
            put("drones", drones.coerceAtLeast(0))
            put("gadgets", gadgets.coerceAtLeast(0))
            put("feds", feds.coerceAtLeast(0))
            put("scanning", scanning)
        }
        return json.toString().toByteArray(Charsets.UTF_8)
    }

    fun encodeAlert(
        deviceClass: String,
        address: String,
        rssi: Int
    ): ByteArray {
        val json = JSONObject().apply {
            put("class", deviceClass)
            put("address", address)
            put("rssi", rssi.coerceIn(-120, 0))
        }
        return json.toString().toByteArray(Charsets.UTF_8)
    }
}
