package art.intel.soft.model

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.toColorInt
import art.intel.soft.extention.singleFrom
import art.intel.soft.ui.edit.collage.BitmapWrapper
import art.intel.soft.ui.edit.collage.DataBitmapWrapper
import art.intel.soft.ui.edit.collage.EmptyBitmapWrapper
import io.reactivex.Single
import kotlin.math.max
import kotlin.math.round

class CollageService {

    companion object {
        val COLOR_MASK = "#EFEFEF".toColorInt()// panelBackground, c ALFA_8 в любом случае будет черным.
    }

    fun createCollageMask(maskSource: Bitmap, scaleRatio: Float): Single<List<DataBitmapWrapper>> {
        val w = maskSource.width
        val h = maskSource.height
        val pixels = IntArray(w * h)
        maskSource.getPixels(pixels, 0, w, 0, 0, w, h)

        val singleRed = singleFrom { findMask(pixels, w, h, Color.RED) }
        val singleGreen = singleFrom { findMask(pixels, w, h, Color.GREEN) }
        val singleBlue = singleFrom { findMask(pixels, w, h, Color.BLUE) }

        return Single.zip(singleRed, singleGreen, singleBlue) { redMask, greenMask, blueMask ->
            val masks = listOf(redMask, greenMask, blueMask).filterIsInstance<DataBitmapWrapper>()
            for (bitmapWrapper in masks) correctScaleRatio(scaleRatio, bitmapWrapper.bounds)

            return@zip masks
        }
    }

    private fun correctScaleRatio(scaleRatio: Float, bounds: FloatArray) {
        val correct = 1f
        for (i in bounds.indices) {
            var value = round(bounds[i] * scaleRatio)

            //Делаю границы чуть больше чтобы не было пустого пространства между маской и коллажом
            if (i < 2) value = max(value - correct, 0F) // Первый x, y
            else value += correct // Последний x, y

            bounds[i] = value
        }
    }

    private fun findMask(pixels: IntArray, width: Int, height: Int, findingColor: Int): BitmapWrapper {
        var colorArray: IntArray? = null

        for (i in pixels.indices) {
            if (pixels[i] == Color.TRANSPARENT) continue

            if (findingColor == getCorrectColor(pixels[i])) {
                if (colorArray == null) colorArray = IntArray(pixels.size)

                colorArray[i] = COLOR_MASK
            }
        }

        return if (colorArray != null) trim(colorArray, width, height) else EmptyBitmapWrapper
    }

    private fun getCorrectColor(color: Int): Int {
        val r = getColorIfThisMax(Color.red(color))
        val g = getColorIfThisMax(Color.green(color))
        val b = getColorIfThisMax(Color.blue(color))

        return Color.argb(255, r, g, b)
    }

    private fun getColorIfThisMax(color: Int): Int = if (color == 255) color else 0

    private fun trim(pixels: IntArray, width: Int, height: Int): DataBitmapWrapper {
        var firstX = 0
        var firstY = 0
        var lastX = width
        var lastY = height

        loop@ for (x in 0 until width) {
            for (y in 0 until height) {
                if (pixels[x + y * width] != Color.TRANSPARENT) {
                    firstX = x
                    break@loop
                }
            }
        }
        loop@ for (y in 0 until height) {
            for (x in firstX until width) {
                if (pixels[x + y * width] != Color.TRANSPARENT) {
                    firstY = y
                    break@loop
                }
            }
        }
        loop@ for (x in width - 1 downTo firstX) {
            for (y in height - 1 downTo firstY) {
                if (pixels[x + y * width] != Color.TRANSPARENT) {
                    lastX = x
                    break@loop
                }
            }
        }
        loop@ for (y in height - 1 downTo firstY) {
            for (x in width - 1 downTo firstX) {
                if (pixels[x + y * width] != Color.TRANSPARENT) {
                    lastY = y
                    break@loop
                }
            }
        }

        val bitmap = Bitmap.createBitmap(
                pixels,
                width * firstY + firstX,
                width,
                lastX - firstX,
                lastY - firstY,
                Bitmap.Config.ALPHA_8
        )
        val bounds = floatArrayOf(firstX.toFloat(), firstY.toFloat(), lastX.toFloat(), lastY.toFloat())

        return DataBitmapWrapper(bounds, bitmap)
    }

}
