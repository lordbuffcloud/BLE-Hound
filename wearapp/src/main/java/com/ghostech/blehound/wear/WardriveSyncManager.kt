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
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

internal object WardriveSyncManager {

    private const val TAG = "WardriveSyncMgr"
    private const val BATCH_INTERVAL_MS = 30_000L
    private const val MAX_QUEUE_SIZE    = 2_000

    private val queue    = ArrayBlockingQueue<WardriveHit>(MAX_QUEUE_SIZE)
    private val hitCount = AtomicInteger(0)

    private val scope   = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncJob: Job? = null
    private var appContext: Context? = null

    fun start(context: Context) {
        if (syncJob?.isActive == true) return
        appContext = context.applicationContext
        hitCount.set(0)
        syncJob = scope.launch {
            while (isActive) {
                delay(BATCH_INTERVAL_MS)
                flush(appContext ?: return@launch)
            }
        }
        Log.d(TAG, "Sync loop started")
    }

    fun stop() {
        syncJob?.cancel()
        syncJob = null
        val ctx = appContext ?: return
        scope.launch { flush(ctx) }
        Log.d(TAG, "Sync loop stopped, final flush queued")
    }

    fun addHit(hit: WardriveHit) {
        if (!queue.offer(hit)) {
            queue.poll()
            queue.offer(hit)
        }
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
            requeueBatch(batch)
            return
        }

        if (nodes.isEmpty()) {
            Log.d(TAG, "No connected nodes — re-queuing ${batch.size} hits")
            requeueBatch(batch)
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
            requeueBatch(batch)
        }
    }

    private fun requeueBatch(batch: List<WardriveHit>) {
        var dropped = 0
        batch.forEach { if (!queue.offer(it)) dropped++ }
        if (dropped > 0) Log.w(TAG, "$dropped hits dropped — queue full")
    }

    private fun drainQueue(): List<WardriveHit> {
        val batch = mutableListOf<WardriveHit>()
        while (true) batch.add(queue.poll() ?: break)
        return batch
    }
}
