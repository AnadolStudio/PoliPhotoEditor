package art.intel.soft.ui.gallery

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anadolstudio.library.curvestool.util.dpToPx

class MarginItemDecoration(
        private val marginStart: Int = MARGIN_NORMAL,
        private val marginTop: Int = MARGIN_NORMAL,
        private val marginEnd: Int = MARGIN_NORMAL,
        private val marginBottom: Int = MARGIN_NORMAL,
) : RecyclerView.ItemDecoration() {

    constructor(margin: Int) : this(margin, margin, margin, margin)

    companion object {
        const val MARGIN_NORMAL = 8
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) =
            outRect.set(
                    marginStart.dpToPx(),
                    marginTop.dpToPx(),
                    marginEnd.dpToPx(),
                    marginBottom.dpToPx()
            )
}
