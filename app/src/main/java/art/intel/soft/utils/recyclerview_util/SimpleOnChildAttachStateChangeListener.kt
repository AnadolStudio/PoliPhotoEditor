package art.intel.soft.utils.recyclerview_util

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SimpleOnChildAttachStateChangeListener(
        private val onChildViewAttachedToWindow: ((view: View) -> Unit)? = null,
        private val onChildViewDetachedFromWindow: ((view: View) -> Unit)? = null,
) : RecyclerView.OnChildAttachStateChangeListener {

    override fun onChildViewAttachedToWindow(view: View) {
        onChildViewAttachedToWindow?.invoke(view)
    }

    override fun onChildViewDetachedFromWindow(view: View) {
        onChildViewDetachedFromWindow?.invoke(view)
    }

}
