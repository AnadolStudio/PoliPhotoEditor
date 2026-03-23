package art.intel.soft.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import art.intel.soft.model.AssetsDirections
import org.wysaid.nativePort.CGENativeLibrary

class CGENativeLoadImageCallback(
        private val context: Context,
        private val directory: AssetsDirections
) : CGENativeLibrary.LoadImageCallback {

    override fun loadImage(name: String, arg: Any): Bitmap? = BitmapFactory.decodeStream(
            try {
                context.assets.open("${directory.nameDir}/$name")
            } catch (ex: Exception) {
                null
            }
    )

    override fun loadImageOK(bmp: Bitmap, arg: Any) {
        bmp.recycle()
    }
}
