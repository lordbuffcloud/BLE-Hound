package com.ghostech.blehound.wear

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition

// ── Palette ─────────────────────────────────────────────────────────────────

private val Amber       = Color(0xFFFFB300)
private val AlertRed    = Color(0xFFFF5252)
private val DroneOrange = Color(0xFFFF7043)
private val GadgetBlue  = Color(0xFF40C4FF)
private val FedGreen    = Color(0xFF69F0AE)
private val WardriveLime = Color(0xFF76FF03)
private val BgDark      = Color(0xFF0B0B0C)
private val Surface     = Color(0xFF151517)
private val SubText     = Color(0xFFA2A3A8)

// ── Navigation ───────────────────────────────────────────────────────────────

private enum class WearScreen { MAIN, DEVICES, DETAIL }

// ── Root ─────────────────────────────────────────────────────────────────────

@Composable
fun BleHoundWearApp(
    vm: WearViewModel,
    onScanToggle: (Boolean) -> Unit,
    onWardriveToggle: (Boolean) -> Unit
) {
    var screen by remember { mutableStateOf(WearScreen.MAIN) }

    val activeAlert       by vm.activeAlert.collectAsStateWithLifecycle()
    val lastAlert = remember { mutableStateOf<TrackerAlert?>(null) }
    if (activeAlert != null) lastAlert.value = activeAlert

    BackHandler(enabled = screen != WearScreen.MAIN) {
        screen = when (screen) {
            WearScreen.DETAIL  -> { vm.clearSelection(); WearScreen.DEVICES }
            WearScreen.DEVICES -> WearScreen.MAIN
            else               -> WearScreen.MAIN
        }
    }

    MaterialTheme {
        when (screen) {
            WearScreen.MAIN -> MainScreen(
                vm               = vm,
                lastAlert        = lastAlert.value,
                activeAlert      = activeAlert,
                onGoDevices      = { screen = WearScreen.DEVICES },
                onScanToggle     = onScanToggle,
                onWardriveToggle = onWardriveToggle
            )
            WearScreen.DEVICES -> DeviceListScreen(
                vm          = vm,
                onDeviceTap = { device ->
                    vm.selectDevice(device)
                    screen = WearScreen.DETAIL
                },
                onBack      = { screen = WearScreen.MAIN }
            )
            WearScreen.DETAIL -> DeviceDetailScreen(
                vm     = vm,
                onBack = { vm.clearSelection(); screen = WearScreen.DEVICES }
            )
        }
    }
}

// ── Main Screen ───────────────────────────────────────────────────────────────

@Composable
private fun MainScreen(
    vm: WearViewModel,
    lastAlert: TrackerAlert?,
    activeAlert: TrackerAlert?,
    onGoDevices: () -> Unit,
    onScanToggle: (Boolean) -> Unit,
    onWardriveToggle: (Boolean) -> Unit
) {
    val summary          by vm.summary.collectAsStateWithLifecycle()
    val phoneConnected   by vm.phoneConnected.collectAsStateWithLifecycle()
    val scanActive       by vm.scanActive.collectAsStateWithLifecycle()
    val nearbyDevices    by vm.nearbyDevices.collectAsStateWithLifecycle()
    val wardriveActive   by vm.wardriveActive.collectAsStateWithLifecycle()
    val wardriveHits     by vm.wardriveHits.collectAsStateWithLifecycle()
    val wardriveGps      by vm.wardriveGpsAvailable.collectAsStateWithLifecycle()

    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            state             = listState,
            contentPadding    = PaddingValues(top = 32.dp, bottom = 24.dp, start = 8.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand header
            item { BrandHeader(scanActive = scanActive) }

            // Alert banner
            item {
                AnimatedVisibility(
                    visible = activeAlert != null,
                    enter   = fadeIn() + slideInVertically { -it / 2 },
                    exit    = fadeOut() + slideOutVertically { -it / 2 }
                ) {
                    lastAlert?.let { AlertChip(alert = it, onDismiss = { vm.dismissAlert() }) }
                }
            }

            // Scan toggle
            item {
                ScanToggleChip(
                    active    = scanActive,
                    onToggle  = { onScanToggle(!scanActive) }
                )
            }

            // Device count — tappable if we have results
            item {
                DeviceCountChip(
                    count   = nearbyDevices.size,
                    active  = scanActive,
                    onClick = { if (nearbyDevices.isNotEmpty()) onGoDevices() }
                )
            }

            // Phone summary (when connected)
            if (phoneConnected && summary.total > 0) {
                item { PhoneSummaryChip(summary = summary) }
            }

            // Wardrive
            item {
                WardriveChip(
                    active       = wardriveActive,
                    gpsAvailable = wardriveGps,
                    hits         = wardriveHits,
                    onToggle     = { onWardriveToggle(!wardriveActive) }
                )
            }

            // Phone status footer
            item {
                Text(
                    text      = if (phoneConnected) "Phone connected" else "Phone offline",
                    color     = if (phoneConnected) SubText else Color.DarkGray,
                    fontSize  = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ── Device List Screen ────────────────────────────────────────────────────────

@Composable
private fun DeviceListScreen(
    vm: WearViewModel,
    onDeviceTap: (WearBleDevice) -> Unit,
    onBack: () -> Unit
) {
    val devices   by vm.nearbyDevices.collectAsStateWithLifecycle()
    val scanActive by vm.scanActive.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText          = { TimeText() },
        vignette          = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            state             = listState,
            contentPadding    = PaddingValues(top = 32.dp, bottom = 24.dp, start = 8.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text       = if (scanActive) "NEARBY \u2014 ${devices.size}" else "LAST SCAN \u2014 ${devices.size}",
                    color      = Amber,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
            }

            if (devices.isEmpty()) {
                item {
                    Text(
                        text      = "No devices found.\nStart a scan on main.",
                        color     = SubText,
                        fontSize  = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth()
                    )
                }
            } else {
                devices.forEach { device ->
                    item { DeviceRow(device = device, onClick = { onDeviceTap(device) }) }
                }
            }

            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick  = onBack,
                    colors   = ChipDefaults.chipColors(backgroundColor = Surface),
                    label    = { Text("BACK", color = SubText, fontSize = 11.sp) }
                )
            }
        }
    }
}

// ── Device Detail Screen ──────────────────────────────────────────────────────

@Composable
private fun DeviceDetailScreen(
    vm: WearViewModel,
    onBack: () -> Unit
) {
    val device by vm.selectedDevice.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()

    LaunchedEffect(device == null) {
        if (device == null) onBack()
    }

    Scaffold(
        timeText          = { TimeText() },
        vignette          = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        val d = device ?: return@Scaffold

        var nowMs by remember(d.mac) { mutableLongStateOf(System.currentTimeMillis()) }
        LaunchedEffect(d.mac) {
            while (true) {
                delay(1_000)
                nowMs = System.currentTimeMillis()
            }
        }

        ScalingLazyColumn(
            state             = listState,
            contentPadding    = PaddingValues(top = 32.dp, bottom = 24.dp, start = 8.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text       = d.displayName,
                    color      = Amber,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    modifier   = Modifier.fillMaxWidth()
                )
            }

            item { DetailRow(label = "MAC",    value = d.mac) }
            item { DetailRow(label = "RSSI",   value = "${d.rssi} dBm \u00b7 ${d.rssiLabel}") }

            if (d.deviceClass.isNotBlank()) {
                item { DetailRow(label = "CLASS", value = d.deviceClass, accent = classColor(d.deviceClass)) }
            }

            item { DetailRow(label = "FIRST",  value = "${elapsedLabel(nowMs - d.firstSeenMs)} ago") }
            item { DetailRow(label = "LAST",   value = "${elapsedLabel(nowMs - d.lastSeenMs)} ago") }

            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick  = onBack,
                    colors   = ChipDefaults.chipColors(backgroundColor = Surface),
                    label    = { Text("BACK", color = SubText, fontSize = 11.sp) }
                )
            }
        }
    }
}

// ── Reusable chips ────────────────────────────────────────────────────────────

@Composable
private fun BrandHeader(scanActive: Boolean) {
    Chip(
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "houndBEE header" },
        onClick  = {},
        enabled  = false,
        colors   = ChipDefaults.chipColors(
            backgroundColor        = Surface,
            disabledBackgroundColor = Surface,
            disabledContentColor   = Color.White
        ),
        label = {
            Text(
                text       = "houndBEE",
                color      = Amber,
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp
            )
        },
        secondaryLabel = {
            Text(
                text     = if (scanActive) "Scanning\u2026" else "BLE Scanner",
                color    = if (scanActive) Amber.copy(alpha = 0.7f) else SubText,
                fontSize = 10.sp
            )
        }
    )
}

@Composable
private fun ScanToggleChip(active: Boolean, onToggle: () -> Unit) {
    val bg    = if (active) Color(0xFF1A1200) else Surface
    val color = if (active) Amber else Color.LightGray
    Chip(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = if (active) "Stop scan" else "Start scan" },
        onClick  = onToggle,
        colors   = ChipDefaults.chipColors(backgroundColor = bg, contentColor = Color.White),
        label    = {
            Text(
                text       = if (active) "STOP SCAN" else "START SCAN",
                color      = color,
                fontWeight = FontWeight.Bold,
                fontSize   = 13.sp
            )
        },
        secondaryLabel = {
            Text(
                text     = if (active) "Tap to stop" else "Scan nearby BLE",
                color    = color.copy(alpha = 0.65f),
                fontSize = 11.sp
            )
        }
    )
}

@Composable
private fun DeviceCountChip(count: Int, active: Boolean, onClick: () -> Unit) {
    val hasDevices = count > 0
    val bg    = if (hasDevices) Amber.copy(alpha = 0.08f) else Surface
    val color = if (hasDevices) Amber else SubText
    Chip(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "$count devices found" },
        onClick  = onClick,
        colors   = ChipDefaults.chipColors(
            backgroundColor        = bg,
            disabledBackgroundColor = bg,
            disabledContentColor   = Color.White
        ),
        enabled = hasDevices,
        label   = {
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment   = Alignment.CenterVertically
            ) {
                Text(
                    text     = if (active) "In range" else "Last scan",
                    color    = SubText,
                    fontSize = 12.sp
                )
                Text(
                    text       = "$count devices",
                    color      = color,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp
                )
            }
        }
    )
}

@Composable
private fun PhoneSummaryChip(summary: BleHoundSummary) {
    Chip(
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Phone summary" },
        onClick  = {},
        enabled  = false,
        colors   = ChipDefaults.chipColors(
            backgroundColor        = Surface,
            disabledBackgroundColor = Surface,
            disabledContentColor   = Color.White
        ),
        label = {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryBadge("T", summary.trackers, Amber)
                SummaryBadge("D", summary.drones, DroneOrange)
                SummaryBadge("G", summary.gadgets, GadgetBlue)
                SummaryBadge("F", summary.feds, FedGreen)
            }
        },
        secondaryLabel = {
            Text("Phone scan: ${summary.total} total", color = SubText, fontSize = 10.sp)
        }
    )
}

@Composable
private fun SummaryBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), color = if (count > 0) color else Color.DarkGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = SubText, fontSize = 9.sp)
    }
}

@Composable
private fun AlertChip(alert: TrackerAlert, onDismiss: () -> Unit) {
    Chip(
        modifier = Modifier.fillMaxWidth().semantics {
            contentDescription = "Alert: ${alert.deviceClass} at ${alert.rssi} dBm. Tap to dismiss."
        },
        onClick  = onDismiss,
        colors   = ChipDefaults.chipColors(backgroundColor = Color(0xFF1F0A00)),
        label    = {
            Text(text = alert.deviceClass, color = AlertRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        },
        secondaryLabel = {
            Text("${alert.rssi} dBm \u00b7 Tap to dismiss", color = AlertRed.copy(alpha = 0.7f), fontSize = 11.sp)
        }
    )
}

@Composable
private fun WardriveChip(active: Boolean, gpsAvailable: Boolean, hits: Int, onToggle: () -> Unit) {
    val bg    = if (active) Color(0xFF0D1A06) else Surface
    val color = if (active) WardriveLime else Color.LightGray
    val sub   = if (active) "${if (gpsAvailable) "\u2022 GPS" else "\u25cb GPS"}  $hits hits" else "BLE + GPS logging"
    Chip(
        modifier = Modifier.fillMaxWidth().semantics {
            contentDescription = if (active) "Wardrive active, $hits hits" else "Start wardrive"
        },
        onClick  = onToggle,
        colors   = ChipDefaults.chipColors(backgroundColor = bg, contentColor = Color.White),
        label    = { Text(if (active) "Wardrive ON" else "Start Wardrive", color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
        secondaryLabel = {
            Text(sub, color = if (active && gpsAvailable) color.copy(alpha = 0.70f) else SubText, fontSize = 11.sp)
        }
    )
}

@Composable
private fun DeviceRow(device: WearBleDevice, onClick: () -> Unit) {
    val classAccent = classColor(device.deviceClass)
    Chip(
        modifier = Modifier.fillMaxWidth().semantics {
            contentDescription = "${device.displayName}, ${device.rssi} dBm"
        },
        onClick  = onClick,
        colors   = ChipDefaults.chipColors(
            backgroundColor = Surface,
            contentColor    = Color.White
        ),
        label = {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text      = device.displayName,
                    color     = Color.White,
                    fontSize  = 12.sp,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis,
                    modifier  = Modifier.weight(1f)
                )
                Text(
                    text      = "${device.rssi}",
                    color     = rssiColor(device.rssi),
                    fontSize  = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        secondaryLabel = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (device.deviceClass.isNotBlank()) {
                    Text(
                        text     = device.deviceClass,
                        color    = classAccent,
                        fontSize = 10.sp
                    )
                    Text(text = "  \u00b7  ", color = SubText, fontSize = 10.sp)
                }
                Text(text = device.rssiLabel, color = SubText, fontSize = 10.sp)
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String, accent: Color = Color.White) {
    Chip(
        modifier = Modifier.fillMaxWidth(),
        onClick  = {},
        enabled  = false,
        colors   = ChipDefaults.chipColors(
            backgroundColor        = Surface,
            disabledBackgroundColor = Surface,
            disabledContentColor   = Color.White
        ),
        label = {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = label, color = SubText, fontSize = 10.sp)
                Text(text = value, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Medium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    textAlign = TextAlign.End)
            }
        }
    )
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun rssiColor(rssi: Int): Color = when {
    rssi >= -60 -> Color(0xFF76FF03)   // lime — excellent
    rssi >= -75 -> Color(0xFFFFB300)   // amber — good
    rssi >= -90 -> Color(0xFFFF7043)   // orange — fair
    else        -> Color(0xFFFF5252)   // red — weak
}

private fun classColor(cls: String): Color = when {
    cls.contains("Tracker", ignoreCase = true) ||
    cls.contains("AirTag",  ignoreCase = true) ||
    cls.contains("Tile",    ignoreCase = true) ||
    cls.contains("Galaxy",  ignoreCase = true) -> Color(0xFFFFB300)

    cls.contains("Drone",   ignoreCase = true) -> DroneOrange
    cls.contains("Fed",     ignoreCase = true) ||
    cls.contains("Axon",    ignoreCase = true) ||
    cls.contains("Flock",   ignoreCase = true) -> FedGreen

    cls.contains("Flipper", ignoreCase = true) ||
    cls.contains("Gadget",  ignoreCase = true) ||
    cls.contains("Dev",     ignoreCase = true) -> GadgetBlue

    else -> SubText
}

private fun elapsedLabel(ms: Long): String = when {
    ms <= 0L        -> "just now"
    ms < 60_000L    -> "${ms / 1000}s"
    ms < 3_600_000L -> "${ms / 60_000}m"
    else            -> "${ms / 3_600_000}h"
}
