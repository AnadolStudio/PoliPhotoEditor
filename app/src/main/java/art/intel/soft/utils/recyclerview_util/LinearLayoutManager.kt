package art.intel.soft.utils.recyclerview_util

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

class ScrollToFirstLinearLayoutManager(context: Context) :
        LinearLayoutManager(context, RecyclerView.HORIZONTAL, false) {

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State?, position: Int) {
        val smoothScroller: RecyclerView.SmoothScroller = object : LinearSmoothScroller(recyclerView.context) {
            override fun getVerticalSnapPreference(): Int = SNAP_TO_START
            override fun getHorizontalSnapPreference(): Int = SNAP_TO_START
        }
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }

}
