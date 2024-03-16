package com.sd.customtime.ui

import android.animation.ValueAnimator
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
    private var lineWidth = AndroidUtils.dp(context, 7F).toFloat() //толщина линии
    private lateinit var bitmap: Bitmap

    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            bitmap = BitmapFactory.decodeResource(
                resources,
            R.drawable.watch_round
            )
        }
    }

    private val paintHours = Paint().apply {
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
    }
    private val paintMin = Paint().apply {
        strokeWidth = lineWidth / 2f
        strokeCap = Paint.Cap.ROUND
    }
    private val paintSec = Paint().apply {
        strokeWidth = lineWidth / 4f
        strokeCap = Paint.Cap.ROUND
        color = Color.RED
    }

    private val listPaint: List<Paint> = listOf(paintHours, paintMin, paintSec)
    private val dataRad: List<Float> = listOf(0.55f, 0.75f, 0.8f)

    var data: List<Double> = emptyList()
        set(value) {
            field = value
              update()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawBitmap(
            bitmap,
            0f,
            0f,
            paintHours
        )
            drawTime(canvas, data)
    }

    private fun drawTime(canvas: Canvas?, listAngle: List<Double>) {
        for (i in 0..2) {
            val angle = when (i) {
                0 -> {
                    (listAngle[i] * 30.0 + listAngle[i + 1] * 0.5 + listAngle[i + 2] * 0.0083)
                }

                1 -> {
                    (listAngle[i] * 6.0 + listAngle[i + 1] * 0.1)
                }

                2 -> {
                    (listAngle[i] * 6.0)
                }

                else -> {0.0}
            } -90.0

            var x1 = ((radius*0.2f)*cos(Math.toRadians(angle-180))+center.x).toFloat()
            var y1 = ((radius*0.2f)*sin(Math.toRadians(angle-180))+center.y).toFloat()
            var x2 = ((radius * dataRad[i]) * cos(Math.toRadians(angle)) + center.x).toFloat()
            var y2 = ((radius * dataRad[i]) * sin(Math.toRadians(angle)) + center.y).toFloat()
            if(i==2) {
                 x1 = ((radius*0.2f)*cos(Math.toRadians(angle-180+progress.toDouble()))+center.x).toFloat()
                 y1 = ((radius*0.2f)*sin(Math.toRadians(angle-180+progress.toDouble()))+center.y).toFloat()
                 x2 = ((radius * dataRad[i]) * cos(Math.toRadians(angle+progress.toDouble())) + center.x).toFloat()
                 y2 = ((radius * dataRad[i]) * sin(Math.toRadians(angle+progress.toDouble())) + center.y).toFloat()
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

        valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                     progress = anim.animatedValue as Float

                if (progress==1f) {
                    val dataNew = data as MutableList
                    val amount = (dataNew[0]*3600+dataNew[1]*60+dataNew[2]).toInt() +1
                    val h = (amount/3600)
                    val m = ((amount-h*3600)/60)
                    val s = (amount-h*3600-m*60)%60
                    dataNew[0]= h.toDouble()
                    dataNew[1] = m.toDouble()
                    dataNew[2] = s.toDouble()
                    data = dataNew
                }
                invalidate()
            }
            duration = 1000
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }
}