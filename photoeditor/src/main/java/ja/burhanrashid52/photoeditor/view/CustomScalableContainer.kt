package ja.burhanrashid52.photoeditor.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import ja.burhanrashid52.photoeditor.util.dpToPx
import kotlin.math.roundToInt

class CustomScalableContainer @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes), Scalable, MaxSize {

    private val scaleChildViews: MutableList<Scalable> = mutableListOf()
    private var wasFindingTry = false
    private var maxWidth: Int? = null

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (!wasFindingTry) findScalableChild(scaleChildViews)
    }

    private fun findScalableChild(scaleChildViews: MutableList<Scalable>, viewGroup: ViewGroup? = null) {
        val currentGroup = viewGroup ?: this

        if (viewGroup == null) {
            wasFindingTry = true
        }

        for (i in 0..currentGroup.childCount) {
            val child = currentGroup.getChildAt(i)

            if (child is ViewGroup) findScalableChild(scaleChildViews, child)
            if (child is Scalable) scaleChildViews.add(child)
        }
    }

    override fun getScalableViews(): List<View> = scaleChildViews.flatMap { it.getScalableViews() }

    override fun setSupportScale(scale: Float) {
        val maxWidth = maxWidth

        if (maxWidth != null && maxWidth < width) {
            return
        }

        scaleChildViews.forEach { it.setSupportScale(scale) }
        scaleX = scale
        scaleY = scale
    }

    override fun setMaxSize(width: Int) {
        maxWidth = width
    }
}
