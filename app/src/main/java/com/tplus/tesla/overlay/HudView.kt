package com.tplus.tesla.overlay


import com.tplus.tesla.R
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

class HudView(
    context: Context,
    private val onHideRequested: () -> Unit
) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isSubpixelText = true
    }

    private val prefs =
        context.getSharedPreferences("tplus_display", Context.MODE_PRIVATE)

    private var downX = 0f
    private var downY = 0f
    private var animationStart = SystemClock.uptimeMillis()

    private val preferenceListener =
        android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            animationStart = SystemClock.uptimeMillis()
            invalidate()
        }

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        prefs.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    override fun onDetachedFromWindow() {
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceListener)
        super.onDetachedFromWindow()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                return true
            }

            MotionEvent.ACTION_UP -> {
                val moved = hypot(
                    (event.x - downX).toDouble(),
                    (event.y - downY).toDouble()
                )

                if (moved < dp(20f)) {
                    val w = width.toFloat()
                    val h = height.toFloat()

                    if (
                        event.x >= w * 0.22f &&
                        event.x <= w * 0.78f &&
                        event.y >= h * 0.10f &&
                        event.y <= h * 0.80f
                    ) {
                        onHideRequested()
                    }
                }

                return true
            }
        }

        return true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        applyImmersiveMode()
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasWindowFocus) applyImmersiveMode()
    }

    private fun applyImmersiveMode() {
        systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

            private fun drawBlackMapMask(canvas: Canvas, w: Float, h: Float) {
        val layer = canvas.saveLayer(0f, 0f, w, h, null)

        // =========================================================
        // TPLUS ORGANIC / RAGGED MAP WINDOW
        // =========================================================
        // Большое вертикальное окно почти от верхнего до нижнего
        // физического края дисплея.
        // Край намеренно неровный и мягко размытый.
        // =========================================================

        val blackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        blackPaint.color = Color.BLACK
        blackPaint.style = Paint.Style.FILL

        // Полностью чёрный внешний cockpit.
        canvas.drawRect(0f, 0f, w, h, blackPaint)

        // ---------------------------------------------------------
        // ORGANIC OVAL PATH
        // ---------------------------------------------------------

        val cx = w * 0.50f
        val cy = h * 0.50f

        val rx = w * 0.325f
        val ry = h * 0.488f

        val path = android.graphics.Path()

        // Верхняя точка.
        path.moveTo(
            cx + w * 0.004f,
            cy - ry
        )

        // Верх -> право.
        path.cubicTo(
            cx + rx * 0.38f,
            cy - ry * 0.995f,
            cx + rx * 0.78f,
            cy - ry * 0.93f,
            cx + rx * 0.96f,
            cy - ry * 0.68f
        )

        // Правая верхняя зона.
        path.cubicTo(
            cx + rx * 1.015f,
            cy - ry * 0.42f,
            cx + rx * 0.985f,
            cy - ry * 0.18f,
            cx + rx * 1.005f,
            cy + ry * 0.03f
        )

        // Правая средняя зона — рваный край.
        path.cubicTo(
            cx + rx * 0.97f,
            cy + ry * 0.20f,
            cx + rx * 1.025f,
            cy + ry * 0.34f,
            cx + rx * 0.955f,
            cy + ry * 0.52f
        )

        // Правая нижняя зона.
        path.cubicTo(
            cx + rx * 0.91f,
            cy + ry * 0.73f,
            cx + rx * 0.67f,
            cy + ry * 0.91f,
            cx + rx * 0.36f,
            cy + ry * 0.985f
        )

        // Низ.
        path.cubicTo(
            cx + rx * 0.10f,
            cy + ry * 1.01f,
            cx - rx * 0.16f,
            cy + ry * 0.995f,
            cx - rx * 0.40f,
            cy + ry * 0.975f
        )

        // Левая нижняя зона.
        path.cubicTo(
            cx - rx * 0.70f,
            cy + ry * 0.92f,
            cx - rx * 0.92f,
            cy + ry * 0.72f,
            cx - rx * 0.98f,
            cy + ry * 0.51f
        )

        // Левая средняя зона — рваный край.
        path.cubicTo(
            cx - rx * 1.025f,
            cy + ry * 0.34f,
            cx - rx * 0.965f,
            cy + ry * 0.20f,
            cx - rx * 1.005f,
            cy + ry * 0.02f
        )

        path.cubicTo(
            cx - rx * 0.98f,
            cy - ry * 0.18f,
            cx - rx * 1.015f,
            cy - ry * 0.40f,
            cx - rx * 0.955f,
            cy - ry * 0.67f
        )

        // Левая верхняя зона.
        path.cubicTo(
            cx - rx * 0.89f,
            cy - ry * 0.88f,
            cx - rx * 0.63f,
            cy - ry * 0.975f,
            cx - rx * 0.35f,
            cy - ry * 0.99f
        )

        path.cubicTo(
            cx - rx * 0.12f,
            cy - ry * 1.005f,
            cx - rx * 0.04f,
            cy - ry * 0.995f,
            cx + w * 0.004f,
            cy - ry
        )

        path.close()

        // ---------------------------------------------------------
        // SOFT / SMOKY TRANSPARENT EDGE
        // ---------------------------------------------------------

        val softPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        softPaint.style = Paint.Style.FILL
        softPaint.xfermode = android.graphics.PorterDuffXfermode(
            android.graphics.PorterDuff.Mode.DST_OUT
        )

        softPaint.maskFilter = android.graphics.BlurMaskFilter(
            w * 0.018f,
            android.graphics.BlurMaskFilter.Blur.NORMAL
        )

        canvas.drawPath(path, softPaint)

        // ---------------------------------------------------------
        // CLEAN INNER TRANSPARENT CORE
        // ---------------------------------------------------------

        softPaint.maskFilter = null

        canvas.drawPath(path, softPaint)

        softPaint.xfermode = null

        canvas.restoreToCount(layer)
    }
override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val rawW = width.toFloat()
        val rawH = height.toFloat()

        canvas.save()

        if (rawW < rawH) {
            canvas.rotate(90f)
            canvas.translate(0f, -rawW)
            val w = rawH
            val h = rawW
            drawHud(canvas, w, h)
        } else {
            val w = rawW
            val h = rawH
            drawHud(canvas, w, h)
        }

        canvas.restore()
    }
private var tplusOverlayBitmap: android.graphics.Bitmap? = null
private fun drawHud(canvas: Canvas, w: Float, h: Float) {

        val mode = prefs.getString("mode", "AUTO") ?: "AUTO"
        val brightness =
            prefs.getInt("brightness", 70).coerceIn(0, 100) / 100f

        val neon =
            prefs.getInt("neon", 50).coerceIn(0, 100) / 100f

        val scale =
            prefs.getInt("scale", 100).coerceIn(80, 120) / 100f

        val night = when (mode) {
            "NIGHT" -> true
            "DAY" -> false
            else ->
                (resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES
        }

        val ui = brightness * if (night) 0.78f else 1f
        val glow = neon * if (night) 0.72f else 1f

        val launch = if (!animationsEnabled()) {
            1f
        } else {
            ((SystemClock.uptimeMillis() - animationStart) / 650f)
                .coerceIn(0f, 1f)
        }

        canvas.save()
        canvas.scale(scale, scale, w / 2f, h / 2f)

        val cyan = Color.rgb(20, 190, 255)
        val blue = Color.rgb(0, 100, 255)
        val white = Color.WHITE
        val gray = Color.rgb(175, 185, 192)
        val green = Color.rgb(0, 245, 135)
        val red = Color.rgb(255, 55, 55)

        // =========================================================
        // FULL BLACK COCKPIT + TRANSPARENT MAP WINDOW
        // =========================================================

        // =========================================================
        // TPLUS APPROVED PNG HUD OVERLAY
        // =========================================================
        //
        // Центральная область PNG прозрачная.
        // Под ней остаётся реальная карта.
        //
        // PNG масштабируется на полный фактический экран:
        // телефон -> весь экран
        // планшет -> весь экран
        //
        // Старые cockpit/mask/speedometer/power/neon
        // здесь НЕ вызываются.
        // =========================================================

        canvas.drawColor(
            Color.TRANSPARENT,
            PorterDuff.Mode.CLEAR
        )

        if (
            tplusOverlayBitmap == null ||
            tplusOverlayBitmap!!.isRecycled
        ) {
            tplusOverlayBitmap =
                android.graphics.BitmapFactory.decodeResource(
                    resources,
                    R.drawable.tplus_overlay
                )
        }

        val overlayBitmap = tplusOverlayBitmap

        if (overlayBitmap != null) {

            val overlayPaint = android.graphics.Paint(
                android.graphics.Paint.ANTI_ALIAS_FLAG or
                    android.graphics.Paint.FILTER_BITMAP_FLAG
            )

            val destination = android.graphics.RectF(
                0f,
                0f,
                w,
                h
            )

            canvas.drawBitmap(
                overlayBitmap,
                null,
                destination,
                overlayPaint
            )
        }
        canvas.restore()
    }

    private fun drawCockpitBackground(
        canvas: Canvas,
        w: Float,
        h: Float,
        a: Float
    ) {
        /*
         * V6 FINAL COCKPIT MASK
         *
         * Central navigation window = approximately 35% of screen width.
         * Outside that window the HUD is opaque black.
         * The transition toward the map is feathered.
         */

        canvas.drawColor(
            Color.TRANSPARENT,
            PorterDuff.Mode.CLEAR
        )

        val mapLeft = w * 0.325f
        val mapRight = w * 0.675f

        val mapTop = h * 0.012f
        val mapBottom = h * 0.988f

        val fadeX = w * 0.075f
        val fadeY = h * 0.105f

        // ---------------------------------------------------------
        // SOLID BLACK LEFT SIDE
        // ---------------------------------------------------------

        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = Color.BLACK
        paint.alpha = alpha(255, a)

        canvas.drawRect(
            0f,
            0f,
            mapLeft - fadeX,
            h,
            paint
        )

        // ---------------------------------------------------------
        // LEFT FEATHER
        // BLACK -> TRANSPARENT
        // ---------------------------------------------------------

        paint.shader = LinearGradient(
            mapLeft - fadeX,
            0f,
            mapLeft,
            0f,
            0xFF000000.toInt(),
            0x00000000,
            Shader.TileMode.CLAMP
        )

        paint.alpha = alpha(255, a)

        canvas.drawRect(
            mapLeft - fadeX,
            0f,
            mapLeft,
            h,
            paint
        )

        // ---------------------------------------------------------
        // RIGHT FEATHER
        // TRANSPARENT -> BLACK
        // ---------------------------------------------------------

        paint.shader = LinearGradient(
            mapRight,
            0f,
            mapRight + fadeX,
            0f,
            0x00000000,
            0xFF000000.toInt(),
            Shader.TileMode.CLAMP
        )

        canvas.drawRect(
            mapRight,
            0f,
            mapRight + fadeX,
            h,
            paint
        )

        // ---------------------------------------------------------
        // SOLID BLACK RIGHT SIDE
        // ---------------------------------------------------------

        paint.shader = null
        paint.color = Color.BLACK
        paint.alpha = alpha(255, a)

        canvas.drawRect(
            mapRight + fadeX,
            0f,
            w,
            h,
            paint
        )

        // ---------------------------------------------------------
        // TOP BLACK COCKPIT MASK
        // ---------------------------------------------------------

        paint.shader = LinearGradient(
            0f,
            mapTop,
            0f,
            mapTop + fadeY,
            0xFF000000.toInt(),
            0x00000000,
            Shader.TileMode.CLAMP
        )

        canvas.drawRect(
            mapLeft,
            mapTop - fadeY,
            mapRight,
            mapTop + fadeY,
            paint
        )

        // ---------------------------------------------------------
        // BOTTOM BLACK COCKPIT MASK
        // ---------------------------------------------------------

        paint.shader = LinearGradient(
            0f,
            mapBottom - fadeY,
            0f,
            mapBottom,
            0x00000000,
            0xFF000000.toInt(),
            Shader.TileMode.CLAMP
        )

        canvas.drawRect(
            mapLeft,
            mapBottom - fadeY,
            mapRight,
            mapBottom + fadeY,
            paint
        )

        // Solid top region.
        paint.shader = null
        paint.color = Color.BLACK
        paint.alpha = alpha(255, a)

        canvas.drawRect(
            0f,
            0f,
            w,
            mapTop - fadeY,
            paint
        )

        // Solid bottom region.
        canvas.drawRect(
            0f,
            mapBottom + fadeY,
            w,
            h,
            paint
        )

        paint.shader = null
    }

    private fun drawSpeedometer(
        canvas: Canvas,
        w: Float,
        h: Float,
        a: Float,
        glow: Float,
        cyan: Int,
        blue: Int,
        white: Int,
        gray: Int,
        red: Int
    ) {
        val cx = w * 0.175f
        val cy = h * 0.500f
        val r = min(
            w * 0.175f,
            h * 0.315f
        )

        drawGaugeBase(
            canvas,
            cx,
            cy,
            r,
            a,
            glow,
            cyan,
            blue
        )

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.BUTT
        paint.strokeWidth = dp(7f)
        paint.color = blue
        paint.alpha = alpha(245, a * glow)

        canvas.drawArc(
            RectF(
                cx - r,
                cy - r,
                cx + r,
                cy + r
            ),
            140f,
            250f * (96f / 260f),
            false,
            paint
        )

        drawTicks(
            canvas,
            cx,
            cy,
            r,
            140f,
            390f,
            a,
            white,
            gray
        )
    }

    private fun drawPower(
        canvas: Canvas,
        w: Float,
        h: Float,
        a: Float,
        glow: Float,
        cyan: Int,
        blue: Int,
        white: Int,
        gray: Int,
        green: Int
    ) {
        val cx = w * 0.825f
        val cy = h * 0.500f
        val r = min(
            w * 0.175f,
            h * 0.315f
        )

        drawGaugeBase(
            canvas,
            cx,
            cy,
            r,
            a,
            glow,
            cyan,
            blue
        )

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.BUTT
        paint.strokeWidth = dp(7f)

        paint.color = blue
        paint.alpha = alpha(245, a * glow)

        canvas.drawArc(
            RectF(
                cx - r,
                cy - r,
                cx + r,
                cy + r
            ),
            -140f,
            112f,
            false,
            paint
        )

        paint.color = green
        paint.alpha = alpha(235, a * glow)

        canvas.drawArc(
            RectF(
                cx - r,
                cy - r,
                cx + r,
                cy + r
            ),
            120f,
            55f,
            false,
            paint
        )

        drawTicks(
            canvas,
            cx,
            cy,
            r,
            -140f,
            140f,
            a,
            white,
            gray
        )
    }

    private fun drawGaugeBase(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        r: Float,
        a: Float,
        glow: Float,
        cyan: Int,
        blue: Int
    ) {
        paint.style = Paint.Style.FILL
        paint.color = 0xB8071017.toInt()
        paint.alpha = alpha(255, a)

        canvas.drawCircle(
            cx,
            cy,
            r - dp(2f),
            paint
        )

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(6f)
        paint.color = blue
        paint.alpha = alpha(75, a * glow)
        paint.setShadowLayer(
            dp(12f),
            0f,
            0f,
            blue
        )

        canvas.drawCircle(
            cx,
            cy,
            r + dp(2f),
            paint
        )

        paint.clearShadowLayer()

        paint.strokeWidth = dp(2f)
        paint.color = Color.WHITE
        paint.alpha = alpha(245, a)

        canvas.drawCircle(
            cx,
            cy,
            r,
            paint
        )

        paint.strokeWidth = dp(1f)
        paint.color = cyan
        paint.alpha = alpha(180, a * glow)

        canvas.drawCircle(
            cx,
            cy,
            r - dp(12f),
            paint
        )

        canvas.drawCircle(
            cx,
            cy,
            r * 0.56f,
            paint
        )
    }

    private fun drawTicks(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        r: Float,
        start: Float,
        end: Float,
        a: Float,
        white: Int,
        gray: Int
    ) {
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.BUTT
        paint.alpha = alpha(240, a)

        for (i in 0..40) {
            val angle = Math.toRadians(
                (start + (end - start) * i / 40f).toDouble()
            )

            val outer = r - dp(5f)

            val inner =
                if (i % 5 == 0)
                    r - dp(20f)
                else
                    r - dp(12f)

            val x1 =
                cx + cos(angle).toFloat() * inner

            val y1 =
                cy + sin(angle).toFloat() * inner

            val x2 =
                cx + cos(angle).toFloat() * outer

            val y2 =
                cy + sin(angle).toFloat() * outer

            paint.strokeWidth =
                if (i % 5 == 0)
                    dp(2f)
                else
                    dp(1f)

            paint.color =
                if (i % 5 == 0)
                    white
                else
                    gray

            canvas.drawLine(
                x1,
                y1,
                x2,
                y2,
                paint
            )
        }
    }

    private fun hudEnabled(key: String): Boolean =
        prefs.getBoolean("hud_$key", true)

    private fun animationsEnabled(): Boolean =
        prefs.getBoolean("animations", true)

    private fun alpha(
        base: Int,
        multiplier: Float
    ): Int =
        (base * multiplier)
            .toInt()
            .coerceIn(0, 255)

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density

    private fun sp(value: Float): Float =
        value * resources.displayMetrics.scaledDensity
}





















