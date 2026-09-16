package com.tplus.tesla.overlay

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager

class TplusOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var hudView: HudView? = null
    private var blackMaskView: BlackMaskView? = null
    private var hiddenButton: TeslaHiddenButtonView? = null

    private val prefs by lazy {
        getSharedPreferences("tplus_hidden_mode", MODE_PRIVATE)
    }

    override fun onCreate() {
        super.onCreate()

        if (!Settings.canDrawOverlays(this)) {
            getSharedPreferences("tplus_hidden_mode", MODE_PRIVATE)
                .edit()
                .putBoolean("hud_started", false)
                .apply()
            stopSelf()
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        showHud()
    }

    private fun createHud() {
        if (hudView != null) return

        hudView = HudView(this) {
            hideHud()
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }

        runCatching {
            windowManager?.addView(hudView, params)
            getSharedPreferences("tplus_hidden_mode", MODE_PRIVATE)
                .edit()
                .putBoolean("hud_started", true)
                .apply()
        }.onFailure {
            getSharedPreferences("tplus_hidden_mode", MODE_PRIVATE)
                .edit()
                .putBoolean("hud_started", false)
                .apply()
            hudView = null
        }
    }

    private fun showHud() {
        removeHiddenButton()
        createHud()
    }

    private fun hideHud() {
        removeHud()
        showHiddenButton()
    }

    private fun removeHud() {
        hudView?.let { view ->
            runCatching {
                windowManager?.removeView(view)
            }
        }

        hudView = null
    }

    private fun showHiddenButton() {
        if (hiddenButton != null) return

        val button = TeslaHiddenButtonView(
            context = this,
            onDoubleTap = {
                showHud()
            }
        )

        hiddenButton = button

        val size = dp(64f)

        val defaultX = dp(24f)
        val defaultY = dp(120f)

        val savedX = prefs.getInt("button_x", -1)
        val savedY = prefs.getInt("button_y", -1)

        val displayWidth = resources.displayMetrics.widthPixels
        val displayHeight = resources.displayMetrics.heightPixels

        val x = if (savedX >= 0) {
            savedX
        } else {
            (displayWidth - size - defaultX).toInt()
        }

        val y = if (savedY >= 0) {
            savedY
        } else {
            (displayHeight - size - defaultY).toInt()
        }

        val params = WindowManager.LayoutParams(
            size.toInt(),
            size.toInt(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            this.x = x.coerceAtLeast(0)
            this.y = y.coerceAtLeast(0)
        }

        button.setWindowManager(windowManager, params)

        runCatching {
            windowManager?.addView(button, params)
        }
    }

    fun saveHiddenButtonPosition(x: Int, y: Int) {
        prefs.edit()
            .putInt("button_x", x)
            .putInt("button_y", y)
            .apply()
    }

    fun resetHiddenButtonPosition() {
        prefs.edit()
            .remove("button_x")
            .remove("button_y")
            .apply()

        hiddenButton?.let {
            runCatching {
                windowManager?.removeView(it)
            }

            hiddenButton = null
        }

        showHiddenButton()
    }

    private fun removeHiddenButton() {
        hiddenButton?.let { button ->
            runCatching {
                windowManager?.removeView(button)
            }
        }

        hiddenButton = null
    }

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density

    override fun onDestroy() {
        removeHud()
        removeHiddenButton()

        windowManager = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}






