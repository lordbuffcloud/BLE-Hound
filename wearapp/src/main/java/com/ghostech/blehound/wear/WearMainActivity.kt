package com.ghostech.blehound.wear

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WearMainActivity : ComponentActivity() {

    private val vm: WearViewModel by viewModels()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            vm.startWardrive()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BleHoundWearApp(
                vm = vm,
                onWardriveToggle = { enable ->
                    if (enable) requestWardriveAndStart() else vm.stopWardrive()
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPhoneConnection()
    }

    private fun requestWardriveAndStart() {
        if (vm.hasLocationPermission()) {
            vm.startWardrive()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
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
