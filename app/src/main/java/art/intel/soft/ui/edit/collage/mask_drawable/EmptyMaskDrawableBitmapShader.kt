package art.intel.soft.ui.edit.collage.mask_drawable

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Shader

class EmptyMaskDrawableBitmapShader(maskBitmap: Bitmap) : BaseMaskDrawableBitmapShader(maskBitmap) {

    override fun createPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        val bitmap = Bitmap.createBitmap(maskBitmap.width, maskBitmap.height, Config.ARGB_8888)
        Canvas(bitmap).apply { drawColor(Color.WHITE) }
        shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

}
