package art.intel.soft.view.body

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View
import art.intel.soft.R
import art.intel.soft.extention.compatDrawable
import ja.burhanrashid52.photoeditor.util.dpToPx
import kotlin.math.sqrt

open class WaistView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    // DefaultSize = 140x140dp

    protected companion object {
        val STROKE_WIDTH = 2F.dpToPx()
        val GAP_LENGTH = 4F.dpToPx()
        val BUTTON_WIDTH = 17F.dpToPx()

        //        val RATIO_WIDTH = sqrt(BUTTON_WIDTH * BUTTON_WIDTH * 2F) / BUTTON_WIDTH
        val PADDING = 6.dpToPx()
        val ARC_PADDING = 32F.dpToPx()

        val MAX_SIZE = 220.dpToPx()
        val MIN_SIZE = 75.dpToPx()
        val PADDING_Y = 14F.dpToPx()
    }

    protected val strokePaint = Paint()
    protected val path = Path()
    protected var lastButtonPoint: PointF? = null
    protected var currentGuest: GuestState? = null
    protected var isPhotoBright: Boolean = false

    init {
        strokePaint.color = Color.BLACK
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = STROKE_WIDTH
        strokePaint.pathEffect = DashPathEffect(
                FloatArray(2) { STROKE_WIDTH; GAP_LENGTH }, GAP_LENGTH
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        drawLines(canvas)
        drawButtons(canvas)
    }

    private fun drawButtons(canvas: Canvas) {
        drawTopButton(canvas, getTopDrawable())
        drawLeftMiddleButton(canvas, getLeftDrawable())
        drawRightMiddleButton(canvas, getRightDrawable())
        drawBottomButton(canvas, getBottomDrawable())
    }

    private fun getBottomDrawable(): Drawable = if (isPhotoBright) {
        context.compatDrawable(R.drawable.ic_waist_bottom_button_dark)
    } else {
        context.compatDrawable(R.drawable.ic_waist_bottom_button)
    }

    private fun getRightDrawable(): Drawable = if (isPhotoBright) {
        context.compatDrawable(R.drawable.ic_waist_end_button_dark)
    } else {
        context.compatDrawable(R.drawable.ic_waist_end_button)
    }

    private fun getLeftDrawable(): Drawable = if (isPhotoBright) {
        context.compatDrawable(R.drawable.ic_waist_start_button_dark)
    } else {
        context.compatDrawable(R.drawable.ic_waist_start_button)
    }

    private fun getTopDrawable(): Drawable = if (isPhotoBright) {
        context.compatDrawable(R.drawable.ic_waist_top_button_dark)
    } else {
        context.compatDrawable(R.drawable.ic_waist_top_button)
    }

    protected open fun drawLines(canvas: Canvas) {
        canvas.drawLine(width / 2F, PADDING_Y, width / 2F, height - PADDING_Y, strokePaint)

        path.reset()
        path.moveTo(0F, height - PADDING_Y)
        path.quadTo(ARC_PADDING, height / 2F, 0F, PADDING_Y)
        canvas.drawPath(path, strokePaint)

        path.reset()
        path.moveTo(width.toFloat(), height - PADDING_Y)
        path.quadTo(width.toFloat() - ARC_PADDING, height / 2F, width.toFloat(), PADDING_Y)
        canvas.drawPath(path, strokePaint)
    }

    private fun RectF.withPadding(padding: Int): RectF = this.apply {
        top -= padding
        left -= padding
        right += padding
        bottom += padding
    }

    protected open fun drawTopButton(canvas: Canvas, drawable: Drawable? = null) = drawButton(
            canvas, topRect(), drawable
    )

    private fun topRect(): RectF = RectF(
            middleStartX(),
            topStartY(),
            middleEndX(),
            topEndY()
    )

    protected open fun drawLeftMiddleButton(canvas: Canvas, drawable: Drawable? = null) = drawButton(
            canvas, leftMiddleRect(), drawable
    )

    private fun leftMiddleRect(): RectF = RectF(
            leftStartX(),
            middleStartY(),
            leftEndX(),
            middleEndY()
    )

    protected open fun drawRightMiddleButton(canvas: Canvas, drawable: Drawable? = null) = drawButton(
            canvas, rightMiddleRect(), drawable
    )

    private fun rightMiddleRect(): RectF = RectF(
            rightStartX(),
            middleStartY(),
            rightEndX(),
            middleEndY(),
    )

    protected open fun drawBottomButton(canvas: Canvas, drawable: Drawable? = null) = drawButton(
            canvas, bottomRect(), drawable
    )

    private fun bottomRect(): RectF = RectF(
            middleStartX(),
            bottomStartY(),
            middleEndX(),
            bottomEndY(),
    )

    protected open fun middleStartY() = height / 2 - BUTTON_WIDTH / 2

    protected open fun middleEndY() = height / 2 + BUTTON_WIDTH / 2

    protected open fun bottomStartY() = height - BUTTON_WIDTH

    protected open fun bottomEndY() = height.toFloat()

    protected open fun leftStartX() = ARC_PADDING / 2F - BUTTON_WIDTH / 2 - STROKE_WIDTH / 2

    protected open fun leftEndX() = ARC_PADDING / 2F + BUTTON_WIDTH / 2 - STROKE_WIDTH / 2

    protected open fun middleStartX() = width / 2 - BUTTON_WIDTH / 2

    protected open fun middleEndX() = width / 2 + BUTTON_WIDTH / 2

    protected open fun rightEndX() = width - ARC_PADDING / 2 + BUTTON_WIDTH / 2 + STROKE_WIDTH / 2

    protected open fun rightStartX() = width - ARC_PADDING / 2 - BUTTON_WIDTH / 2 + STROKE_WIDTH / 2

    protected open fun topStartY() = 0F

    protected open fun topEndY() = BUTTON_WIDTH

    override fun onTouchEvent(event: MotionEvent): Boolean = when (event.action and event.actionMasked) {
        ACTION_DOWN -> {
            onActionDown(event)
        }
        ACTION_MOVE -> {
            onActionMove(event)
        }
        ACTION_UP, ACTION_CANCEL -> false.also {
            currentGuest = null
            lastButtonPoint = null
        }
        else -> super.onTouchEvent(event)
    }

    private fun drawButton(
            canvas: Canvas, rectF: RectF, drawable: Drawable? = null
    ) {
        if (drawable == null) return

        val left = rectF.left
        val top = rectF.top
        val right = rectF.right
        val bottom = rectF.bottom

        drawable.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        drawable.draw(canvas)
    }

    private fun onActionMove(event: MotionEvent): Boolean = when (currentGuest) {
        GuestState.CHANGE_WIDTH -> changeWidth(event)
        GuestState.CHANGE_HEIGHT -> changeHeight(event)
        GuestState.TRANSLATION -> translation(event)
        else -> false

    }

    private fun changeWidth(event: MotionEvent): Boolean {
        val lastPoint = lastButtonPoint
        if (lastPoint == null || currentGuest != GuestState.CHANGE_WIDTH) return false

        var (currentPoint, delta) = getDelta(event, lastPoint)

        if (layoutParams.width + delta > MAX_SIZE) {
            delta = MAX_SIZE - layoutParams.width
        }
        if (layoutParams.width + delta < MIN_SIZE) {
            delta = layoutParams.width - MIN_SIZE
        }

        layoutParams.width = layoutParams.width + delta
        requestLayout()

        translationX -= delta / 2F

        this.lastButtonPoint = currentPoint

        return true
    }

    private fun changeHeight(event: MotionEvent): Boolean {
        val lastPoint = lastButtonPoint
        if (lastPoint == null || currentGuest != GuestState.CHANGE_HEIGHT) return false

        var (currentPoint, delta) = getDelta(event, lastPoint)

        if (layoutParams.height + delta > MAX_SIZE) {
            delta = MAX_SIZE - layoutParams.height
        }
        if (layoutParams.height + delta < MIN_SIZE) {
            delta = layoutParams.height - MIN_SIZE
        }

        layoutParams.height = layoutParams.height + delta
        requestLayout()

        translationY -= delta / 2F

        this.lastButtonPoint = currentPoint

        return true
    }

    private fun getDelta(event: MotionEvent, lastPoint: PointF): Pair<PointF, Int> {
        val currentPoint = correctCoordinate(event.x, event.y)

        val currentRadius = sqrt(currentPoint.x * currentPoint.x + currentPoint.y * currentPoint.y)
        val previousRadius = sqrt(lastPoint.x * lastPoint.x + lastPoint.y * lastPoint.y)
        val delta = (currentRadius - previousRadius).toInt() * 2

        return Pair(currentPoint, delta)
    }

    private fun translation(event: MotionEvent): Boolean {
        val lastPoint = lastButtonPoint
        if (lastPoint == null || currentGuest != GuestState.TRANSLATION) return false

        val correctPoint = correctCoordinate(event.x, event.y)
        val deltaX = (correctPoint.x - lastPoint.x).toInt()
        val deltaY = (correctPoint.y - lastPoint.y).toInt()

        translationX += deltaX
        translationY += deltaY

        return true
    }

    private fun onActionDown(event: MotionEvent): Boolean {
        lastButtonPoint = correctCoordinate(event.x, event.y)

        currentGuest = when {
            topRect().withPadding(PADDING).contains(event.x, event.y)
                    || bottomRect().withPadding(PADDING).contains(event.x, event.y) -> {
                GuestState.CHANGE_HEIGHT
            }
            leftMiddleRect().withPadding(PADDING).contains(event.x, event.y)
                    || rightMiddleRect().withPadding(PADDING).contains(event.x, event.y) -> {
                GuestState.CHANGE_WIDTH
            }
            else -> {
                GuestState.TRANSLATION
            }
        }

        return true
    }

    private fun correctCoordinate(x: Float, y: Float): PointF {
        val mathCoordinateX = x - width / 2
        val mathCoordinateY = y - height / 2

        return PointF(mathCoordinateX, mathCoordinateY)
    }

    fun setPhotoIsBright(photoIsBright: Boolean) {
        this.isPhotoBright = photoIsBright

        if (photoIsBright) {
            strokePaint.color = Color.BLACK
        } else {
            strokePaint.color = Color.WHITE
        }
    }

    enum class GuestState { CHANGE_WIDTH, CHANGE_HEIGHT, TRANSLATION }
}
