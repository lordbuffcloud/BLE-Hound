package com.ghostech.blehound.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

internal object WardriveSyncManager {

    private const val TAG = "WardriveSyncMgr"
    private const val BATCH_INTERVAL_MS = 30_000L
    private const val MAX_QUEUE_SIZE    = 2_000

    private val queue    = ConcurrentLinkedQueue<WardriveHit>()
    private val hitCount = AtomicInteger(0)

    private val scope   = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncJob: Job? = null

    fun start(context: Context) {
        if (syncJob?.isActive == true) return
        syncJob = scope.launch {
            while (isActive) {
                delay(BATCH_INTERVAL_MS)
                flush(context)
            }
        }
        Log.d(TAG, "Sync loop started")
    }

    fun stop(context: Context) {
        syncJob?.cancel()
        syncJob = null
        scope.launch { flush(context) }
        Log.d(TAG, "Sync loop stopped, final flush queued")
    }

    fun addHit(hit: WardriveHit) {
        if (queue.size >= MAX_QUEUE_SIZE) {
            queue.poll()
        }
        queue.add(hit)
        WearRepository.incrementWardriveHits(hitCount.incrementAndGet())
    }

    fun totalHits(): Int = hitCount.get()

    private suspend fun flush(context: Context) {
        val batch = drainQueue()
        if (batch.isEmpty()) return

        val payload = try {
            WardriveCodec.encodeHits(batch)
        } catch (e: Exception) {
            Log.e(TAG, "Encode failed, ${batch.size} hits dropped: ${e.message}")
            return
        }

        val nodes = try {
            Wearable.getNodeClient(context).connectedNodes.await()
        } catch (e: Exception) {
            Log.w(TAG, "No nodes available — re-queuing ${batch.size} hits: ${e.message}")
            batch.forEach { queue.add(it) }
            return
        }

        if (nodes.isEmpty()) {
            Log.d(TAG, "No connected nodes — re-queuing ${batch.size} hits")
            batch.forEach { queue.add(it) }
            return
        }

        val mc = Wearable.getMessageClient(context)
        var sent = false
        for (node in nodes) {
            try {
                mc.sendMessage(node.id, WearConstants.PATH_WARDRIVE_BATCH, payload).await()
                sent = true
                Log.d(TAG, "Flushed ${batch.size} hits to ${node.displayName}")
                break
            } catch (e: Exception) {
                Log.w(TAG, "Send to ${node.displayName} failed: ${e.message}")
            }
        }

        if (!sent) {
            Log.w(TAG, "All nodes failed — re-queuing ${batch.size} hits")
            batch.forEach { queue.add(it) }
        }
    }

    private fun drainQueue(): List<WardriveHit> {
        val batch = mutableListOf<WardriveHit>()
        while (true) batch.add(queue.poll() ?: break)
        return batch
    }
}
