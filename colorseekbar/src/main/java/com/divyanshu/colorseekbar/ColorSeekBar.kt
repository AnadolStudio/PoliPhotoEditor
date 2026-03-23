package com.divyanshu.colorseekbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ArrayRes

class ColorSeekBar(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private companion object {
        val DEFAULT_COLORS = intArrayOf(
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#FF4F00"),
                Color.parseColor("#FF8100"),
                Color.parseColor("#FFC300"),
                Color.parseColor("#ACFF00"),
                Color.parseColor("#00FF68"),
                Color.parseColor("#00FFB5"),
                Color.parseColor("#00F5FF"),
                Color.parseColor("#00DBFF"),
                Color.parseColor("#228BDE"),
                Color.parseColor("#008EFF"),
                Color.parseColor("#007CFF"),
                Color.parseColor("#0431FF"),
                Color.parseColor("#2D01FF"),
                Color.parseColor("#9A00FF"),
                Color.parseColor("#E000FB"),
                Color.parseColor("#FC0072"),
                Color.parseColor("#F00000"),
                Color.parseColor("#000000"),
        )
    }

    private val minThumbRadius = 16f
    private var colorSeeds = DEFAULT_COLORS
    private var canvasHeight: Int = 60
    private var barHeight: Int = 20
    private var rectf: RectF = RectF()
    private var rectPaint: Paint = Paint()
    private var thumbBorderPaint: Paint = Paint()
    private var thumbPaint: Paint = Paint().apply {
        color = DEFAULT_COLORS.first()
    }
    private lateinit var colorGradient: LinearGradient
    private var thumbX: Float = 24f
    private var thumbY: Float = (canvasHeight / 2).toFloat()
    private var thumbBorder: Float = 4f
    private var thumbRadius: Float = 16f
    private var thumbBorderRadius: Float = thumbRadius + thumbBorder
    private var thumbBorderColor = Color.BLACK
    private var paddingStart = 30f
    private var paddingEnd = 30f
    private var barCornerRadius: Float = 8f
    private var oldThumbRadius = thumbRadius
    private var oldThumbBorderRadius = thumbBorderRadius
    private var colorChangeListener: OnColorChangeListener? = null

    init {
        attributeSet.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ColorSeekBar)
            val colorsId = typedArray.getResourceId(R.styleable.ColorSeekBar_colorSeeds, 0)
            if (colorsId != 0) colorSeeds = getColorsById(colorsId)
            barCornerRadius = typedArray.getDimension(R.styleable.ColorSeekBar_cornerRadius, 8f)
            barHeight = typedArray.getDimension(R.styleable.ColorSeekBar_barHeight, 20f).toInt()
            thumbBorder = typedArray.getDimension(R.styleable.ColorSeekBar_thumbBorder, 4f)
            thumbBorderColor =
                typedArray.getColor(R.styleable.ColorSeekBar_thumbBorderColor, Color.BLACK)
            typedArray.recycle()
        }
        rectPaint.isAntiAlias = true

        thumbBorderPaint.isAntiAlias = true
        thumbBorderPaint.color = thumbBorderColor

        thumbPaint.isAntiAlias = true

        thumbRadius = (barHeight / 2).toFloat().let {
            if (it < minThumbRadius) minThumbRadius else it
        }
        thumbBorderRadius = thumbRadius + thumbBorder
        canvasHeight = (thumbBorderRadius * 3).toInt()
        thumbY = (canvasHeight / 2).toFloat()

        oldThumbRadius = thumbRadius
        oldThumbBorderRadius = thumbBorderRadius
    }

    private fun getColorsById(@ArrayRes id: Int): IntArray {
        if (isInEditMode) {
            val s = context.resources.getStringArray(id)
            val colors = IntArray(s.size)
            for (j in s.indices) {
                colors[j] = Color.parseColor(s[j])
            }
            return colors
        } else {
            val typedArray = context.resources.obtainTypedArray(id)
            val colors = IntArray(typedArray.length())
            for (j in 0 until typedArray.length()) {
                colors[j] = typedArray.getColor(j, Color.BLACK)
            }
            typedArray.recycle()
            return colors
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        //color bar position
        val barLeft: Float = paddingStart
        val barRight: Float = width.toFloat() - paddingEnd
        val barTop: Float = ((canvasHeight / 2) - (barHeight / 2)).toFloat()
        val barBottom: Float = ((canvasHeight / 2) + (barHeight / 2)).toFloat()

        //draw color bar
        rectf.set(barLeft, barTop, barRight, barBottom)
        canvas?.drawRoundRect(rectf, barCornerRadius, barCornerRadius, rectPaint)

        if (thumbX < barLeft) {
            thumbX = barLeft
        } else if (thumbX > barRight) {
            thumbX = barRight
        }
        val color = pickColor(thumbX, width)
        thumbPaint.color = color

        // draw color bar thumb
        canvas?.drawCircle(thumbX, thumbY, thumbBorderRadius, thumbBorderPaint)
        canvas?.drawCircle(thumbX, thumbY, thumbRadius, thumbPaint)
    }

    fun setColorSeed(vararg colors: Int) {
        colorSeeds = colors
        setupColorGradient(width)
        invalidate()
    }

    fun setDefaultColorSeed() = setColorSeed(*DEFAULT_COLORS)

    private fun pickColor(position: Float, canvasWidth: Int): Int {
        val value = (position - paddingStart) / (canvasWidth - (paddingStart + paddingEnd))
        when {
            value <= 0.0 -> return colorSeeds[0]
            value >= 1 -> return colorSeeds[colorSeeds.size - 1]
            else -> {
                var colorPosition = value * (colorSeeds.size - 1)
                val i = colorPosition.toInt()
                colorPosition -= i
                val c0 = colorSeeds[i]
                val c1 = colorSeeds[i + 1]

                val red = mix(Color.red(c0), Color.red(c1), colorPosition)
                val green = mix(Color.green(c0), Color.green(c1), colorPosition)
                val blue = mix(Color.blue(c0), Color.blue(c1), colorPosition)

                return Color.rgb(red, green, blue)
            }
        }
    }

    private fun mix(start: Int, end: Int, position: Float): Int {
        return start + Math.round(position * (end - start))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setupColorGradient(w)
    }

    private fun setupColorGradient(w: Int) {
        colorGradient =
            LinearGradient(0f, 0f, w.toFloat(), 0f, colorSeeds, null, Shader.TileMode.CLAMP)
        rectPaint.shader = colorGradient
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, canvasHeight)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled) return true

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                thumbBorderRadius = (oldThumbBorderRadius * 1.5).toFloat()
                thumbRadius = (oldThumbRadius * 1.5).toFloat()
            }
            MotionEvent.ACTION_MOVE -> {
                parent.requestDisallowInterceptTouchEvent(true)
                event.x.let {
                    thumbX = it
                    invalidate()
                }
                colorChangeListener?.onColorChangeListener(getColor())
            }
            MotionEvent.ACTION_UP -> {
                thumbBorderRadius = oldThumbBorderRadius
                thumbRadius = oldThumbRadius
                invalidate()
            }
        }

        return true
    }

    fun reset(notifyListener: Boolean = true) {
        thumbX = 0F
        invalidate()
        if (notifyListener) {
            colorChangeListener?.onColorChangeListener(getColor())
        }
    }

    fun getColor(): Int = thumbPaint.color

    fun getSliderValue(): Float = thumbX

    fun setSliderValue(value: Float) {
        thumbX = value
        invalidate()
    }

    fun setOnColorChangeListener(onColorChangeListener: OnColorChangeListener) {
        this.colorChangeListener = onColorChangeListener
    }

    interface OnColorChangeListener {

        fun onColorChangeListener(color: Int)
    }
}
