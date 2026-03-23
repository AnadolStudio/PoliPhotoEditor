package art.intel.soft.ui.edit.collage.state

import android.graphics.Bitmap
import art.intel.soft.ui.edit.collage.DataBitmapWrapper

object CollageState {

    data class BottomViewState(
            val collagePathList: List<String>,
            val maskPathList: List<String>,
    )

    sealed class ImageViewState {

        object Loading : ImageViewState()

        class Content(
                val currentCollageBitmap: Bitmap,
                val currentMaskBitmapList: List<DataBitmapWrapper>,
                val photoBitmapList: List<Bitmap>,
        ) : ImageViewState()
    }

}
