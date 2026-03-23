package ja.burhanrashid52.photoeditor.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View
import androidx.core.content.ContextCompat
import ja.burhanrashid52.photoeditor.util.dpToPx

open class BorderView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes), Scalable {

    private companion object {
        val BUTTON_RADIUS = 12.dpToPx()
        val SUPPORT_BUTTON_RADIUS = 10.dpToPx()
        val BUTTON_WIDTH = BUTTON_RADIUS * 2
        val SUPPORT_BUTTON_WIDTH = SUPPORT_BUTTON_RADIUS * 2
        val BORDER_WIDTH = 3.dpToPx()
    }

    private val borderPaint = Paint()
    private val borderFillPaint = Paint()
    private val buttonPaint = Paint()
    private var borderData = BorderData()
    private val movableButtonsSet = mutableSetOf<ButtonsData>()
    private var clearListener: (() -> Unit)? = null
    private var needShowSupportCircle: Boolean = true

    private var supportScale = 1f

    init {
        borderPaint.color = Color.WHITE
        borderPaint.style = Paint.Style.STROKE

        borderFillPaint.color = Color.TRANSPARENT
        borderFillPaint.style = Paint.Style.FILL

        buttonPaint.color = Color.WHITE
    }

    override fun getScalableViews(): List<View> = listOf(this)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        drawBorder(canvas)
        drawMainButtons(canvas)
        drawSupportButtons(canvas)
    }

    private fun drawMainButtons(canvas: Canvas) {
        drawLeftTopButton(canvas, borderData.leftTopButton?.drawableId?.toDrawable(context))
        drawRightTopButton(canvas, borderData.rightTopButton?.drawableId?.toDrawable(context))
        drawLeftBottomButton(canvas, borderData.leftBottomButton?.drawableId?.toDrawable(context))
        drawRightBottomButton(canvas, borderData.rightBottomButton?.drawableId?.toDrawable(context))
    }

    protected open fun drawLeftTopButton(canvas: Canvas, drawable: Drawable? = null) =
            drawButton(canvas, leftTopRect(), drawable)

    protected open fun drawRightTopButton(canvas: Canvas, drawable: Drawable? = null) =
            drawButton(canvas, rightTopRect(), drawable)

    protected open fun drawLeftBottomButton(canvas: Canvas, drawable: Drawable? = null) =
            drawButton(canvas, leftBottomRect(), drawable)

    protected open fun drawRightBottomButton(canvas: Canvas, drawable: Drawable? = null) =
            drawButton(canvas, rightBottomRect(), drawable)

    private fun leftTopRect(): RectF = RectF(leftStartX(), topStartY(), leftEndX(BUTTON_WIDTH), topEndY(BUTTON_WIDTH))

    private fun rightTopRect(): RectF = RectF(rightStartX(BUTTON_WIDTH), topStartY(), rightEndX(), topEndY(BUTTON_WIDTH))

    private fun leftBottomRect(): RectF = RectF(leftStartX(), bottomStartY(BUTTON_WIDTH), leftEndX(BUTTON_WIDTH), bottomEndY())

    private fun rightBottomRect(): RectF = RectF(rightStartX(BUTTON_WIDTH), bottomStartY(BUTTON_WIDTH), rightEndX(), bottomEndY())

    private fun drawSupportButtons(canvas: Canvas) {
        drawTopButton(canvas, borderData.topButton?.drawableId?.toDrawable(context))
        drawLeftMiddleButton(canvas, borderData.leftMiddleButton?.drawableId?.toDrawable(context))
        drawRightMiddleButton(canvas, borderData.rightMiddleButton?.drawableId?.toDrawable(context))
        drawBottomButton(canvas, borderData.bottomButton?.drawableId?.toDrawable(context))
    }

    protected open fun drawTopButton(canvas: Canvas, drawable: Drawable? = null) = drawButton(
            canvas, topRect(), drawable, needShowSupportCircle
    )

    private fun topRect(): RectF = RectF(
            middleStartX(SUPPORT_BUTTON_RADIUS),
            topStartY() + SUPPORT_BUTTON_RADIUS / supportScale,
            middleEndX(SUPPORT_BUTTON_RADIUS),
            topEndY(SUPPORT_BUTTON_WIDTH) + SUPPORT_BUTTON_RADIUS / supportScale
    )

    protected open fun drawLeftMiddleButton(canvas: Canvas, drawable: Drawable? = null) = drawButton(
            canvas, leftMiddleRect(), drawable, needShowSupportCircle
    )

    private fun leftMiddleRect(): RectF = RectF(
            leftStartX() + SUPPORT_BUTTON_RADIUS / supportScale,
            middleStartY(SUPPORT_BUTTON_RADIUS),
            leftEndX(SUPPORT_BUTTON_WIDTH) + SUPPORT_BUTTON_RADIUS / supportScale,
            middleEndY(SUPPORT_BUTTON_RADIUS)
    )

    protected open fun drawRightMiddleButton(canvas: Canvas, drawable: Drawable? = null) = drawButton(
            canvas, rightMiddleRect(), drawable, needShowSupportCircle
    )

    private fun rightMiddleRect(): RectF = RectF(
            rightStartX(SUPPORT_BUTTON_WIDTH) - SUPPORT_BUTTON_RADIUS / supportScale - BORDER_WIDTH / (2 * supportScale),
            middleStartY(SUPPORT_BUTTON_RADIUS),
            rightEndX() - SUPPORT_BUTTON_RADIUS / supportScale - BORDER_WIDTH / (2 * supportScale),
            middleEndY(SUPPORT_BUTTON_RADIUS),
    )

    protected open fun drawBottomButton(canvas: Canvas, drawable: Drawable? = null) = drawButton(
            canvas, bottomRect(), drawable, needShowSupportCircle
    )

    private fun bottomRect(): RectF = RectF(
            middleStartX(SUPPORT_BUTTON_RADIUS),
            bottomStartY(SUPPORT_BUTTON_WIDTH) - SUPPORT_BUTTON_RADIUS / supportScale,
            middleEndX(SUPPORT_BUTTON_RADIUS),
            bottomEndY() - SUPPORT_BUTTON_RADIUS / supportScale,
    )

    protected open fun middleStartY(buttonRadius: Int) = height / 2 - buttonRadius / supportScale

    protected open fun middleEndY(buttonRadius: Int) = height / 2 + buttonRadius / supportScale

    protected open fun bottomStartY(buttonWidth: Int) = height - buttonWidth / supportScale

    protected open fun bottomEndY() = height.toFloat()

    protected open fun leftStartX() = 0F

    protected open fun leftEndX(buttonWidth: Int) = buttonWidth / supportScale

    protected open fun middleStartX(buttonRadius: Int) = width / 2 - buttonRadius / supportScale

    protected open fun middleEndX(buttonRadius: Int) = width / 2 + buttonRadius / supportScale

    protected open fun rightEndX() = width.toFloat()

    protected open fun rightStartX(buttonWidth: Int) = width - buttonWidth / supportScale

    protected open fun topStartY() = 0F

    protected open fun topEndY(buttonWidth: Int) = buttonWidth / supportScale

    private fun drawButton(
            canvas: Canvas, rectF: RectF, drawable: Drawable? = null, needShowCircle: Boolean = true
    ) {
        if (drawable == null) return

        val left = rectF.left
        val top = rectF.top
        val right = rectF.right
        val bottom = rectF.bottom

        if (needShowCircle) {
            canvas.drawCircle((left + right) / 2, (top + bottom) / 2, (right - left) / 2, buttonPaint)
        }

        drawable.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        drawable.draw(canvas)
    }

    private fun drawBorder(canvas: Canvas) {
        val borderWidth = BORDER_WIDTH / supportScale
        val padding = BUTTON_WIDTH / supportScale
        borderPaint.strokeWidth = borderWidth

        val startXRect = leftStartX() + padding - borderWidth / 2
        val startYRect = topStartY() + padding - borderWidth / 2
        val endXRect = rightEndX() - padding + borderWidth / 2
        val endYRect = bottomEndY() - padding + borderWidth / 2

        canvas.drawRect(startXRect, startYRect, endXRect, endYRect, borderPaint)
        canvas.drawRect(startXRect, startYRect, endXRect, endYRect, borderFillPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean = when (event?.action) {
        ACTION_DOWN -> onButtonClick(event)
        ACTION_MOVE -> onButtonMove(event)
        else -> super.onTouchEvent(event).also { actionClear(event) }
    }

    private fun actionClear(event: MotionEvent?) {
        if (event?.action != ACTION_UP && event?.action != ACTION_CANCEL) return

        clearListener?.invoke()
        movableButtonsSet.clear()
    }

    private fun onButtonClick(event: MotionEvent): Boolean = when {
        borderData.leftTopButton != null && leftTopRect().contains(event.x, event.y) -> true.also { actionDown(borderData.leftTopButton, event) }
        borderData.rightTopButton != null && rightTopRect().contains(event.x, event.y) -> true.also { actionDown(borderData.rightTopButton, event) }
        borderData.leftBottomButton != null && leftBottomRect().contains(event.x, event.y) -> true.also { actionDown(borderData.leftBottomButton, event) }
        borderData.rightBottomButton != null && rightBottomRect().contains(event.x, event.y) -> true.also { actionDown(borderData.rightBottomButton, event) }
        borderData.leftMiddleButton != null && leftMiddleRect().contains(event.x, event.y) -> true.also { actionDown(borderData.leftMiddleButton, event) }
        borderData.topButton != null && topRect().contains(event.x, event.y) -> true.also { actionDown(borderData.topButton, event) }
        borderData.rightMiddleButton != null && rightMiddleRect().contains(event.x, event.y) -> true.also { actionDown(borderData.rightMiddleButton, event) }
        borderData.bottomButton != null && bottomRect().contains(event.x, event.y) -> true.also { actionDown(borderData.bottomButton, event) }
        else -> false
    }

    private fun onButtonMove(event: MotionEvent): Boolean = when {
        borderData.leftTopButton?.let(movableButtonsSet::contains) ?: false && borderData.leftTopButton.isMovable() -> true.also { actionDown(borderData.leftTopButton, event) }
        borderData.rightTopButton?.let(movableButtonsSet::contains) ?: false && borderData.rightTopButton.isMovable() -> true.also { actionDown(borderData.rightTopButton, event) }
        borderData.leftBottomButton?.let(movableButtonsSet::contains) ?: false && borderData.leftBottomButton.isMovable() -> true.also { actionDown(borderData.leftBottomButton, event) }
        borderData.rightBottomButton?.let(movableButtonsSet::contains) ?: false && borderData.rightBottomButton.isMovable() -> true.also { actionDown(borderData.rightBottomButton, event) }
        borderData.leftMiddleButton?.let(movableButtonsSet::contains) ?: false && borderData.leftMiddleButton.isMovable() -> true.also { actionDown(borderData.leftMiddleButton, event) }
        borderData.topButton?.let(movableButtonsSet::contains) ?: false && borderData.topButton.isMovable() -> true.also { actionDown(borderData.topButton, event) }
        borderData.rightMiddleButton?.let(movableButtonsSet::contains) ?: false && borderData.rightMiddleButton.isMovable() -> true.also { actionDown(borderData.rightMiddleButton, event) }
        borderData.bottomButton?.let(movableButtonsSet::contains) ?: false && borderData.bottomButton.isMovable() -> true.also { actionDown(borderData.bottomButton, event) }
        else -> false
    }

    private fun actionDown(buttonsData: ButtonsData?, event: MotionEvent) {
        buttonsData?.action?.apply {
            invoke(event.x, event.y)
            movableButtonsSet.add(buttonsData)
        }
    }

    override fun setSupportScale(scale: Float) {
        supportScale = scale
        invalidate()
    }

    fun setBorderData(borderData: BorderData) {
        this.borderData = borderData
    }

    fun setActionClear(action: () -> Unit) {
        clearListener = action
    }

    fun needShowSupportCircle(show: Boolean) {
        needShowSupportCircle = show
    }

    data class BorderData(
            val leftTopButton: ButtonsData? = null,
            val rightTopButton: ButtonsData? = null,
            val leftBottomButton: ButtonsData? = null,
            val rightBottomButton: ButtonsData? = null,
            val leftMiddleButton: ButtonsData? = null,
            val topButton: ButtonsData? = null,
            val rightMiddleButton: ButtonsData? = null,
            val bottomButton: ButtonsData? = null,
    )

    data class ButtonsData(val drawableId: Int, val action: ((x: Float, y: Float) -> Unit), val isMovable: Boolean = false)

    private fun Int.toDrawable(context: Context): Drawable? = ContextCompat.getDrawable(context, this)

    private fun ButtonsData?.isMovable(): Boolean = this?.isMovable == true
}
