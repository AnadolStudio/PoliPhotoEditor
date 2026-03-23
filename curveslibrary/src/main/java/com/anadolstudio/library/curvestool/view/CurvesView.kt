package com.anadolstudio.library.curvestool.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toPoint
import androidx.core.graphics.toPointF
import com.anadolstudio.library.R
import com.anadolstudio.library.curvestool.data.CurvePoint
import com.anadolstudio.library.curvestool.data.CurvePoint.Companion.MAX_VALUE
import com.anadolstudio.library.curvestool.listener.CurvesValuesChangeListener
import com.anadolstudio.library.curvestool.util.*
import kotlin.math.abs

class CurvesView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val INTENSITY = 0.2F
        private const val X_RANGE_PERCENT = 0.08334F // 8.334 %
        private const val Y_RANGE_PERCENT = 0.2F // 20 %
        private const val DELETE_ZONE = 100
        private const val MAX_SIZE = 12
        private val PADDING = 0.dpToPx()
        private val DEFAULT_SIZE = 2.dpToPx().toFloat()
        private val DEFAULT_POINT_SIDE = 10.dpToPx().toFloat()
    }

    private var viewState: CurvesViewState = CurvesViewState.WHITE_STATE
        set(value) {
            field = value
            changeColor(value)

            currentPoints = when (value) {
                CurvesViewState.WHITE_STATE -> whitePoints
                CurvesViewState.RED_STATE -> redPoints
                CurvesViewState.GREEN_STATE -> greenPoints
                CurvesViewState.BLUE_STATE -> bluePoints
            }

            invalidate()
        }

    private val themeManager = ThemeManager(context, viewState)

    private val whitePoints = mutableListOf<CurvePoint>()
    private val redPoints = mutableListOf<CurvePoint>()
    private val greenPoints = mutableListOf<CurvePoint>()
    private val bluePoints = mutableListOf<CurvePoint>()
    private var currentPoints = mutableListOf<CurvePoint>()
    private var selectedPoint: CurvePoint? = null
        set(value) {
            field?.isSelected = false
            field = value
            field?.isSelected = true
        }

    private var pointSide: Float = DEFAULT_POINT_SIDE
    private var startX: Int = 0
    private var startY: Int = 0
    private var endX: Int = 0
    private var endY: Int = 0
    private var borderWidth: Int = 0
    private var borderHeight: Int = 0

    private var changeListener: CurvesValuesChangeListener? = null

    init {
        if (attrs != null) {
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.CurvesView)

            themeManager.borderWidth =
                    typeArray.getDimension(R.styleable.CurvesView_borderStrokeWidth, DEFAULT_SIZE)
            themeManager.curveWidth =
                    typeArray.getDimension(R.styleable.CurvesView_curveStrokeWidth, DEFAULT_SIZE)
            themeManager.pointStrokeWidth =
                    typeArray.getDimension(R.styleable.CurvesView_pointStrokeWidth, DEFAULT_SIZE)
            pointSide =
                    typeArray.getDimension(R.styleable.CurvesView_pointSide, DEFAULT_POINT_SIDE)
            themeManager.borderStrokeColor =
                    typeArray.getColor(R.styleable.CurvesView_borderStrokeColor, Color.TRANSPARENT)
            themeManager.borderFillColor =
                    typeArray.getColor(R.styleable.CurvesView_borderFillColor, Color.TRANSPARENT)

            typeArray.recycle()
        }

    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        startX = PADDING
        startY = PADDING
        endX = width - PADDING
        endY = height - PADDING
        borderWidth = endX - startX
        borderHeight = endY - startY

        if (changed && currentPoints.isEmpty()) {
            initDefaultPoints()
            showWhiteState()
        }
    }

    private fun initDefaultPoints() {
        whitePoints.apply { clear() }.init()
        redPoints.apply { clear() }.init()
        greenPoints.apply { clear() }.init()
        bluePoints.apply { clear() }.init()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawVerticalLines(canvas)

        val drawPoints = currentPoints.filter { curvePoint -> !curvePoint.candidateToDelete }
        drawCurveCubicBezier(canvas, drawPoints.map { it.viewPoint })
        drawAllPoints(canvas, drawPoints)
    }

    private fun drawVerticalLines(canvas: Canvas) {
        val count = when {
            width > 120.dpToPx() -> 4
            width <= 120.dpToPx() && width > 90.dpToPx() -> 3
            width <= 90.dpToPx() && width > 60.dpToPx() -> 2
            width <= 60.dpToPx() && width > 30.dpToPx() -> 1
            else -> 0
        }

        if (count == 0) return

        val delta = width / (count + 1)
        var currentLine = delta

        for (i in 0 until count) {
            canvas.drawLine(currentLine, 0, currentLine, height, themeManager.diagonalStrokePaint)
            currentLine += delta
        }
    }

    private fun drawAllPoints(canvas: Canvas, points: List<CurvePoint>) {
        points.forEach { current -> drawPoint(canvas, current.viewPoint, current.isSelected) }
    }

    private fun drawPoint(canvas: Canvas, current: PointF, isSelected: Boolean = false) {
        val rect = pointRect(current, pointSide / 2)
        canvas.drawOval(rect, themeManager.getPointFillPaint(isSelected)) // Fill
        canvas.drawOval(rect, themeManager.pointStrokePaint) // Stroke
    }

    private fun pointRect(current: PointF, halfSize: Float) = RectF(
            current.x - halfSize,
            current.y - halfSize,
            current.x + halfSize,
            current.y + halfSize
    )

    private fun drawCurveCubicBezier(canvas: Canvas, points: List<PointF>) {
        val path = Path()

        val first = points.first()
        path.moveTo(first.x, first.y)

        points.forEachWithPreviousAndNext { prevPrevious, previous, current, next ->
            val prevDx = (current.x - prevPrevious.x) * INTENSITY
            val prevDy = (current.y - prevPrevious.y) * INTENSITY
            val curDx = (next.x - previous.x) * INTENSITY
            val curDy = (next.y - previous.y) * INTENSITY

            path.cubicTo(
                    previous.x + prevDx,
                    previous.y + prevDy,
                    current.x - curDx,
                    current.y - curDy,
                    current.x,
                    current.y
            )
        }

        canvas.drawInRect(startX, startY, endX, endY) {
            drawBorder(canvas)
            drawPath(path, themeManager.curvePaint)
        }
    }

    private fun Canvas.drawInRect(
            left: Int, top: Int, right: Int, bottom: Int, action: Canvas.() -> Unit
    ) {
        save()
        clipRect(left, top, right, bottom)
        action.invoke(this)
        restore()
    }

    private fun drawBorder(canvas: Canvas) {
        val borderWidth = themeManager.borderStrokePaint.strokeWidth.toInt() / 2

        // Diagonal
        canvas.drawLine(
                startX + borderWidth / 2,
                endY - borderWidth / 2,
                endX - borderWidth / 2,
                startY + borderWidth / 2,
                themeManager.diagonalStrokePaint
        )
        canvas.saveLayer(0F, 0F, width.toFloat(), height.toFloat(), null)
    }

    private fun changeColor(value: CurvesViewState) {
        themeManager.curvePaint.color = value.toColor(context)
        themeManager.curveFillPaint.color = value.toColor(context)
        themeManager.pointStrokePaint.color = value.toColor(context)
        themeManager.pointFillPaint.color = value.toColor(context)
        themeManager.pointFillSelectedPaint.color = value.toPointFillSelectedColor(context)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean = true.also {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> onTap(event)
            MotionEvent.ACTION_MOVE -> onMove(event)
            MotionEvent.ACTION_UP -> onUp()
        }
    }

    private fun onTap(event: MotionEvent) = onTouchEventAction {
        val scaleX = event.x.minus(startX * 2).scaleTo(borderWidth, MAX_VALUE)
        val scaleY = event.y.minus(scaleY * 2).scaleTo(borderHeight, MAX_VALUE)

        val pointInXRange = getPointInXRange(scaleX)
        selectedPoint = null

        when (pointInXRange != null) {
            true -> selectIfYInTouchRange(scaleY, pointInXRange)
            false -> selectNewPoint(event)
        }
    }

    private fun selectNewPoint(event: MotionEvent) {
        if (currentPoints.size >= MAX_SIZE) return

        selectedPoint = CurvePoint(event.x, event.y, width, height, true).also(this::addNewPointAndSort)
    }

    private fun selectIfYInTouchRange(y: Int, pointInRange: CurvePoint) {
        val range = (borderHeight * Y_RANGE_PERCENT).scaleTo(height, MAX_VALUE).toFloat()

        if (y.inRange(pointInRange.curvePoint.y, range)) {
            selectedPoint = pointInRange
        }
    }

    private fun getPointInXRange(scaleX: Int) = currentPoints
            .filter { curvePoint ->
                val range = (borderWidth * X_RANGE_PERCENT).scaleTo(width, MAX_VALUE).toFloat()
                scaleX.inRange(curvePoint.curvePoint.x, range)
            }
            .minByOrNull { curvePoint ->
                abs(curvePoint.curvePoint.x - scaleX)
            }

    private fun addNewPointAndSort(newPoint: CurvePoint) {
        currentPoints.add(newPoint)
        currentPoints.sortBy { curvePoint -> curvePoint.curvePoint.x }
        notifyListener()
    }

    private fun onMove(event: MotionEvent) = onTouchEventAction {
        val selectPoint = selectedPoint ?: return@onTouchEventAction

        val scaleX = event.x.minus(startX * 2).scaleTo(borderWidth, MAX_VALUE)
        val scaleY = event.y.minus(startX * 2).scaleTo(borderHeight, MAX_VALUE)
        var isChanged = false

        if (currentPoints.first() != selectPoint && currentPoints.last() != selectPoint) {
            val range = borderWidth * X_RANGE_PERCENT

            val index = currentPoints.indexOf(selectPoint)
            val leftPoint = currentPoints[index - 1]
            val rightPoint = currentPoints[index + 1]

            if (event.x in leftPoint.viewPoint.x + range..rightPoint.viewPoint.x - range) {
                selectPoint.viewPoint.x = event.x
                selectPoint.curvePoint.x = scaleX.toFloat()
                isChanged = true
            }

            selectPoint.candidateToDelete = event.y.toInt() !in -DELETE_ZONE..height + DELETE_ZONE
        }

        when {
            event.y.toInt() in startY..endY -> {
                selectPoint.viewPoint.y = event.y
                selectPoint.curvePoint.y = scaleY.toFloat()
                isChanged = true
            }
            event.y.toInt() < startY -> {
                selectPoint.viewPoint.y = startY.toFloat()
                selectPoint.curvePoint.y = 0F
                isChanged = true
            }
            event.y.toInt() > endY -> {
                selectPoint.viewPoint.y = endY.toFloat()
                selectPoint.curvePoint.y = MAX_VALUE.toFloat()
                isChanged = true
            }
        }

        if (isChanged) {
            notifyListener()
        }
    }

    private fun notifyListener() {
        val changedPoints = currentPoints
                .filter { curvePoint -> !curvePoint.candidateToDelete }
                .mapToPoint()

        when (viewState) {
            CurvesViewState.WHITE_STATE -> changeListener?.onWhiteChanelChanged(changedPoints)
            CurvesViewState.RED_STATE -> changeListener?.onRedChanelChanged(changedPoints)
            CurvesViewState.GREEN_STATE -> changeListener?.onGreenChanelChanged(changedPoints)
            CurvesViewState.BLUE_STATE -> changeListener?.onBlueChanelChanged(changedPoints)
        }
    }

    private fun onUp() = onTouchEventAction { removeCandidateToDelete() }

    private fun removeCandidateToDelete() {
        currentPoints
                .find { point -> point.candidateToDelete }
                ?.also { candidateToDelete -> currentPoints.remove(candidateToDelete) }
    }

    private fun onTouchEventAction(action: () -> Unit) {
        action.invoke()
        invalidate()
    }

    fun showRedState() {
        viewState = CurvesViewState.RED_STATE
    }

    fun showGreenState() {
        viewState = CurvesViewState.GREEN_STATE
    }

    fun showBlueState() {
        viewState = CurvesViewState.BLUE_STATE
    }

    fun showWhiteState() {
        viewState = CurvesViewState.WHITE_STATE
    }

    fun setChangeListener(listener: CurvesValuesChangeListener) {
        changeListener = listener
    }

    fun reset() {
        initDefaultPoints()
        selectedPoint = null
        changeListener?.onReset(whitePoints.mapToPoint(), redPoints.mapToPoint(), greenPoints.mapToPoint(), bluePoints.mapToPoint())
        invalidate()
    }

    fun resetTo(rgb: List<Point>, r: List<Point>, g: List<Point>, b: List<Point>) {
        whitePoints.clear().also { whitePoints.addAll(rgb.mapToCurvePoint()) }
        redPoints.clear().also { redPoints.addAll(r.mapToCurvePoint()) }
        greenPoints.clear().also { greenPoints.addAll(g.mapToCurvePoint()) }
        bluePoints.clear().also { bluePoints.addAll(b.mapToCurvePoint()) }

        selectedPoint = null
        changeListener?.onReset(whitePoints.mapToPoint(), redPoints.mapToPoint(), greenPoints.mapToPoint(), bluePoints.mapToPoint())
        invalidate()
    }

    private fun MutableList<CurvePoint>.init() {
        this.add(CurvePoint(Point(startX, endY).toPointF(), Point(0, MAX_VALUE).toPointF()))
        this.add(CurvePoint(Point(endX, startY).toPointF(), Point(MAX_VALUE, 0).toPointF()))
    }

    private fun List<CurvePoint>.mapToPoint(): List<Point> = this.map { curvePoint ->
        PointF(curvePoint.curvePoint.x, MAX_VALUE - curvePoint.curvePoint.y).toPoint()
    }

    private fun List<Point>.mapToCurvePoint(): List<CurvePoint> = this.map { point ->
        val correctY = MAX_VALUE - point.y
        val viewX = point.x.scaleTo(MAX_VALUE, borderWidth)
        val viewY = correctY.scaleTo(MAX_VALUE, borderHeight)

        CurvePoint(Point(viewX, viewY).toPointF(), Point(point.x, correctY).toPointF())
    }

}
