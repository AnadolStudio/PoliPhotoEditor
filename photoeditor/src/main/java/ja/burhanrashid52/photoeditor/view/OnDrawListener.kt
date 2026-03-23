package ja.burhanrashid52.photoeditor.view

import android.view.MotionEvent
import android.view.View
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener
import ja.burhanrashid52.photoeditor.ViewType

class OnDrawListener(private val onDraw: () -> Unit) : OnPhotoEditorListener {

    override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) = Unit

    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) = Unit

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) = Unit

    override fun onStartViewChangeListener(viewType: ViewType?) = Unit

    override fun onStopViewChangeListener(viewType: ViewType?) {
        if (viewType == ViewType.BRUSH_DRAWING) {
            onDraw.invoke()
        }
    }

    override fun onTouchSourceImage(event: MotionEvent?) = Unit

    override fun onChangeSelectedView(view: View?) = Unit
}
