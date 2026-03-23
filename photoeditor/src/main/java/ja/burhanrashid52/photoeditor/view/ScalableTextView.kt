package ja.burhanrashid52.photoeditor.view

import android.app.ActionBar.LayoutParams
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import ja.burhanrashid52.photoeditor.util.dpToPx
import ja.burhanrashid52.photoeditor.util.setMarginsPx
import kotlin.math.roundToInt

class ScalableTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0,
) : AppCompatTextView(context, attrs, defStyleAttr), Scalable {

    private companion object {
        val MARGIN_DP = 24.dpToPx()
    }

    private var supportScale = 1f
    private var lastMargin: Int = MARGIN_DP

    override fun getScalableViews(): List<View> = listOf(this)

    override fun setSupportScale(scale: Float) {
        supportScale = scale

        val margin = (MARGIN_DP / supportScale).roundToInt()
        setMarginsPx(margin)
        requestLayout()

        changeParent(margin)
    }

    private fun changeParent(margin: Int) {
        val parent = parent as? View ?: return

        if (parent.layoutParams.width != LayoutParams.WRAP_CONTENT) {
            parent.layoutParams.width += (margin - lastMargin) * 2
            parent.requestLayout()

            lastMargin = margin
        }
    }
}
