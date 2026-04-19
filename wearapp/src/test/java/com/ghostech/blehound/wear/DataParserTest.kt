package com.ghostech.blehound.wear

import kotlin.test.assertIs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DataParserTest {

    // ── parseSummary ─────────────────────────────────────────────────────────

    @Test
    fun `valid JSON returns Success with correct field values including receivedAtMs`() {
        val json = """{"total":10,"trackers":2,"drones":1,"gadgets":4,"feds":3,"scanning":true}"""
        val now = 999_000L
        val result = DataParser.parseSummary(json.toByteArray(Charsets.UTF_8), nowMs = now)
        val success = assertIs<ParseResult.Success<BleHoundSummary>>(result)
        assertEquals(10, success.value.total)
        assertEquals(2, success.value.trackers)
        assertEquals(1, success.value.drones)
        assertEquals(4, success.value.gadgets)
        assertEquals(3, success.value.feds)
        assertTrue(success.value.isScanning)
        assertEquals(now, success.value.receivedAtMs)
    }

    @Test
    fun `missing fields returns Success with all zeros`() {
        val json = """{}"""
        val result = DataParser.parseSummary(json.toByteArray(Charsets.UTF_8))
        val success = assertIs<ParseResult.Success<BleHoundSummary>>(result)
        assertEquals(0, success.value.total)
        assertEquals(0, success.value.trackers)
        assertEquals(0, success.value.drones)
        assertEquals(0, success.value.gadgets)
        assertEquals(0, success.value.feds)
        assertEquals(false, success.value.isScanning)
    }

    @Test
    fun `negative counts are clamped to zero`() {
        val json = """{"total":-5,"trackers":-1,"drones":-2,"gadgets":-3,"feds":-4,"scanning":false}"""
        val result = DataParser.parseSummary(json.toByteArray(Charsets.UTF_8))
        val success = assertIs<ParseResult.Success<BleHoundSummary>>(result)
        assertEquals(0, success.value.total)
        assertEquals(0, success.value.trackers)
        assertEquals(0, success.value.drones)
        assertEquals(0, success.value.gadgets)
        assertEquals(0, success.value.feds)
    }

    @Test
    fun `empty byte array returns Failure`() {
        val result = DataParser.parseSummary(ByteArray(0))
        assertIs<ParseResult.Failure>(result)
    }

    @Test
    fun `malformed JSON string returns Failure`() {
        val result = DataParser.parseSummary("not json at all".toByteArray(Charsets.UTF_8))
        assertIs<ParseResult.Failure>(result)
    }

    // ── parseAlert ───────────────────────────────────────────────────────────

    @Test
    fun `valid JSON returns Success with correct fields`() {
        val json = """{"class":"AirTag","address":"AA:BB:CC:DD:EE:FF","rssi":-65}"""
        val now = 12345L
        val result = DataParser.parseAlert(json.toByteArray(Charsets.UTF_8), nowMs = now)
        val success = assertIs<ParseResult.Success<TrackerAlert>>(result)
        assertEquals("AirTag", success.value.deviceClass)
        assertEquals("AA:BB:CC:DD:EE:FF", success.value.address)
        assertEquals(-65, success.value.rssi)
        assertEquals(now, success.value.receivedAtMs)
    }

    @Test
    fun `positive RSSI is clamped to 0`() {
        val json = """{"class":"Tile","address":"11:22:33:44:55:66","rssi":10}"""
        val result = DataParser.parseAlert(json.toByteArray(Charsets.UTF_8))
        val success = assertIs<ParseResult.Success<TrackerAlert>>(result)
        assertEquals(0, success.value.rssi)
    }

    @Test
    fun `RSSI below -120 is clamped to -120`() {
        val json = """{"class":"Tile","address":"11:22:33:44:55:66","rssi":-200}"""
        val result = DataParser.parseAlert(json.toByteArray(Charsets.UTF_8))
        val success = assertIs<ParseResult.Success<TrackerAlert>>(result)
        assertEquals(-120, success.value.rssi)
    }

    @Test
    fun `missing class field returns Failure`() {
        val json = """{"address":"AA:BB:CC:DD:EE:FF","rssi":-70}"""
        val result = DataParser.parseAlert(json.toByteArray(Charsets.UTF_8))
        assertIs<ParseResult.Failure>(result)
    }

    @Test
    fun `blank class field returns Failure`() {
        val json = """{"class":"   ","address":"AA:BB:CC:DD:EE:FF","rssi":-70}"""
        val result = DataParser.parseAlert(json.toByteArray(Charsets.UTF_8))
        assertIs<ParseResult.Failure>(result)
    }

    @Test
    fun `missing address field returns Failure`() {
        val json = """{"class":"AirTag","rssi":-70}"""
        val result = DataParser.parseAlert(json.toByteArray(Charsets.UTF_8))
        assertIs<ParseResult.Failure>(result)
    }

    @Test
    fun `blank address field returns Failure`() {
        val json = """{"class":"AirTag","address":"  ","rssi":-70}"""
        val result = DataParser.parseAlert(json.toByteArray(Charsets.UTF_8))
        assertIs<ParseResult.Failure>(result)
    }

    @Test
    fun `empty byte array returns Failure for parseAlert`() {
        val result = DataParser.parseAlert(ByteArray(0))
        assertIs<ParseResult.Failure>(result)
    }

    @Test
    fun `malformed JSON returns Failure for parseAlert`() {
        val result = DataParser.parseAlert("{bad json".toByteArray(Charsets.UTF_8))
        assertIs<ParseResult.Failure>(result)
    }

    // ── Roundtrip ────────────────────────────────────────────────────────────

    @Test
    fun `encodeSummary then parseSummary preserves all values`() {
        val encoded = DataParser.encodeSummary(
            total = 15, trackers = 3, drones = 2, gadgets = 7, feds = 1, scanning = true
        )
        val result = DataParser.parseSummary(encoded)
        val success = assertIs<ParseResult.Success<BleHoundSummary>>(result)
        assertEquals(15, success.value.total)
        assertEquals(3, success.value.trackers)
        assertEquals(2, success.value.drones)
        assertEquals(7, success.value.gadgets)
        assertEquals(1, success.value.feds)
        assertTrue(success.value.isScanning)
    }

    @Test
    fun `encodeAlert then parseAlert preserves all values`() {
        val encoded = DataParser.encodeAlert(
            deviceClass = "Galaxy Tag",
            address = "DE:AD:BE:EF:00:01",
            rssi = -80
        )
        val result = DataParser.parseAlert(encoded)
        val success = assertIs<ParseResult.Success<TrackerAlert>>(result)
        assertEquals("Galaxy Tag", success.value.deviceClass)
        assertEquals("DE:AD:BE:EF:00:01", success.value.address)
        assertEquals(-80, success.value.rssi)
    }

    @Test
    fun `encodeSummary with negative inputs clamps values`() {
        val encoded = DataParser.encodeSummary(
            total = -10, trackers = -5, drones = -1, gadgets = -3, feds = -2, scanning = false
        )
        val result = DataParser.parseSummary(encoded)
        val success = assertIs<ParseResult.Success<BleHoundSummary>>(result)
        assertEquals(0, success.value.total)
        assertEquals(0, success.value.trackers)
        assertEquals(0, success.value.drones)
        assertEquals(0, success.value.gadgets)
        assertEquals(0, success.value.feds)
    }
}
