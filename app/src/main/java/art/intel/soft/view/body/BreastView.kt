package art.intel.soft.view.body

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

open class BreastView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    // DefaultSize = 65x65dp

    private companion object {
        val STROKE_WIDTH = 2F.dpToPx()
        val GAP_LENGTH = 4F.dpToPx()
        val DRAWABLE_WIDTH = 12.dpToPx()
        val PADDING = 8.dpToPx()
        val ACTION_PADDING = 8.dpToPx()

        val MAX_SIZE = 220.dpToPx()
        val MIN_SIZE = 55.dpToPx()
    }

    private val strokePaint = Paint()
    private var radius = 0F
    private var isMirror = false
    private var lastButtonPoint: PointF? = null
    private var currentGuest: GuestState? = null
    private var photoIsBright: Boolean = false

    private fun getRightDrawable(context: Context): Drawable = if (photoIsBright) {
        context.compatDrawable(R.drawable.ic_breast_radius_right_button_dark)
    } else {
        context.compatDrawable(R.drawable.ic_breast_radius_right_button)
    }

    private fun getLeftDrawable(context: Context): Drawable = if (photoIsBright) {
        context.compatDrawable(R.drawable.ic_breast_radius_left_button_dark)
    } else {
        context.compatDrawable(R.drawable.ic_breast_radius_left_button)
    }

    init {
        strokePaint.color = Color.WHITE
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = STROKE_WIDTH
        strokePaint.pathEffect = DashPathEffect(
                FloatArray(2) { STROKE_WIDTH; GAP_LENGTH }, GAP_LENGTH
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        radius = width / 2F
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        canvas.drawCircle(pivotX, pivotY, radius - STROKE_WIDTH - PADDING, strokePaint)

        val drawable = getDrawable()
        drawable.bounds = getButtonBounds()
        drawable.draw(canvas)
    }

    private fun getButtonBounds(): Rect = if (isMirror) getButtonBoundsLeft() else getButtonBoundsRight()

    private fun Rect.withPadding(padding: Int): Rect = this.apply {
        top -= padding
        left -= padding
        right += padding
        bottom += padding
    }

    private fun getButtonBoundsRight(): Rect {
        val x = ((radius - PADDING * 2 + STROKE_WIDTH / 2) * sin(PI / 4)).toInt()
        val y = ((radius + PADDING * 2 - STROKE_WIDTH / 2) * cos(PI / 4)).toInt()

        return Rect(
                width / 2 + x - DRAWABLE_WIDTH / 2,
                y - height / 4 - DRAWABLE_WIDTH / 2,
                width / 2 + x + DRAWABLE_WIDTH / 2,
                y - height / 4 + DRAWABLE_WIDTH / 2,
        )
    }

    private fun getButtonBoundsLeft(): Rect {
        val x = ((radius - PADDING * 2 + STROKE_WIDTH / 2) * sin(PI / 4)).toInt()
        val y = ((radius + PADDING * 2 - STROKE_WIDTH / 2) * cos(PI / 4)).toInt()

        return Rect(
                width / 2 - x - DRAWABLE_WIDTH / 2,
                y - height / 4 - DRAWABLE_WIDTH / 2,
                width / 2 - x + DRAWABLE_WIDTH / 2,
                y - height / 4 + DRAWABLE_WIDTH / 2,
        )
    }

    fun isMirror(isMirror: Boolean) {
        this.isMirror = isMirror
    }

    private fun getDrawable(): Drawable = if (isMirror) getLeftDrawable(context) else getRightDrawable(context)

    override fun onTouchEvent(event: MotionEvent): Boolean = when (event.action and event.actionMasked) {
        ACTION_DOWN -> {
            bringToFront()
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

    private fun onActionMove(event: MotionEvent): Boolean = when (currentGuest) {
        GuestState.BUTTON_SCALE -> buttonScale(event)
        GuestState.TRANSLATION -> translation(event)
        else -> false

    }

    private fun buttonScale(event: MotionEvent): Boolean {
        val lastPoint = lastButtonPoint
        if (lastPoint == null || currentGuest != GuestState.BUTTON_SCALE) return false

        val currentPoint = correctCoordinate(event.x, event.y)

        val currentRadius = sqrt(currentPoint.x * currentPoint.x + currentPoint.y * currentPoint.y)
        val previousRadius = sqrt(lastPoint.x * lastPoint.x + lastPoint.y * lastPoint.y)
        val delta = (currentRadius - previousRadius).toInt() * 2

        if (layoutParams.width + delta > MAX_SIZE || layoutParams.width + delta < MIN_SIZE) return false

        layoutParams.width = layoutParams.width + delta
        layoutParams.height = layoutParams.height + delta
        requestLayout()

        translationX -= delta / 2F
        translationY -= delta / 2F

        this.lastButtonPoint = currentPoint

        return true
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
            getButtonBounds().withPadding(ACTION_PADDING).contains(event.x.toInt(), event.y.toInt()) -> GuestState.BUTTON_SCALE
            else -> GuestState.TRANSLATION
        }

        return true
    }

    private fun correctCoordinate(x: Float, y: Float): PointF {
        val mathCoordinateX = x - width / 2
        val mathCoordinateY = y - height / 2

        return PointF(mathCoordinateX, mathCoordinateY)
    }

    fun setPhotoIsBright(photoIsBright: Boolean) {
        this.photoIsBright = photoIsBright

        if (photoIsBright) {
            strokePaint.color = Color.BLACK
        } else {
            strokePaint.color = Color.WHITE
        }
    }

    enum class GuestState { BUTTON_SCALE, TRANSLATION }
}
