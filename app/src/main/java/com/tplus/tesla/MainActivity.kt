package com.tplus.tesla

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.tplus.tesla.overlay.TplusOverlayService

class MainActivity : AppCompatActivity() {

    private fun hideSystemBars() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )

        window.decorView.setBackgroundColor(
            Color.TRANSPARENT
        )

        window.clearFlags(
            WindowManager.LayoutParams.FLAG_DIM_BEHIND
        )

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        hideSystemBars()

        // Overlay permission is required before the Activity can hand control
        // to the overlay service. Do NOT close the Activity when permission
        // is missing: otherwise TPLUS appears to launch and immediately vanish.
        if (!Settings.canDrawOverlays(this)) {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
            return
        }

        val servicePrefs =
            getSharedPreferences("tplus_hidden_mode", MODE_PRIVATE)

        // Reset the launch marker. The service sets it to true only after
        // WindowManager successfully adds the HUD overlay.
        servicePrefs.edit()
            .putBoolean("hud_started", false)
            .apply()

        startService(
            Intent(
                this,
                TplusOverlayService::class.java
            )
        )

        // Give the service a short moment to create the overlay. Only then
        // remove MainActivity. If overlay creation fails, keep the Activity
        // alive so the failure is observable instead of silently closing TPLUS.
        window.decorView.postDelayed({
            if (!isFinishing && servicePrefs.getBoolean("hud_started", false)) {
                finishAndRemoveTask()
            }
        }, 500L)
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            hideSystemBars()
        }
    }
}