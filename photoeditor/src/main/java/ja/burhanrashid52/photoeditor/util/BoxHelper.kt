package ja.burhanrashid52.photoeditor.util

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isVisible
import ja.burhanrashid52.photoeditor.PhotoEditorViewState
import ja.burhanrashid52.photoeditor.R
import ja.burhanrashid52.photoeditor.view.DrawingView

class BoxHelper(
        private val viewGroup: ViewGroup,
        private val viewState: PhotoEditorViewState
) {

    fun clearHelperBox() {
        for (i in 0 until viewGroup.childCount) {
            val childAt: View = viewGroup.getChildAt(i)

            childAt.findViewById<View>(R.id.frmBorder)?.isVisible = false
        }
        viewState.clearCurrentSelectedView()
    }

    fun clearAllViews(drawingView: DrawingView?) {
        for (i in 0 until viewState.addedViewsCount) {
            viewGroup.removeView(viewState.getAddedView(i))
        }

        if (viewState.containsAddedView(drawingView)) viewGroup.addView(drawingView)

        viewState.clearAddedViews()
        viewState.clearRedoViews()
        drawingView?.clearAll()
    }
}
