package art.intel.soft.ui.edit.collage.mask_drawable

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

abstract class BaseMaskDrawableBitmapShader(protected val maskBitmap: Bitmap) : Drawable() {

    abstract fun createPaint(): Paint

    private val paint: Paint by lazy { createPaint() }

    override fun draw(canvas: Canvas) {
        canvas.drawBitmap(maskBitmap, 0f, 0f, paint)
    }

    override fun setAlpha(i: Int) {
        paint.alpha = i
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.UNKNOWN

}
