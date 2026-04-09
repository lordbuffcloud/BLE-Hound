package com.ghostech.blehound

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class FilterModeActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("blehound_prefs", MODE_PRIVATE) }
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF000000.toInt())
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(12))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xFF2A0000.toInt(), 0xFF140000.toInt(), 0xFF000000.toInt())
            ).apply { setStroke(dp(1), themeColor(this@FilterModeActivity)) }
        }

        val title = TextView(this).apply {
            text = "FILTER MODE"
            gravity = android.view.Gravity.CENTER_VERTICAL
        textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            textSize = 20f
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD_ITALIC)
            setTextColor(themeColor(this@FilterModeActivity))
            gravity = android.view.Gravity.CENTER
        textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            setShadowLayer(12f, 0f, 0f, 0xFFFF9900.toInt())
        }
        header.addView(title)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(24))
        }

        val scroll = ScrollView(this).apply { addView(content) }

        val backWrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(0), dp(16), dp(20))
        }

        val back = buildHellButton("BACK")
        back.setOnClickListener { finish() }
        backWrap.addView(back)

        root.addView(header)
        root.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        root.addView(backWrap)

        setContentView(root)
        render()
    }

    private fun render() {
        content.removeAllViews()

        val top = buildHellButton(
            if (prefs.getBoolean("filtered_mode", true)) "FILTER MODE: ON" else "FILTER MODE: OFF"
        )
        top.setOnClickListener {
            prefs.edit().putBoolean("filtered_mode", !prefs.getBoolean("filtered_mode", true)).apply()
            render()
        }
        content.addView(top)

        addNote("CATEGORY ON = show all in that category. CATEGORY OFF = only specifically ON device types from that category are shown.")
        addSection("BLACKLIST / WHITELIST")
        addDotRow("filter_show_blacklisted", "BLACKLISTED")
        addDotRow("filter_show_whitelisted", "WHITELISTED")
        addSection("CATEGORIES")
        addDotRow("filter_cat_trackers", "TRACKERS")
        addDotRow("filter_cat_gadgets", "GADGETS")
        addDotRow("filter_cat_drones", "DRONES")
        addDotRow("filter_cat_feds", "FEDS")

        addSection("DEVICE TYPES")
        addNote("Use these when the category above is empty and you only want specific devices from that category shown.")
        addDotRow("filter_type_airtag", "AIRTAG")
        addDotRow("filter_type_findmy", "FIND MY")
        addDotRow("filter_type_tile", "TILE")
        addDotRow("filter_type_galaxytag", "GALAXY TAG")
        addDotRow("filter_type_flipperzero", "FLIPPER ZERO")
        addDotRow("filter_type_pwnagotchi", "PWNAGOTCHI")
        addDotRow("filter_type_cardskimmer", "CARD SKIMMER")
        addDotRow("filter_type_esp32arduino", "ESP32 / ARDUINO DEVICE")
        addDotRow("filter_type_wifipineapple", "WIFI PINEAPPLE")
        addDotRow("filter_type_metaglasses", "META GLASSES")
        addDotRow("filter_type_djidrone", "DJI DRONE")
        addDotRow("filter_type_parrotdrone", "PARROT DRONE")
        addDotRow("filter_type_skydiodrone", "SKYDIO DRONE")
        addDotRow("filter_type_auteldrone", "AUTEL DRONE")
        addDotRow("filter_type_remoteiddrone", "REMOTE ID DRONE")
        addDotRow("filter_type_axon", "AXON")
        addDotRow("filter_type_flock", "FLOCK")
    }

    private fun addSection(label: String) {
        val tv = TextView(this).apply {
            text = label
            textSize = 14f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            setTextColor(themeColor(this@FilterModeActivity))
            gravity = android.view.Gravity.CENTER
        textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            setPadding(0, dp(12), 0, dp(8))
        }
        content.addView(tv)
    }

    private fun addNote(label: String) {
        val tv = TextView(this).apply {
            text = label
            textSize = 12f
            typeface = Typeface.MONOSPACE
            setTextColor(0xFFFFE0C0.toInt())
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, dp(8))
        }
        content.addView(tv)
    }

    

    private fun addDotRow(key: String, label: String) {
        val enabled = prefs.getBoolean(key, true)

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = GradientDrawable().apply {
                setColor(0x22111111)
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), themeColor(this@FilterModeActivity))
            }
        }

        val labelView = TextView(this).apply {
            text = label
            textSize = 14f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            setTextColor(0xFFFFF1E0.toInt())
            gravity = android.view.Gravity.CENTER
            textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val dotView = TextView(this).apply {
            val h = dp(22)
            val onColor = themeColor(this@FilterModeActivity)
            val offColor = 0xFF444444.toInt()
            layoutParams = LinearLayout.LayoutParams(dp(48), h)
            text = if (enabled) "ON" else "OFF"
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            setTextColor(
                if (enabled && isColorLight(onColor)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            )
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = h / 2f
                setColor(if (enabled) onColor else offColor)
            }
        }

        val leftSpacer = android.view.View(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(48), dp(22))
        }

        row.addView(leftSpacer)
        row.addView(labelView)
        row.addView(dotView)

        row.setOnClickListener {
            prefs.edit().putBoolean(key, !enabled).apply()
            render()
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, dp(4), 0, dp(4))
        }
        content.addView(row, params)
    }

    private fun isColorLight(color: Int): Boolean {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        val luminance = (0.299 * r) + (0.587 * g) + (0.114 * b)
        return luminance >= 186
    }

    private fun buildHellButton(label: String) = Button(this).apply {
        text = label
        gravity = android.view.Gravity.CENTER
        textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
        textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
        gravity = android.view.Gravity.CENTER
        textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
        isAllCaps = true
        textSize = 13f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setTextColor(0xFFFFF1E0.toInt())
            gravity = android.view.Gravity.CENTER
        background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(0xFF6A0000.toInt(), 0xFF260000.toInt())
        ).apply {
            cornerRadius = dp(18).toFloat()
            setStroke(dp(1), themeColor(this@FilterModeActivity))
        }
        setPadding(dp(10), dp(14), dp(10), dp(14))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
