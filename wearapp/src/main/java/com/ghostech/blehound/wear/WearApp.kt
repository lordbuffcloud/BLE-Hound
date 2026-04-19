package com.ghostech.blehound.wear

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

private val Amber = Color(0xFFFFB300)
private val AlertRed = Color(0xFFFF5252)
private val DroneOrange = Color(0xFFFF7043)
private val GadgetBlue = Color(0xFF40C4FF)
private val FedGreen = Color(0xFF69F0AE)
private val CardSurface = Color(0xFF13131C)

@Composable
fun BleHoundWearApp(vm: WearViewModel) {
    val summary by vm.summary.collectAsStateWithLifecycle()
    val activeAlert by vm.activeAlert.collectAsStateWithLifecycle()
    val phoneConnected by vm.phoneConnected.collectAsStateWithLifecycle()

    // Keep the last non-null alert alive so the exit animation has content to render.
    val lastAlert = remember { androidx.compose.runtime.mutableStateOf<TrackerAlert?>(null) }
    if (activeAlert != null) lastAlert.value = activeAlert

    val listState = rememberScalingLazyListState()

    MaterialTheme {
        Scaffold(
            timeText = { TimeText() },
            vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
            positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
        ) {
            ScalingLazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    top = 32.dp,
                    bottom = 24.dp,
                    start = 8.dp,
                    end = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item { HeaderChip(phoneConnected = phoneConnected, scanning = summary.isScanning) }

                item {
                    AnimatedVisibility(
                        visible = activeAlert != null,
                        enter = fadeIn() + slideInVertically { -it / 2 },
                        exit = fadeOut() + slideOutVertically { -it / 2 }
                    ) {
                        lastAlert.value?.let { alert ->
                            AlertChip(alert = alert, onDismiss = { vm.dismissAlert() })
                        }
                    }
                }

                if (!phoneConnected && summary.receivedAtMs == 0L) {
                    item { DisconnectedPlaceholder() }
                } else {
                    item {
                        CountChip(
                            label = "Trackers",
                            count = summary.trackers,
                            accent = Amber,
                            description = "Trackers: ${summary.trackers}"
                        )
                    }
                    item {
                        CountChip(
                            label = "Drones",
                            count = summary.drones,
                            accent = DroneOrange,
                            description = "Drones: ${summary.drones}"
                        )
                    }
                    item {
                        CountChip(
                            label = "Gadgets",
                            count = summary.gadgets,
                            accent = GadgetBlue,
                            description = "Gadgets: ${summary.gadgets}"
                        )
                    }
                    item {
                        CountChip(
                            label = "Feds",
                            count = summary.feds,
                            accent = FedGreen,
                            description = "Feds: ${summary.feds}"
                        )
                    }
                    item { TotalFooter(total = summary.total) }
                }
            }
        }
    }
}

@Composable
private fun HeaderChip(phoneConnected: Boolean, scanning: Boolean) {
    Chip(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "BLE Hound header" },
        onClick = {},
        enabled = false,
        colors = ChipDefaults.chipColors(backgroundColor = CardSurface),
        label = {
            Text(
                text = "BLE Hound",
                color = Amber,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        },
        secondaryLabel = {
            Text(
                text = when {
                    !phoneConnected -> "Phone offline"
                    scanning -> "Scanning\u2026"
                    else -> "Idle"
                },
                color = if (scanning) Amber.copy(alpha = 0.7f) else Color.Gray,
                fontSize = 11.sp
            )
        }
    )
}

@Composable
private fun AlertChip(alert: TrackerAlert, onDismiss: () -> Unit) {
    Chip(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Alert: ${alert.deviceClass} at ${alert.rssi} dBm. Tap to dismiss." },
        onClick = onDismiss,
        colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF1F0A00)),
        label = {
            Text(
                text = alert.deviceClass,
                color = AlertRed,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        },
        secondaryLabel = {
            Text(
                text = "${alert.rssi} dBm \u00b7 Tap to dismiss",
                color = AlertRed.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    )
}

@Composable
private fun CountChip(label: String, count: Int, accent: Color, description: String) {
    val bg = if (count > 0) accent.copy(alpha = 0.10f) else CardSurface
    Chip(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = description },
        onClick = {},
        enabled = false,
        colors = ChipDefaults.chipColors(
            backgroundColor = bg,
            disabledBackgroundColor = bg,
            disabledContentColor = Color.White
        ),
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
                Text(
                    text = count.toString(),
                    color = if (count > 0) accent else Color.DarkGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.End
                )
            }
        }
    )
}

@Composable
private fun TotalFooter(total: Int) {
    Text(
        text = "$total in range",
        color = if (total > 0) Amber.copy(alpha = 0.7f) else Color.DarkGray,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Total: $total devices in range" }
    )
}

@Composable
private fun DisconnectedPlaceholder() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Phone offline",
            color = Color.Gray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { contentDescription = "Phone offline" }
        )
        Text(
            text = "Open BLE Hound\non your phone",
            color = Color.DarkGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics {
                contentDescription = "Open BLE Hound on your phone"
            }
        )
    }
}
