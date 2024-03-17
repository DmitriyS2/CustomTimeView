package com.sd.customtime.custom

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import com.sd.customtime.R
import com.sd.customtime.utils.AndroidUtils
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


class TimeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var lineWidth = AndroidUtils.dp(context, 7F).toFloat() //толщина стрелок
    private var colorHourMin = Color.BLACK // цвет часовой и минутной стрелки
    private var colorSec = Color.RED // цвет секундной стрелки
    private lateinit var bitmap: Bitmap

    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.TimeView) {
            lineWidth = getDimension(R.styleable.TimeView_lineWidth, lineWidth)
            colorHourMin = getColor(R.styleable.TimeView_colorHourMin, colorHourMin)
            colorSec = getColor(R.styleable.TimeView_colorSec, colorSec)
            bitmap = BitmapFactory.decodeResource(
                resources,
                R.drawable.watch_round
            )
        }
    }

    private val paintHours = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = lineWidth
        color = colorHourMin
        strokeCap = Paint.Cap.ROUND
    }
    private val paintMin = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = lineWidth / 2f
        color = colorHourMin
        strokeCap = Paint.Cap.ROUND
    }
    private val paintSec = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = lineWidth / 4f
        strokeCap = Paint.Cap.ROUND
        color = colorSec
    }
    private val paintCenter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = lineWidth * 2
        strokeCap = Paint.Cap.ROUND
        color = colorHourMin
    }

    private val listPaint: List<Paint> = listOf(paintHours, paintMin, paintSec)
    private val dataRadius: List<Float> = listOf(0.45f, 0.53f, 0.75f)

    var data: List<Double> = emptyList()
        @SuppressLint("SuspiciousIndentation")
        set(value) {
            field = value
            update()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F
        center = PointF(w / 2F, h / 2F)
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(
            bitmap,
            0f,
            0f,
            paintHours
        )
        canvas.drawPoint(center.x, center.y, paintCenter)
        drawTime(canvas)
    }

    private fun drawTime(canvas: Canvas?) {
        for (i in 0..2) {
            var angle: Double
            var x1 = 0f
            var x2 = 0f
            var y1 = 0f
            var y2 = 0f
            when (i) {
                0 -> {
                    angle =
                        (data[i] * 30.0 + data[i + 1] * 0.5 + data[i + 2] * 0.0083) - 90
                    x1 =
                        ((radius * 0.2f) * cos(Math.toRadians(angle - 180 + (progress.toDouble() / 3600.0))) + center.x).toFloat()
                    y1 =
                        ((radius * 0.2f) * sin(Math.toRadians(angle - 180 + (progress.toDouble() / 3600.0))) + center.y).toFloat()
                    x2 =
                        ((radius * dataRadius[i]) * cos(Math.toRadians(angle + (progress.toDouble() / 3600.0))) + center.x).toFloat()
                    y2 =
                        ((radius * dataRadius[i]) * sin(Math.toRadians(angle + (progress.toDouble() / 3600.0))) + center.y).toFloat()

                }

                1 -> {
                    angle = (data[i] * 6.0 + data[i + 1] * 0.1) - 90
                    x1 =
                        ((radius * 0.2f) * cos(Math.toRadians(angle - 180 + (progress.toDouble() / 60.0))) + center.x).toFloat()
                    y1 =
                        ((radius * 0.2f) * sin(Math.toRadians(angle - 180 + (progress.toDouble() / 60.0))) + center.y).toFloat()
                    x2 =
                        ((radius * dataRadius[i]) * cos(Math.toRadians(angle + (progress.toDouble() / 60.0))) + center.x).toFloat()
                    y2 =
                        ((radius * dataRadius[i]) * sin(Math.toRadians(angle + (progress.toDouble() / 60.0))) + center.y).toFloat()
                }

                2 -> {
                    angle = (data[i] * 6.0) - 90
                    x1 =
                        ((radius * 0.2f) * cos(Math.toRadians(angle - 180 + progress.toDouble())) + center.x).toFloat()
                    y1 =
                        ((radius * 0.2f) * sin(Math.toRadians(angle - 180 + progress.toDouble())) + center.y).toFloat()
                    x2 =
                        ((radius * dataRadius[i]) * cos(Math.toRadians(angle + progress.toDouble())) + center.x).toFloat()
                    y2 =
                        ((radius * dataRadius[i]) * sin(Math.toRadians(angle + progress.toDouble())) + center.y).toFloat()
                }
            }
            canvas?.drawLine(x1, y1, x2, y2, listPaint[i])
        }
    }

    private fun update() {
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        progress = 0F

        valueAnimator = ValueAnimator.ofFloat(0F, 360F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float

                if (progress == 360f) {
                    val dataNew = data as MutableList
                    val amount =
                        ((dataNew[0] * 3600 + dataNew[1] * 60 + dataNew[2]).toInt() + 60) % 86400 //amount всегда до 24:00
                    val h = (amount / 3600)
                    val m = ((amount - h * 3600) / 60)
                    val s = (amount - h * 3600 - m * 60) % 60
                    dataNew[0] = h.toDouble()
                    dataNew[1] = m.toDouble()
                    dataNew[2] = s.toDouble()
                    data = dataNew
                }
                invalidate()
            }
            duration = 60000
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }
}