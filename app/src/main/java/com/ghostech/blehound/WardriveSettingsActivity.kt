package com.ghostech.blehound

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class WardriveSettingsActivity : Activity() {

    private lateinit var apiNameField: EditText
    private lateinit var apiTokenField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("blehound_prefs", MODE_PRIVATE)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0B0B0C.toInt())
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(12))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xFF1C1C20.toInt(), 0xFF151517.toInt(), 0xFF0B0B0C.toInt())
            ).apply { setStroke(dp(1), themeColor(this@WardriveSettingsActivity)) }
        }
        header.addView(TextView(this).apply {
            text = "WARDRIVE / WIGLE"
            gravity = Gravity.CENTER
            textSize = 20f
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD_ITALIC)
            setTextColor(themeColor(this@WardriveSettingsActivity))
            setShadowLayer(12f, 0f, 0f, 0xFFFFB300.toInt())
        })
        header.addView(TextView(this).apply {
            text = "Auto-upload wardrive sessions to wigle.net"
            gravity = Gravity.CENTER
            textSize = 11f
            setTextColor(0xFF666666.toInt())
            setPadding(0, dp(4), 0, 0)
        })

        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        content.addView(buildLabel("WIGLE API NAME"))
        apiNameField = buildField("API Name (from wigle.net profile)")
        apiNameField.setText(prefs.getString(WigleUploader.PREF_API_NAME, ""))
        content.addView(apiNameField)

        content.addView(buildLabel("WIGLE API TOKEN"))
        apiTokenField = buildField("Token (from wigle.net profile)")
        apiTokenField.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        apiTokenField.setText(prefs.getString(WigleUploader.PREF_API_TOKEN, ""))
        content.addView(apiTokenField)

        content.addView(buildHint("Get credentials at wigle.net > My Account > API Token"))

        scroll.addView(content)

        val btnContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), 0, dp(16), dp(20))
        }
        btnContainer.addView(buildHellButton("SAVE").apply {
            setOnClickListener {
                prefs.edit()
                    .putString(WigleUploader.PREF_API_NAME,  apiNameField.text.toString().trim())
                    .putString(WigleUploader.PREF_API_TOKEN, apiTokenField.text.toString().trim())
                    .apply()
                finish()
            }
        })
        btnContainer.addView(buildHellButton("CLEAR CREDENTIALS").apply {
            setOnClickListener {
                prefs.edit()
                    .remove(WigleUploader.PREF_API_NAME)
                    .remove(WigleUploader.PREF_API_TOKEN)
                    .apply()
                apiNameField.setText("")
                apiTokenField.setText("")
            }
        })
        btnContainer.addView(buildHellButton("BACK").apply {
            setOnClickListener { finish() }
        })

        root.addView(header)
        root.addView(scroll)
        root.addView(btnContainer)
        setContentView(root)
    }

    private fun buildLabel(text: String) = TextView(this).apply {
        this.text = text
        textSize = 11f
        setTextColor(0xFF888888.toInt())
        typeface = Typeface.MONOSPACE
        setPadding(dp(4), dp(14), dp(4), dp(4))
        isAllCaps = true
    }

    private fun buildHint(text: String) = TextView(this).apply {
        this.text = text
        textSize = 10f
        setTextColor(0xFF444444.toInt())
        typeface = Typeface.MONOSPACE
        setPadding(dp(4), dp(8), dp(4), 0)
    }

    private fun buildField(hint: String) = EditText(this).apply {
        this.hint = hint
        setHintTextColor(0xFF333333.toInt())
        setTextColor(0xFFFFB300.toInt())
        textSize = 13f
        typeface = Typeface.MONOSPACE
        setBackgroundColor(0xFF0D0D0D.toInt())
        setPadding(dp(12), dp(10), dp(12), dp(10))
        inputType = InputType.TYPE_CLASS_TEXT
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(2) }
    }

    private fun buildHellButton(label: String) = Button(this).apply {
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
            setStroke(dp(1), themeColor(this@WardriveSettingsActivity))
        }
        setPadding(dp(10), dp(14), dp(10), dp(14))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(8) }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
