package com.ghostech.blehound.wear

import app.cash.turbine.test
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class WearRepositoryTest {

    @Before
    fun setUp() {
        WearRepository.resetForTest()
    }

    @Test
    fun `updateSummary reflects in summary StateFlow`() = runTest {
        val newSummary = BleHoundSummary(
            total = 5, trackers = 2, drones = 1, gadgets = 1, feds = 1,
            isScanning = true, receivedAtMs = 1000L
        )
        WearRepository.updateSummary(newSummary)
        assertEquals(newSummary, WearRepository.summary.value)
    }

    @Test
    fun `emitAlert is received via turbine`() = runTest {
        val alert = TrackerAlert(
            deviceClass = "AirTag",
            address = "AA:BB:CC:DD:EE:FF",
            rssi = -75,
            receivedAtMs = 2000L
        )
        WearRepository.alerts.test {
            WearRepository.emitAlert(alert)
            val received = awaitItem()
            assertEquals(alert, received)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updatePhoneConnected true reflects in flow`() = runTest {
        WearRepository.updatePhoneConnected(true)
        assertTrue(WearRepository.phoneConnected.value)
    }

    @Test
    fun `updatePhoneConnected false reflects in flow`() = runTest {
        WearRepository.updatePhoneConnected(true)
        WearRepository.updatePhoneConnected(false)
        assertFalse(WearRepository.phoneConnected.value)
    }

    @Test
    fun `multiple updateSummary calls result in latest value`() = runTest {
        val first = BleHoundSummary(
            total = 1, trackers = 0, drones = 0, gadgets = 1, feds = 0,
            isScanning = false, receivedAtMs = 100L
        )
        val second = BleHoundSummary(
            total = 8, trackers = 3, drones = 2, gadgets = 2, feds = 1,
            isScanning = true, receivedAtMs = 200L
        )
        WearRepository.updateSummary(first)
        WearRepository.updateSummary(second)
        assertEquals(second, WearRepository.summary.value)
    }

    @Test
    fun `replay 1 means new turbine collector receives last emitted alert`() = runTest {
        val alert = TrackerAlert(
            deviceClass = "Tile",
            address = "11:22:33:44:55:66",
            rssi = -60,
            receivedAtMs = 3000L
        )
        WearRepository.emitAlert(alert)

        WearRepository.alerts.test {
            val received = awaitItem()
            assertEquals(alert, received)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetForTest restores summary to EMPTY`() = runTest {
        WearRepository.updateSummary(
            BleHoundSummary(
                total = 99, trackers = 5, drones = 5, gadgets = 5, feds = 5,
                isScanning = true, receivedAtMs = 9999L
            )
        )
        WearRepository.resetForTest()
        assertEquals(BleHoundSummary.EMPTY, WearRepository.summary.value)
    }

    @Test
    fun `resetForTest restores phoneConnected to false`() = runTest {
        WearRepository.updatePhoneConnected(true)
        WearRepository.resetForTest()
        assertFalse(WearRepository.phoneConnected.value)
    }
}
