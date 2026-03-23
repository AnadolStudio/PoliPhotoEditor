package ja.burhanrashid52.photoeditor.brush

import ja.burhanrashid52.photoeditor.view.DrawingView

interface BrushViewChangeListener {

    fun onViewAdd(drawingView: DrawingView?)

    fun onViewRemoved(drawingView: DrawingView?)

    fun onStartDrawing()

    fun onStopDrawing()
}
