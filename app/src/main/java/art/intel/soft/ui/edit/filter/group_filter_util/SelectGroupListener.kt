package art.intel.soft.ui.edit.filter.group_filter_util

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import art.intel.soft.utils.recyclerview_util.BaseOnScrollListener
import art.intel.soft.utils.recyclerview_util.SimpleOnChildAttachStateChangeListener

class SelectGroupListener(private val recyclerView: RecyclerView) {

    private var scrollByUserClick = false
    private var onScrollListener: ((recyclerView: RecyclerView) -> Unit)? = null

    init {
        recyclerView.apply {

            addOnScrollListener(
                    BaseOnScrollListener(
                            onScrollStateChanged = { _, newState ->
                                if (newState == SCROLL_STATE_IDLE) {
                                    scrollByUserClick = false
                                }
                            }
                    )
            )

            addOnChildAttachStateChangeListener(
                    SimpleOnChildAttachStateChangeListener(
                            onChildViewDetachedFromWindow = {
                                if (!scrollByUserClick) {
                                    onScrollListener?.invoke(recyclerView)
                                }
                            }
                    )
            )
        }
    }

    fun setOnScrollListener(listener: (recyclerView: RecyclerView) -> Unit) {
        onScrollListener = listener
    }

    fun scrollToPosition(position: Int) {
        scrollByUserClick = true
        recyclerView.smoothScrollToPosition(position)
    }
}
