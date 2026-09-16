package com.tplus.tesla

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private val cyan = Color.rgb(0, 220, 255)
    private val white = Color.rgb(235, 240, 243)
    private val gray = Color.rgb(145, 155, 162)
    private val green = Color.rgb(70, 235, 150)
    private val bg = Color.rgb(8, 12, 15)
    private val panel = Color.rgb(18, 25, 30)
    private val panel2 = Color.rgb(25, 34, 40)
    private val border = Color.rgb(55, 70, 78)

    private lateinit var content: LinearLayout
    private lateinit var menu: LinearLayout

    private val prefs by lazy {
        getSharedPreferences("tplus_display", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK

        buildUi()
        showDisplay()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        root.addView(
            createTopBar(),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
            )
        )

        val body = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(16), 0, dp(16), 0)
        }

        menu = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(0), dp(8), dp(12), dp(4))
        }

        val menuScroll = ScrollView(this).apply {
            isFillViewport = true
            overScrollMode = View.OVER_SCROLL_NEVER
            setPadding(0, 0, 0, 0)
        }

        menuScroll.addView(
            menu,
            android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        body.addView(
            menuScroll,
            LinearLayout.LayoutParams(
                dp(190),
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )

        val divider = View(this).apply {
            setBackgroundColor(border)
        }

        body.addView(
            divider,
            LinearLayout.LayoutParams(dp(1), LinearLayout.LayoutParams.MATCH_PARENT)
        )

        val rightScroll = ScrollView(this).apply {
            isFillViewport = true
            overScrollMode = View.OVER_SCROLL_NEVER
            setPadding(dp(18), dp(8), dp(12), dp(4))
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        rightScroll.addView(
            content,
            android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        body.addView(
            rightScroll,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        root.addView(
            body,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(
            createBottomBar(),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(42)
            )
        )

        createMenu()

        setContentView(root)
    }

    private fun createTopBar(): View {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), 0, dp(18), 0)
            setBackgroundColor(Color.BLACK)
        }

        val logo = TextView(this).apply {
            text = "TPLUS"
            textSize = 20f
            setTextColor(cyan)
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }

        bar.addView(
            logo,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val subtitle = TextView(this).apply {
            text = "  TESLA DIGITAL HUD  /  SETTINGS"
            textSize = 9f
            setTextColor(gray)
        }

        bar.addView(
            subtitle,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val vehicle = TextView(this).apply {
            text = "MODEL 3  •  READY"
            textSize = 9f
            setTextColor(green)
            gravity = Gravity.CENTER_VERTICAL
        }

        bar.addView(vehicle)

        return bar
    }

    private fun createMenu() {
        val items = listOf(
            "VEHICLE",
            "NAVIGATION",
            "DISPLAY",
            "HUD",
            "AUTOMATION",
            "HIDDEN MODE",
            "MEDIA",
            "UNITS",
            "ABOUT"
        )

        items.forEach { item ->
            val row = TextView(this).apply {
                text = item
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(white)
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(14), 0, dp(8), 0)
                background = rounded(panel, 6)

                setOnClickListener {
                    selectMenu(this)
                    when (item) {
                        "DISPLAY" -> showDisplay()
                        "VEHICLE" -> showPlaceholder("VEHICLE")
                        "NAVIGATION" -> showPlaceholder("NAVIGATION")
                        "HUD" -> showHudSettings()
                        "AUTOMATION" -> showPlaceholder("AUTOMATION")
                        "HIDDEN MODE" -> showPlaceholder("HIDDEN MODE")
                        "MEDIA" -> showPlaceholder("MEDIA")
                        "UNITS" -> showPlaceholder("UNITS")
                        "ABOUT" -> showAbout()
                    }
                }
            }

            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(36)
            )
            lp.setMargins(0, dp(2), 0, dp(2))
            menu.addView(row, lp)
        }

        if (menu.childCount >= 3) {
            selectMenu(menu.getChildAt(2) as TextView)
        }
    }

    private fun selectMenu(selected: TextView) {
        for (i in 0 until menu.childCount) {
            val item = menu.getChildAt(i) as TextView
            item.setTextColor(if (item === selected) cyan else white)
            item.background = rounded(
                if (item === selected) Color.rgb(24, 42, 48) else panel,
                6
            )
        }
    }

    private fun showDisplay() {
        content.removeAllViews()

        addTitle("DISPLAY")
        addDescription("HUD appearance, brightness and visual behavior")

        addSection("MODE")

        val modeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val savedMode = prefs.getString("mode", "AUTO") ?: "AUTO"

        listOf("AUTO", "DAY", "NIGHT").forEach { mode ->
            val b = TextView(this).apply {
                text = mode
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(if (mode == savedMode) cyan else white)
                background = rounded(
                    if (mode == savedMode) Color.rgb(24, 42, 48) else panel2,
                    5
                )
                setOnClickListener {
                    prefs.edit().putString("mode", mode).apply()
                    showDisplay()
                }
            }

            val lp = LinearLayout.LayoutParams(dp(105), dp(34))
            lp.setMargins(0, 0, dp(8), 0)
            modeRow.addView(b, lp)
        }

        content.addView(
            modeRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(40)
            )
        )

        addSection("HUD BRIGHTNESS")
        addSlider(
            key = "brightness",
            min = 0,
            max = 100,
            default = 70,
            suffix = "%"
        )

        addSection("NEON INTENSITY")
        addSlider(
            key = "neon",
            min = 0,
            max = 100,
            default = 50,
            suffix = "%"
        )

        addSection("HUD SCALE")
        addSlider(
            key = "scale",
            min = 80,
            max = 120,
            default = 100,
            suffix = "%"
        )

        addSection("ANIMATIONS")

        val animationValue = prefs.getBoolean("animations", true)

        val animationRow = createSwitchRow(
            "SMOOTH HUD ANIMATIONS",
            if (animationValue) "ON" else "OFF",
            animationValue
        )

        animationRow.setOnClickListener {
            prefs.edit()
                .putBoolean("animations", !animationValue)
                .apply()
            showDisplay()
        }

        content.addView(
            animationRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(46)
            )
        )
    }

    private fun addSlider(
        key: String,
        min: Int,
        max: Int,
        default: Int,
        suffix: String
    ) {
        val value = prefs.getInt(key, default)

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val seek = SeekBar(this).apply {
            this.max = max - min
            progress = value - min
        }

        val valueText = TextView(this).apply {
            text = "$value$suffix"
            textSize = 12f
            setTextColor(white)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        row.addView(
            seek,
            LinearLayout.LayoutParams(
                0,
                dp(42),
                1f
            )
        )

        row.addView(
            valueText,
            LinearLayout.LayoutParams(dp(80), dp(42))
        )

        seek.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val actual = progress + min
                    valueText.text = "$actual$suffix"

                    if (fromUser) {
                        prefs.edit().putInt(key, actual).apply()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )

        content.addView(
            row,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(46)
            )
        )
    }

    private fun createSwitchRow(
        title: String,
        state: String,
        active: Boolean
    ): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), 0, dp(14), 0)
            background = rounded(panel2, 6)
        }

        val label = TextView(this).apply {
            text = title
            textSize = 10f
            setTextColor(white)
        }

        row.addView(
            label,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val stateText = TextView(this).apply {
            text = state
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (active) cyan else gray)
        }

        row.addView(stateText)

        return row
    }

    private fun addTitle(text: String) {
        val title = TextView(this).apply {
            this.text = text
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
        }

        content.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(36)
            )
        )
    }

    private fun addDescription(text: String) {
        val description = TextView(this).apply {
            this.text = text
            textSize = 10f
            setTextColor(gray)
        }

        content.addView(
            description,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(30)
            )
        )
    }

    private fun addSection(text: String) {
        val label = TextView(this).apply {
            this.text = text
            textSize = 9f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(cyan)
            gravity = Gravity.BOTTOM
        }

        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(30)
        )
        lp.topMargin = dp(4)

        content.addView(label, lp)
    }

    private fun showHudSettings() {
        content.removeAllViews()

        addTitle("HUD")
        addDescription("Choose which cockpit elements are visible")

        val items = listOf(
            "speedometer" to "SPEEDOMETER",
            "battery" to "BATTERY",
            "power" to "POWER METER",
            "efficiency" to "EFFICIENCY GRAPH",
            "navigation" to "NAVIGATION INFO",
            "tpms" to "TPMS",
            "bottomBar" to "BOTTOM BAR"
        )

        items.forEach { (key, title) ->
            val active = prefs.getBoolean("hud_$key", true)

            val row = createSwitchRow(
                title,
                if (active) "ON" else "OFF",
                active
            )

            row.setOnClickListener {
                prefs.edit()
                    .putBoolean("hud_$key", !prefs.getBoolean("hud_$key", true))
                    .apply()

                showHudSettings()
            }

            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(46)
            )
            lp.setMargins(0, 0, 0, dp(6))

            content.addView(row, lp)
        }

        addSection("HUD PRESET")

        val presetRow = createSwitchRow(
            "RESTORE ALL HUD ELEMENTS",
            "RESET",
            true
        )

        presetRow.setOnClickListener {
            prefs.edit()
                .putBoolean("hud_speedometer", true)
                .putBoolean("hud_battery", true)
                .putBoolean("hud_power", true)
                .putBoolean("hud_efficiency", true)
                .putBoolean("hud_navigation", true)
                .putBoolean("hud_tpms", true)
                .putBoolean("hud_bottomBar", true)
                .apply()

            showHudSettings()
        }

        content.addView(
            presetRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(46)
            )
        )
    }
    private fun showPlaceholder(titleText: String) {
        content.removeAllViews()

        addTitle(titleText)
        addDescription("Configuration section")

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(20), dp(20), dp(20))
            background = rounded(panel, 8)
        }

        val text = TextView(this).apply {
            text = "THIS SECTION WILL BE CONNECTED NEXT"
            textSize = 11f
            setTextColor(gray)
            gravity = Gravity.CENTER
        }

        card.addView(text)

        content.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(140)
            )
        )
    }

    private fun showAbout() {
        content.removeAllViews()

        addTitle("ABOUT")
        addDescription("TPLUS Tesla Digital HUD")

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(18))
            background = rounded(panel, 8)
        }

        addInfo(card, "TPLUS", "TESLA DIGITAL HUD")
        addInfo(card, "VERSION", "1.0")
        addInfo(card, "VEHICLE DATA", "OBD / CAN")
        addInfo(card, "NAVIGATION", "GOOGLE MAPS / RADARBOT")

        content.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(170)
            )
        )
    }

    private fun addInfo(parent: LinearLayout, label: String, value: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val l = TextView(this).apply {
            text = label
            textSize = 9f
            setTextColor(gray)
        }

        val v = TextView(this).apply {
            text = value
            textSize = 10f
            setTextColor(white)
            gravity = Gravity.END
        }

        row.addView(
            l,
            LinearLayout.LayoutParams(
                0,
                dp(30),
                1f
            )
        )

        row.addView(
            v,
            LinearLayout.LayoutParams(
                dp(210),
                dp(30)
            )
        )

        parent.addView(row)
    }

    private fun createBottomBar(): View {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), 0, dp(18), 0)
            setBackgroundColor(Color.BLACK)
        }

        val back = TextView(this).apply {
            text = "‹  BACK"
            textSize = 10f
            setTextColor(white)
            gravity = Gravity.CENTER_VERTICAL
            setOnClickListener { finish() }
        }

        bar.addView(
            back,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        val version = TextView(this).apply {
            text = "TPLUS 1.0  •  DIGITAL HUD"
            textSize = 9f
            setTextColor(gray)
            gravity = Gravity.CENTER_VERTICAL
        }

        bar.addView(version)

        return bar
    }

    private fun rounded(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), border)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}




