package com.sd.customtime.custom

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import com.sd.customtime.R
import com.sd.customtime.utils.AndroidUtils
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class TimePaintView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var lineWidth =
        AndroidUtils.dp(context, 7F).toFloat() //толщина стрелок и окантовки часов
    private var fontSize = AndroidUtils.dp(context, 30F).toFloat() //размер шрифта
    private var colorHourMin = Color.BLACK // цвет часовой и минутной стрелки
    private var colorSec = Color.RED // цвет секундной стрелки
    private var textColor = Color.BLACK // цвет чисел
    private var watchColor = Color.WHITE // цвет циферблата
    private var watchColorEdging = Color.BLACK //цвет окантовки часов

    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.TimePaintView) {
            lineWidth = getDimension(R.styleable.TimePaintView_lineWidthPaint, lineWidth)
            fontSize = getDimension(R.styleable.TimePaintView_fontSizePaint, fontSize)
            colorHourMin = getColor(R.styleable.TimePaintView_colorHourMinPaint, colorHourMin)
            colorSec = getColor(R.styleable.TimePaintView_colorSecPaint, colorSec)
            textColor = getColor(R.styleable.TimePaintView_textColorPaint, textColor)
            watchColor = getColor(R.styleable.TimePaintView_watchColorPaint, watchColor)
            watchColorEdging =
                getColor(R.styleable.TimePaintView_watchColorEdging, watchColorEdging)
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
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
        color = textColor
    }

    private val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = lineWidth
        color = watchColor
    }
    private val paintCircleEdging = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        color = watchColorEdging
    }
    private val paintSmallPoint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = lineWidth / 2
        strokeCap = Paint.Cap.ROUND
        color = watchColorEdging
    }
    private val paintBigPoint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        color = watchColorEdging
    }
    private val paintCenter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = lineWidth * 2
        strokeCap = Paint.Cap.ROUND
        color = colorHourMin
    }

    private val listPaint: List<Paint> = listOf(paintHours, paintMin, paintSec)
    private val dataRadius: List<Float> = listOf(0.45f, 0.6f, 0.8f)

    var data: List<Double> = emptyList()
        @SuppressLint("SuspiciousIndentation")
        set(value) {
            field = value
            update()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(
            center.x,
            center.y,
            radius,
            paintCircle
        )
        canvas.drawCircle(
            center.x,
            center.y,
            radius,
            paintCircleEdging
        )
        drawPointAndNumber(canvas)
        canvas.drawPoint(center.x, center.y, paintCenter)
        drawTime(canvas)
    }

    private fun drawPointAndNumber(canvas: Canvas) {
        for (i in 1..60) {
            val x =
                ((radius * 0.9f) * cos(Math.toRadians(((i * 6) - 90).toDouble())) + center.x).toFloat()
            val y =
                ((radius * 0.9f) * sin(Math.toRadians(((i * 6) - 90).toDouble())) + center.y).toFloat()

            if (i % 5 == 0) {
                canvas.drawPoint(x, y, paintBigPoint)

                val text = (i / 5).toString()
                val xT =
                    ((radius * 0.73f) * cos(Math.toRadians(((i * 6) - 90).toDouble())) + center.x).toFloat()
                val yT =
                    ((radius * 0.73f) * sin(Math.toRadians(((i * 6) - 90).toDouble())) + center.y).toFloat()
                val mTextBoundRect = Rect()
                paintText.getTextBounds(text, 0, text.length, mTextBoundRect)
                val textHeight = mTextBoundRect.height()

                canvas.drawText(
                    text,
                    xT,
                    yT + (textHeight / 2f),
                    paintText
                )
            } else {
                canvas.drawPoint(x, y, paintSmallPoint)
            }
        }
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