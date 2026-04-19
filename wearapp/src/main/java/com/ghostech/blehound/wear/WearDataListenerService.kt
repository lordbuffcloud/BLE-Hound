package com.ghostech.blehound.wear

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class WearDataListenerService : WearableListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onDataChanged(events: DataEventBuffer) {
        events.use { buffer ->
            buffer.forEach { event ->
                if (event.type != DataEvent.TYPE_CHANGED) return@forEach
                val path = event.dataItem.uri.path ?: return@forEach
                val data = event.dataItem.data ?: return@forEach
                when (path) {
                    WearConstants.PATH_SUMMARY -> handleSummary(data)
                    else -> Log.d(TAG, "Unknown path: $path")
                }
            }
        }
    }

    override fun onMessageReceived(event: MessageEvent) {
        when (event.path) {
            WearConstants.PATH_ALERT -> handleAlert(event.data)
            else -> Log.d(TAG, "Unknown message path: ${event.path}")
        }
    }

    private fun handleSummary(data: ByteArray) {
        when (val result = DataParser.parseSummary(data)) {
            is ParseResult.Success -> WearRepository.updateSummary(result.value)
            is ParseResult.Failure -> Log.w(TAG, "parseSummary failed: ${result.reason}")
        }
    }

    private fun handleAlert(data: ByteArray) {
        when (val result = DataParser.parseAlert(data)) {
            is ParseResult.Success -> scope.launch { WearRepository.emitAlert(result.value) }
            is ParseResult.Failure -> Log.w(TAG, "parseAlert failed: ${result.reason}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        private const val TAG = "WearDataListenerSvc"
    }
}
