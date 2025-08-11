package tech.airobotics.dd_receiver.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class PolarCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private val radiusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val lastRadiusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private var cx = 0f
    private var cy = 0f
    private var r = 0f

    // Храним список углов (в градусах)
    private val angles = mutableListOf<Float>()

    fun setCircleColor(color: Int) {
        circlePaint.color = color
        invalidate()
    }

    fun setRadiusColor(color: Int) {
        radiusPaint.color = color
        invalidate()
    }

    fun setLastRadiusColor(color: Int) {
        lastRadiusPaint.color = color
        invalidate()
    }

    /**
     * Добавляет радиус для угла [0..360]. Избыточные значения нормализуются по модулю 360.
     * 0° направлен вправо, углы растут по часовой стрелке.
     */
    fun addRadius(angleDeg: Float) {
        val norm = ((angleDeg % 360f) + 360f) % 360f
        angles += norm
        invalidate()
    }

    /** Очищает все нарисованные радиусы. */
    fun clean() {
        angles.clear()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val pw = w - paddingLeft - paddingRight
        val ph = h - paddingTop - paddingBottom
        r = min(pw, ph) * 0.45f
        cx = paddingLeft + pw / 2f
        cy = paddingTop + ph / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Круг
        canvas.drawCircle(cx, cy, r, circlePaint)

        // Радиусы
        for (i in 0 until angles.size) {
            val a = Math.toRadians(angles[i].toDouble())
            val x = cx + r * sin(a).toFloat()
            val y = cy - r * cos(a).toFloat()
            val paint = if (i == angles.lastIndex) lastRadiusPaint else radiusPaint
            canvas.drawLine(cx, cy, x, y, paint)
        }
    }

}
