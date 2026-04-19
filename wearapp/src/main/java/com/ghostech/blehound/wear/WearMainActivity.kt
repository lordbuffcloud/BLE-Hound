package com.ghostech.blehound.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WearMainActivity : ComponentActivity() {

    private val vm: WearViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BleHoundWearApp(vm) }
    }

    override fun onResume() {
        super.onResume()
        refreshPhoneConnection()
    }

    private fun refreshPhoneConnection() {
        lifecycleScope.launch {
            val connected = try {
                Wearable.getNodeClient(this@WearMainActivity)
                    .connectedNodes
                    .await()
                    .isNotEmpty()
            } catch (e: Exception) {
                false
            }
            WearRepository.updatePhoneConnected(connected)
        }
    }
}
