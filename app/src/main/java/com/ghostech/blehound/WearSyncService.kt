package com.ghostech.blehound

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

object WearSyncService {

    private const val TAG = "WearSyncService"
    private const val PATH_SUMMARY = "/ble-hound/summary"
    private const val PATH_ALERT = "/ble-hound/alert"
    private const val MAX_RETRIES = 3
    private const val MAX_ALERT_ENTRIES = 512
    private const val SYNC_INTERVAL_MS = 5_000L
    private const val ALERT_THROTTLE_MS = 15_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lastPushMs = AtomicLong(0L)
    private val lastAlertMs = ConcurrentHashMap<String, Long>()

    private val TRACKER_CLASSES = setOf("AirTag", "Tile", "Galaxy Tag", "Find My")
    private val DRONE_CLASSES = setOf(
        "DJI Drone", "Parrot Drone", "Skydio Drone", "Autel Drone",
        "Anafi Drone", "Remote ID Drone", "Drone"
    )
    private val FED_CLASSES = setOf("Axon Device", "Axon Cam", "Axon Taser", "Flock")

    fun maybePushSummary(context: Context) {
        val now = System.currentTimeMillis()
        // getAndUpdate is a single atomic CAS-based operation — no TOCTOU between check and set.
        val prev = lastPushMs.getAndUpdate { stored ->
            if (now - stored >= SYNC_INTERVAL_MS) now else stored
        }
        if (now - prev < SYNC_INTERVAL_MS) return
        scope.launch { pushSummaryInternal(context) }
    }

    fun pushTrackerAlert(context: Context, deviceClass: String, address: String, rssi: Int) {
        val now = System.currentTimeMillis()
        // merge is atomic on ConcurrentHashMap — returns the value that was (or remains) stored.
        val stored = lastAlertMs.merge(address, now) { existing, new ->
            if (now - existing < ALERT_THROTTLE_MS) existing else new
        }!!
        if (stored != now) return
        scope.launch { pushAlertInternal(context, deviceClass, address, rssi) }
        pruneAlertMap()
    }

    private fun pruneAlertMap() {
        if (lastAlertMs.size <= MAX_ALERT_ENTRIES) return
        val cutoff = System.currentTimeMillis() - ALERT_THROTTLE_MS * 4
        lastAlertMs.entries.removeIf { it.value < cutoff }
    }

    internal data class Counts(
        val total: Int,
        val trackers: Int,
        val drones: Int,
        val gadgets: Int,
        val feds: Int
    )

    internal fun buildCounts(): Counts {
        val devices = ArrayList(BleStore.devices.values)
        val latched = HashMap(BleStore.latchedTrackerClassByAddress)

        var trackers = 0
        var drones = 0
        var feds = 0
        var gadgets = 0

        for (device in devices) {
            val cls = latched[device.address] ?: ""
            when {
                cls in TRACKER_CLASSES -> trackers++
                cls in DRONE_CLASSES -> drones++
                cls in FED_CLASSES -> feds++
                cls.isNotBlank() && cls != "Unknown" -> gadgets++
            }
        }

        return Counts(
            total = devices.size,
            trackers = trackers,
            drones = drones,
            gadgets = gadgets,
            feds = feds
        )
    }

    private suspend fun pushSummaryInternal(context: Context) {
        val c = buildCounts()
        val payload = encodeJson {
            put("total", c.total)
            put("trackers", c.trackers)
            put("drones", c.drones)
            put("gadgets", c.gadgets)
            put("feds", c.feds)
            put("scanning", BleStore.shouldScan)
        } ?: return

        val req = PutDataMapRequest.create(PATH_SUMMARY).also { putReq ->
            putReq.dataMap.putByteArray("data", payload)
            putReq.dataMap.putLong("ts", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()

        retryTask(MAX_RETRIES) { Wearable.getDataClient(context).putDataItem(req) }
    }

    private suspend fun pushAlertInternal(
        context: Context,
        deviceClass: String,
        address: String,
        rssi: Int
    ) {
        val payload = encodeJson {
            put("class", deviceClass)
            put("address", address)
            put("rssi", rssi.coerceIn(-120, 0))
        } ?: return

        val nodes = try {
            Wearable.getNodeClient(context).connectedNodes.await()
        } catch (e: Exception) {
            Log.w(TAG, "getConnectedNodes failed: ${e.message}")
            return
        }

        if (nodes.isEmpty()) {
            Log.d(TAG, "No connected nodes, skipping alert push")
            return
        }

        for (node in nodes) {
            retryTask(MAX_RETRIES) {
                Wearable.getMessageClient(context).sendMessage(node.id, PATH_ALERT, payload)
            }
        }
    }

    private suspend fun <T> retryTask(maxAttempts: Int, block: () -> Task<T>): T? {
        for (attempt in 1..maxAttempts) {
            try {
                return block().await()
            } catch (e: Exception) {
                Log.w(TAG, "Attempt $attempt/$maxAttempts failed: ${e.message}")
            }
        }
        Log.e(TAG, "All $maxAttempts attempts failed")
        return null
    }

    private inline fun encodeJson(block: JSONObject.() -> Unit): ByteArray? {
        return try {
            JSONObject().apply(block).toString().toByteArray(Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "encodeJson failed: ${e.message}")
            null
        }
    }
}
