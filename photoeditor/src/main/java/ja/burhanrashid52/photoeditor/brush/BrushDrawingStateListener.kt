package ja.burhanrashid52.photoeditor.brush

import android.view.View
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener
import ja.burhanrashid52.photoeditor.PhotoEditorViewState
import ja.burhanrashid52.photoeditor.ViewType
import ja.burhanrashid52.photoeditor.view.DrawingView
import ja.burhanrashid52.photoeditor.view.PhotoEditorView

class BrushDrawingStateListener(
        val photoEditorView: PhotoEditorView,
        val viewState: PhotoEditorViewState
) : BrushViewChangeListener {

    private var onPhotoEditorListener: OnPhotoEditorListener? = null

    fun setOnPhotoEditorListener(listener: OnPhotoEditorListener) {
        onPhotoEditorListener = listener
    }

    override fun onViewAdd(drawingView: DrawingView?) {
        if (viewState.redoViewsCount > 0) viewState.popRedoView()
        viewState.addAddedView(drawingView)

        onPhotoEditorListener?.onAddViewListener(ViewType.BRUSH_DRAWING, viewState.addedViewsCount)
    }

    override fun onViewRemoved(drawingView: DrawingView?) {
        if (viewState.redoViewsCount > 0) {
            val removeView: View = viewState.removeAddedView(viewState.addedViewsCount - 1)

            if (removeView !is DrawingView) photoEditorView.removeView(removeView)
            viewState.pushRedoView(removeView)
        }

        onPhotoEditorListener?.onRemoveViewListener(ViewType.BRUSH_DRAWING, viewState.addedViewsCount)

    }

    override fun onStartDrawing() {
        onPhotoEditorListener?.onStartViewChangeListener(ViewType.BRUSH_DRAWING)
    }

    override fun onStopDrawing() {
        onPhotoEditorListener?.onStopViewChangeListener(ViewType.BRUSH_DRAWING)
    }
}
