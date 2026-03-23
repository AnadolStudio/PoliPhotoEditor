package art.intel.soft.view.body

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import ja.burhanrashid52.photoeditor.util.dpToPx

class HipsView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0,
) : WaistView(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        val ARC_PADDING_Y = 28F.dpToPx()
    }

    override fun drawLines(canvas: Canvas) {
        canvas.drawLine(width / 2F, PADDING_Y, width / 2F, height - PADDING_Y, strokePaint)

        path.reset()
        path.moveTo(ARC_PADDING, height - ARC_PADDING_Y)
        path.quadTo(0F, height / 2F, ARC_PADDING, ARC_PADDING_Y)
        canvas.drawPath(path, strokePaint)

        path.reset()
        path.moveTo(width.toFloat() - ARC_PADDING, height - ARC_PADDING_Y)
        path.quadTo(width.toFloat(), height / 2F, width.toFloat() - ARC_PADDING, ARC_PADDING_Y)
        canvas.drawPath(path, strokePaint)
    }

}
