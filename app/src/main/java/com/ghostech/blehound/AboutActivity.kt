package com.ghostech.blehound

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class AboutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0B0B0C.toInt())
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(24), dp(18), dp(14))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xFF1C1C20.toInt(), 0xFF151517.toInt(), 0xFF0B0B0C.toInt())
            ).apply { setStroke(dp(1), themeColor(this@AboutActivity)) }
        }

        val title = TextView(this).apply {
            text = "houndBEE"
            gravity = Gravity.CENTER
            textSize = 22f
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD_ITALIC)
            setTextColor(themeColor(this@AboutActivity))
            setShadowLayer(14f, 0f, 0f, 0xFFFFB300.toInt())
        }
        val subtitle = TextView(this).apply {
            text = "BLE SURVEILLANCE + WARDRIVE"
            gravity = Gravity.CENTER
            textSize = 10f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            setTextColor(0xFFA2A3A8.toInt())
            letterSpacing = 0.15f
            setPadding(0, dp(4), 0, 0)
        }

        header.addView(title)
        header.addView(subtitle)

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(24))
        }

        content.addView(sectionTitle("WHAT IT DETECTS"))
        content.addView(bodyText("TRACKERS  —  AirTag, Tile, Galaxy Tag, Find My"))
        content.addView(bodyText("GADGETS   —  Flipper Zero, Pwnagotchi, Card Skimmer, Dev Board, WiFi Pineapple, Meta Glasses"))
        content.addView(bodyText("DRONES    —  DJI / Parrot / Skydio / Autel / BLE Remote ID"))
        content.addView(bodyText("FEDS      —  Axon body cam, Taser, Flock ALPR"))
        content.addView(spannableNote("NOTE:", " Pwnagotchi and WiFi Pineapple detections are limited on Android. Unlike dedicated hardware tools that use raw 802.11 frame analysis, this app relies on standard WiFi scan results."))

        content.addView(sectionTitle("WARDRIVE"))
        content.addView(bodyText("Scan BLE devices with GPS tagging via a paired Wear OS watch or the phone itself. Sessions export as WiGLE-format CSV with optional auto-upload to wigle.net."))

        content.addView(sectionTitle("RSSI"))
        content.addView(bodyText("Received Signal Strength Indicator. Reflects signal strength at time of scan. Use it to track proximity changes and movement patterns."))

        content.addView(sectionTitle("BUILT BY CK42X"))
        content.addView(bodyText("houndBEE is a CK42X product. Every tool has a purpose. Every purpose, a tool."))

        val ck42xButton = buildButton("CK42X.COM")
        ck42xButton.setOnClickListener { openUrl("https://ck42x.com") }
        content.addView(ck42xButton)

        val houndBeeButton = buildButton("HOUNDBEE FIRMWARE")
        houndBeeButton.setOnClickListener { openUrl("https://ck42x.com/houndbee") }
        content.addView(houndBeeButton)

        content.addView(sectionTitle("OFFENSIVE TOOLS"))
        content.addView(bodyText("BLE attack suite (Sour Apple, BLE Flood, Swift Pair, Phantom Flood) by CK42X / nyanBEE."))

        val nyanbeeButton = buildButton("NYANBEE FIRMWARE")
        nyanbeeButton.setOnClickListener { openUrl("https://ck42x.com/nyanbee") }
        content.addView(nyanbeeButton)

        content.addView(sectionTitle("ORIGINS"))
        content.addView(bodyText("Originally created by GH0ST3CH. houndBEE forks and extends the original BLE-Hound with Wear OS companion, wardrive mode, and full CK42X offensive toolkit."))

        val ghButton = buildButton("ORIGINAL REPO (GH0ST3CH)")
        ghButton.setOnClickListener { openUrl("https://github.com/GH0ST3CH") }
        content.addView(ghButton)

        content.addView(sectionTitle("CREDITS"))
        content.addView(bodyText("Inspired by HaleHound (JesseCHale) and ESP32 Marauder (justcallmekoko)."))

        val halehoundButton = buildButton("HALEHOUND")
        halehoundButton.setOnClickListener { openUrl("https://github.com/JesseCHale/HaleHound-CYD") }
        content.addView(halehoundButton)

        val marauderButton = buildButton("ESP32 MARAUDER")
        marauderButton.setOnClickListener { openUrl("https://github.com/justcallmekoko/ESP32Marauder") }
        content.addView(marauderButton)

        scroll.addView(content)
        root.addView(header)
        root.addView(scroll)
        setContentView(root)
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun sectionTitle(text: String) = TextView(this).apply {
        this.text = text
        textSize = 16f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setTextColor(0xFFFFB300.toInt())
        setPadding(0, dp(18), 0, dp(8))
    }

    private fun bodyText(text: String) = TextView(this).apply {
        this.text = text
        textSize = 13f
        typeface = Typeface.MONOSPACE
        setTextColor(0xFFE9E9EA.toInt())
        setPadding(0, 0, 0, dp(10))
    }

    private fun buildButton(label: String) = Button(this).apply {
        text = label
        isAllCaps = true
        textSize = 13f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setTextColor(0xFFFFB300.toInt())
        background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(0xFF1E1E22.toInt(), 0xFF151517.toInt())
        ).apply {
            cornerRadius = dp(18).toFloat()
            setStroke(dp(1), themeColor(this@AboutActivity))
        }
        setPadding(dp(10), dp(14), dp(10), dp(14))
    }

    
    private fun spannableNote(prefix: String, body: String) = TextView(this).apply {
        val full = prefix + body
        val spannable = android.text.SpannableString(full)
        spannable.setSpan(
            android.text.style.ForegroundColorSpan(0xFFFF4444.toInt()),
            0,
            prefix.length,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        text = spannable
        textSize = 13f
        typeface = Typeface.MONOSPACE
        setTextColor(0xFFE9E9EA.toInt())
        setPadding(0, 0, 0, dp(10))
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
