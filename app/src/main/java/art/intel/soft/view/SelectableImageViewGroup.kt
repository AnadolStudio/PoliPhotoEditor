package art.intel.soft.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.children

class SelectableImageViewGroup @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var selectableImageViews: List<SelectableImageView>
    private var listener: ((SelectableImageView) -> Unit)? = null

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        selectableImageViews = bindWithChildren()
    }

    private fun bindWithChildren(): List<SelectableImageView> = children
            .toList()
            .filterIsInstance<SelectableImageView>()
            .onEach { view ->
                view.onSelectedListener = this@SelectableImageViewGroup::changeSelectableView
            }

    fun changeSelectableView(currentView: SelectableImageView) {
        selectableImageViews.forEach { view ->
            if (view == currentView) {
                listener?.invoke(view)

                return@forEach
            }

            view.isImageSelected = false
        }
    }

    fun setOnChangeListener(action: (SelectableImageView) -> Unit) {
        listener = action
    }

}
