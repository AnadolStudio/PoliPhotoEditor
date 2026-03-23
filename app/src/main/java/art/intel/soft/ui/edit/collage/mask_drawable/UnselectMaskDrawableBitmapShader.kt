package art.intel.soft.ui.edit.collage.mask_drawable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import art.intel.soft.R

class UnselectMaskDrawableBitmapShader(
        context: Context,
        pictureBitmap: Bitmap,
        maskBitmap: Bitmap
) : PreviewMaskDrawableBitmapShader(pictureBitmap, maskBitmap) {

    private val colorOverlay = context.getColor(R.color.collageOverlayColor)

    override fun postDraw(canvas: Canvas) {
        canvas.drawColor(colorOverlay)
    }
}
