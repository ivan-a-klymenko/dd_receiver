package tech.airobotics.dd_receiver.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class CircleWithRadiusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.BLUE
        strokeWidth = 5f
    }

    private val radiusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 3f
    }

    private val currentAngles = mutableListOf<Float>()
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    fun setAngle(angle: Float) {
        if (angle in 0f..360f) {
            currentAngles.add(angle)
            invalidate()
        }
    }

    fun clean() {
        currentAngles.clear()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = w.coerceAtMost(h) / 2f * 0.8f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Рисуем круг
        canvas.drawCircle(centerX, centerY, radius, circlePaint)

        // Рисуем все радиусы
        currentAngles.forEach { angle ->
            // Вычитаем 90 градусов, чтобы 0° смотрел вверх
            val adjustedAngle = angle - 90f
            val radians = Math.toRadians(adjustedAngle.toDouble())
            val endX = centerX + radius * cos(radians).toFloat()
            val endY = centerY + radius * sin(radians).toFloat()
            canvas.drawLine(centerX, centerY, endX, endY, radiusPaint)
        }
    }
}