package ja.burhanrashid52.photoeditor.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.updatePadding
import ja.burhanrashid52.photoeditor.util.dpToPx

class ScalableImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr), Scalable {

    private companion object {
        val padding = 24.dpToPx()
    }

    private var supportScale = 1f

    override fun getScalableViews(): List<View> = listOf(this)

    override fun setSupportScale(scale: Float) {
        supportScale = scale
        val padding = (padding / supportScale).toInt()
        updatePadding(padding, padding, padding, padding)
    }
}