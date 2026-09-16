package com.tplus.tesla.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.view.View

class BlackMaskView(context: Context) : View(context) {

    private val blackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.TRANSPARENT
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        val layer = canvas.saveLayer(0f, 0f, w, h, null)

        canvas.drawRect(0f, 0f, w, h, blackPaint)

        val oval = RectF(
            w * 0.18f,
            h * 0.19f,
            w * 0.82f,
            h * 0.79f
        )

        canvas.drawOval(oval, clearPaint)

        canvas.restoreToCount(layer)

        clearPaint.xfermode = null
    }
}
