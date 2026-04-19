package com.ghostech.blehound.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WearAppScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private lateinit var vm: WearViewModel

    @Before
    fun setUp() {
        WearRepository.resetForTest()
        vm = WearViewModel(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `phone offline with no cached data shows placeholder text`() {
        WearRepository.updatePhoneConnected(false)

        rule.setContent { BleHoundWearApp(vm) }

        rule.onNodeWithText("Phone offline").assertIsDisplayed()
        rule.onNodeWithText("Open BLE Hound\non your phone", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `tracker count 3 is displayed in chip`() {
        WearRepository.updatePhoneConnected(true)
        WearRepository.updateSummary(
            BleHoundSummary(
                total = 5, trackers = 3, drones = 0, gadgets = 2, feds = 0,
                isScanning = false, receivedAtMs = System.currentTimeMillis()
            )
        )

        rule.setContent { BleHoundWearApp(vm) }

        rule.onNodeWithContentDescription("Trackers: 3").assertIsDisplayed()
    }

    @Test
    fun `active alert chip appears when alert emitted`() {
        WearRepository.updatePhoneConnected(true)
        WearRepository.updateSummary(
            BleHoundSummary(
                total = 1, trackers = 1, drones = 0, gadgets = 0, feds = 0,
                isScanning = true, receivedAtMs = System.currentTimeMillis()
            )
        )

        rule.setContent { BleHoundWearApp(vm) }

        val alert = TrackerAlert(
            deviceClass = "AirTag",
            address = "AA:BB:CC:DD:EE:FF",
            rssi = -70,
            receivedAtMs = System.currentTimeMillis()
        )
        runBlocking { WearRepository.emitAlert(alert) }

        rule.waitForIdle()
        rule.onNodeWithContentDescription(
            "Alert: AirTag at -70 dBm. Tap to dismiss.", substring = true
        ).assertIsDisplayed()
    }

    @Test
    fun `dismissing alert removes chip from screen`() {
        WearRepository.updatePhoneConnected(true)
        WearRepository.updateSummary(
            BleHoundSummary(
                total = 1, trackers = 1, drones = 0, gadgets = 0, feds = 0,
                isScanning = false, receivedAtMs = System.currentTimeMillis()
            )
        )

        rule.setContent { BleHoundWearApp(vm) }

        val alert = TrackerAlert(
            deviceClass = "Galaxy Tag",
            address = "11:22:33:44:55:66",
            rssi = -55,
            receivedAtMs = System.currentTimeMillis()
        )
        runBlocking { WearRepository.emitAlert(alert) }
        rule.waitForIdle()

        rule.onNodeWithContentDescription(
            "Alert: Galaxy Tag at -55 dBm. Tap to dismiss.", substring = true
        ).performClick()

        rule.waitForIdle()
        rule.onNodeWithContentDescription(
            "Alert: Galaxy Tag at -55 dBm. Tap to dismiss.", substring = true
        ).assertDoesNotExist()
    }

    @Test
    fun `scanning state shows Scanning label in header`() {
        WearRepository.updatePhoneConnected(true)
        WearRepository.updateSummary(
            BleHoundSummary(
                total = 0, trackers = 0, drones = 0, gadgets = 0, feds = 0,
                isScanning = true, receivedAtMs = System.currentTimeMillis()
            )
        )

        rule.setContent { BleHoundWearApp(vm) }

        rule.onNodeWithText("Scanning\u2026").assertIsDisplayed()
    }
}
