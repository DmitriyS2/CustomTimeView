package com.sd.customtime.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import com.sd.customtime.R
import com.sd.customtime.utils.AndroidUtils
import kotlin.random.Random
import kotlin.math.min

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)

    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat() //толщина линии
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat() //размер шрифта
    private var colors = emptyList<Int>()
    private var orderOfFilling = 0

    private var progress = 0F
    private var progress2 = 0F
    private var progress3 = 0F
    private var progress4 = 0F
    private var valueAnimator: ValueAnimator? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            colors = resources.getIntArray(resId).toList()
            orderOfFilling = getInteger(R.styleable.StatsView_orderOfFilling, 0)
            update()
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth //толщина линии
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius,
        )
    }

    var startFrom = -90F
    var angle = 90F

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        when (orderOfFilling) {
            0 -> {
                for (index in data.indices) {
                    paint.color = colors.getOrNull(index) ?: randomColor()
                    canvas.drawArc(oval, startFrom, angle * progress, false, paint)
                    startFrom += angle
                }
            }

            1 -> {
                if (progress != 0F) {
                    drawPart(canvas, 0, progress)
                }
                if (progress2 != 0F) {
                    drawPart(canvas, 1, progress2 - 1F)
                }
                if (progress3 != 0F) {
                    drawPart(canvas, 2, progress3 - 2F)
                }
                if (progress4 != 0F) {
                    drawPart(canvas, 3, progress4 - 3F)
                }
            }
            2 -> {
                startFrom =-45F
                angle = 45F
                for (index in data.indices) {
                    paint.color = colors.getOrNull(index) ?: randomColor()
                    canvas.drawArc(oval, startFrom, -angle * progress, false, paint)
                    canvas.drawArc(oval, startFrom, angle * progress, false, paint)
                    startFrom += 90F
                }
            }
        }

        canvas.drawText(
            "%.2f%%".format(data.sum() * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint,
        )
    }

    private fun drawPart(canvas: Canvas, number: Int, progressAnim: Float) {
        paint.color = colors.getOrNull(number) ?: randomColor()
        canvas.drawArc(
            oval,
            startFrom + (90F * number.toFloat()),
            angle * (progressAnim),
            false,
            paint
        )
    }

    private fun update() {
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        //     progress = 0F

        valueAnimator = ValueAnimator.ofFloat(0F, 4F).apply {
            addUpdateListener { anim ->
                //     progress = anim.animatedValue as Float
                if (0F < (anim.animatedValue as Float) && (anim.animatedValue as Float) <= 1F) {
                    progress = anim.animatedValue as Float
                } else if (1F < (anim.animatedValue as Float) && (anim.animatedValue as Float) <= 2F) {
                    progress2 = anim.animatedValue as Float
                } else if (2F < (anim.animatedValue as Float) && (anim.animatedValue as Float) <= 3F) {
                    progress3 = anim.animatedValue as Float
                } else if (3F < (anim.animatedValue as Float) && (anim.animatedValue as Float) <= 4F) {
                    progress4 = anim.animatedValue as Float
                }
                invalidate()
            }
            duration = 5000
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }

    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}