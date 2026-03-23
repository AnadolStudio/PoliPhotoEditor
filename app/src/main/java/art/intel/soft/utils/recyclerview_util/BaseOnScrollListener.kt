package art.intel.soft.utils.recyclerview_util

import androidx.recyclerview.widget.RecyclerView

class BaseOnScrollListener(
        private val onScrollStateChanged: ((recyclerView: RecyclerView, newState: Int) -> Unit)? = null,
        private val onScrolled: ((recyclerView: RecyclerView, dx: Int, dy: Int) -> Unit)? = null,
) : RecyclerView.OnScrollListener() {

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        onScrollStateChanged?.invoke(recyclerView, newState)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        onScrolled?.invoke(recyclerView, dx, dy)
    }
}
