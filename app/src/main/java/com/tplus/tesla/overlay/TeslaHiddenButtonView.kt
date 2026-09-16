package com.tplus.tesla.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.abs
import kotlin.math.hypot

class TeslaHiddenButtonView(
    context: Context,
    private val onDoubleTap: () -> Unit
) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        isDither = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private var windowManager: WindowManager? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var downTime = 0L
    private var lastTapTime = 0L

    private var downRawX = 0f
    private var downRawY = 0f

    private var startX = 0
    private var startY = 0

    private var dragging = false

    fun setWindowManager(
        manager: WindowManager?,
        params: WindowManager.LayoutParams
    ) {
        windowManager = manager
        layoutParams = params
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f

        val radius = minOf(width, height) * 0.32f

        // Soft dark glass background.
        paint.style = Paint.Style.FILL
        paint.color = 0xB0081014.toInt()
        canvas.drawCircle(cx, cy, radius, paint)

        // Neon outer ring.
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.5f)
        paint.color = 0xFF18D9FF.toInt()
        paint.alpha = 235

        canvas.drawCircle(cx, cy, radius, paint)

        // Simplified Tesla-like vehicle silhouette.
        val car = Path()

        val left = cx - radius * 0.62f
        val right = cx + radius * 0.62f
        val top = cy - radius * 0.24f
        val bottom = cy + radius * 0.28f

        car.moveTo(left, bottom)
        car.lineTo(left + radius * 0.16f, top)
        car.lineTo(left + radius * 0.32f, top - radius * 0.16f)
        car.lineTo(right - radius * 0.30f, top - radius * 0.16f)
        car.lineTo(right - radius * 0.12f, top)
        car.lineTo(right, bottom)

        car.lineTo(right - radius * 0.08f, bottom + radius * 0.12f)
        car.lineTo(left + radius * 0.08f, bottom + radius * 0.12f)
        car.close()

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2f)
        paint.color = 0xFF18D9FF.toInt()
        paint.alpha = 255

        canvas.drawPath(car, paint)

        // Wheels.
        paint.style = Paint.Style.FILL
        paint.color = 0xFF18D9FF.toInt()

        canvas.drawCircle(
            left + radius * 0.20f,
            bottom + radius * 0.04f,
            radius * 0.08f,
            paint
        )

        canvas.drawCircle(
            right - radius * 0.20f,
            bottom + radius * 0.04f,
            radius * 0.08f,
            paint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {

                downTime = SystemClock.uptimeMillis()

                downRawX = event.rawX
                downRawY = event.rawY

                layoutParams?.let {
                    startX = it.x
                    startY = it.y
                }

                dragging = false

                return true
            }

            MotionEvent.ACTION_MOVE -> {

                val dx = event.rawX - downRawX
                val dy = event.rawY - downRawY

                val distance = hypot(dx.toDouble(), dy.toDouble())

                val heldTime =
                    SystemClock.uptimeMillis() - downTime

                /*
                 * Drag is intentionally locked until the button
                 * has been held for approximately 600 ms.
                 */
                if (!dragging &&
                    heldTime >= 600L &&
                    distance >= dp(6f)
                ) {
                    dragging = true
                }

                if (dragging) {

                    layoutParams?.let { params ->

                        params.x = startX + dx.toInt()
                        params.y = startY + dy.toInt()

                        runCatching {
                            windowManager?.updateViewLayout(
                                this,
                                params
                            )
                        }
                    }
                }

                return true
            }

            MotionEvent.ACTION_UP -> {

                val now = SystemClock.uptimeMillis()

                if (dragging) {

                    layoutParams?.let {
                        val service =
                            context as? TplusOverlayService

                        service?.saveHiddenButtonPosition(
                            it.x,
                            it.y
                        )
                    }

                    dragging = false
                    lastTapTime = 0L

                    return true
                }

                val dx = event.rawX - downRawX
                val dy = event.rawY - downRawY

                val distance = hypot(
                    dx.toDouble(),
                    dy.toDouble()
                )

                val pressDuration = now - downTime

                /*
                 * Only a short stationary tap participates in
                 * double-tap detection.
                 */
                if (pressDuration < 500L &&
                    distance < dp(12f)
                ) {

                    if (lastTapTime != 0L &&
                        now - lastTapTime <= 300L
                    ) {
                        lastTapTime = 0L
                        onDoubleTap()
                    } else {
                        /*
                         * Single tap intentionally does nothing.
                         */
                        lastTapTime = now
                    }
                } else {
                    lastTapTime = 0L
                }

                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                dragging = false
                lastTapTime = 0L
                return true
            }
        }

        return true
    }

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density
}
