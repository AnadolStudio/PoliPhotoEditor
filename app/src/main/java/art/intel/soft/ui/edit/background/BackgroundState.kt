package art.intel.soft.ui.edit.background

import android.graphics.Bitmap

sealed class BackgroundState {

    object Loading : BackgroundState()

    object Error : BackgroundState()

    abstract class Content : BackgroundState() {

        class CutContent(
                val maskBitmap: Bitmap?,
                val drawingBitmap: Bitmap? = null,
        ) : Content()

        class ChoiceContent(
                val maskBitmap: Bitmap?,
                val drawingBitmap: Bitmap?,
                val cutBitmap: Bitmap,
                val backgroundPathList: List<String>,
        ) : Content()
    }

}

data class BrushSettings(
        val size: Float,
        val color: Int,
        val mode: BrushMode
) {
    companion object {
        const val XSMALL: Float = 5.0f
        const val XLARGE: Float = 100.0f
        const val NORMAL: Float = (XLARGE - XSMALL) / 2
    }

}

enum class BrushMode {
    DRAW, ERASER
}
