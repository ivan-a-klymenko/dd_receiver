package tech.airobotics.dd_receiver.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin

class AngleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 40f
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        strokeWidth = 3f
    }

    private var distance: Float = 0f
    private var height: Float = 0f

    fun setParams(distance: Float, height: Float) {
        this.distance = distance
        this.height = height
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerY = height / 2f
        val text = "Дистанция: ${distance}m"
        canvas.drawText(text, 10f, 30f, textPaint)

        if (distance > 0 && height > 0) {
            val angle = Math.toDegrees(atan(height / distance).toDouble())
            val angleText = "Угол: ${"%.1f".format(angle)}°"
            canvas.drawText(angleText, 10f, 70f, textPaint)

            // Рисуем линию угла
            val lineLength = width * 0.8f
            val endX = lineLength * cos(angle * Math.PI / 180).toFloat()
            val endY = centerY - lineLength * sin(angle * Math.PI / 180).toFloat()
            canvas.drawLine(50f, centerY, 50f + endX, centerY - endY, linePaint)
        }
    }
}