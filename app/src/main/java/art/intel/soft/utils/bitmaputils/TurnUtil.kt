package art.intel.soft.utils.bitmaputils

import android.graphics.Bitmap
import android.graphics.Matrix

private const val ROTATE_LEFT_90 = -90f
private const val ROTATE_RIGHT_90 = 90f

enum class FlipType {
    HORIZONTAL, VERTICAL
}

fun Bitmap.flipVertical(): Bitmap = flip(this, FlipType.VERTICAL)

fun Bitmap.flipHorizontal(): Bitmap = flip(this, FlipType.HORIZONTAL)

private fun flip(source: Bitmap, type: FlipType): Bitmap = Bitmap.createBitmap(
        source, 0, 0,
        source.width, source.height,
        getFlipMatrix(source, type), true
)

private fun getFlipMatrix(source: Bitmap, type: FlipType): Matrix {
    val matrix = Matrix()
    val xFlip = if (type == FlipType.HORIZONTAL) -1 else 1
    val yFlip = if (type == FlipType.HORIZONTAL) 1 else -1
    matrix.postScale(xFlip.toFloat(), yFlip.toFloat(), source.width / 2f, source.height / 2f)
    return matrix
}

fun Bitmap.rotateLeft(): Bitmap = rotate(this, ROTATE_LEFT_90)

fun Bitmap.rotateRight(): Bitmap = rotate(this, ROTATE_RIGHT_90)

//Если хочешь кастомный градус, то используй editor().setScaleType(ImageView.ScaleType.MATRIX);
fun rotate(source: Bitmap, degree: Float): Bitmap = Bitmap.createBitmap(
        source, 0, 0,
        source.width, source.height,
        getRotateMatrix(degree), true
)

private fun getRotateMatrix(degree: Float): Matrix {
    val matrix = Matrix()
    matrix.postRotate(degree)
    return matrix
}
