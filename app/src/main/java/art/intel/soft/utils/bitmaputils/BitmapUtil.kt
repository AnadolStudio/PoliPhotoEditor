package art.intel.soft.utils.bitmaputils

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import art.intel.soft.utils.getAppDir
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val TAG = "BitmapUtil"

fun centerCrop(source: Bitmap): Bitmap = with(source) {
    val minSide = min(width, height)
    val maxSide = max(width, height)
    val x = if (width >= height) maxSide / 2 - minSide / 2 else 0
    val y = if (width >= height) 0 else maxSide / 2 - minSide / 2

    Bitmap.createBitmap(source, x, y, minSide, minSide)
}

fun Bitmap.totalPixels(): Int = width * height

fun Bitmap.getPixels(): IntArray {
    val pixels = IntArray(width * height)
    getPixels(pixels, 0, width, 0, 0, width, height)

    return pixels
}

fun isBrightColor(color: Int): Boolean {
    if (Color.TRANSPARENT == color) return true

    val rgb = intArrayOf(Color.red(color), Color.green(color), Color.blue(color))
    val brightness = sqrt(rgb[0] * rgb[0] * 0.241 + (rgb[1] * rgb[1] * 0.691) + rgb[2] * rgb[2] * 0.068).toInt()

    // color is light
    if (brightness >= 200) {
        return true
    }

    return false
}

fun getXSpace(a: Bitmap, b: Bitmap) = getSpace(a.width, b.width)

fun getSpace(a: Int, b: Int) = abs((a - b) / 2)

fun getYSpace(one: Bitmap, two: Bitmap) = abs((two.height - one.height) / 2)

fun cropFromSource(width: Int, height: Int, x: Int, y: Int, source: Bitmap): Bitmap =
        Bitmap.createBitmap(source, x, y, width, height)

fun scaleBitmap(main: Bitmap, support: Bitmap): Bitmap =
        scaleBitmap(main.width.toFloat(), main.height.toFloat(), support, true)

fun createScaledBitmap(bitmap: Bitmap, scale: Float): Bitmap = Bitmap.createScaledBitmap(
        bitmap,
        (bitmap.width * scale).roundToInt(),
        (bitmap.height * scale).roundToInt(),
        true
)

fun scaleBitmap(mainW: Float, mainH: Float, support: Bitmap): Bitmap =
        scaleBitmap(mainW, mainH, support, true)

fun scaleBitmap(main: Bitmap, support: Bitmap, isHard: Boolean): Bitmap =
        scaleBitmap(main.width.toFloat(), main.height.toFloat(), support, isHard)

fun scaleBitmap(mainW: Float, mainH: Float, support: Bitmap, isHard: Boolean): Bitmap =
        with(support) {
            val scaleRatio = getScaleRatio(
                    mainW, mainH,
                    width.toFloat(), height.toFloat()
            )

            val scaleW = (if (isHard) mainW else width.toFloat() * scaleRatio).toInt()
            val scaleH = (if (isHard) mainH else height.toFloat() * scaleRatio).toInt()

            Bitmap.createScaledBitmap(support, scaleW, scaleH, true)
        }

//TODO когда нужен max, а когда min?
fun getScaleRatio(mainW: Float, mainH: Float, supportW: Float, supportH: Float) =
        if (supportW > mainW && supportH > mainH)
            min(mainW / supportW, mainH / supportH)
        else
            max(mainW / supportW, mainH / supportH)

fun getScaleRatioMax(mainW: Float, mainH: Float, supportW: Float, supportH: Float) =
        max(mainW / supportW, mainH / supportH)

fun getScaleRatioMax(mainW: Int, mainH: Int, supportW: Int, supportH: Int) =
        getScaleRatioMax(mainW.toFloat(), mainH.toFloat(), supportW.toFloat(), supportH.toFloat())

fun getScaleRatio(current: Float, defaultInt: Float): Float = defaultInt / current

fun scaleRatioCircumscribed(
        mainW: Int, mainH: Int, supportW: Int, supportH: Int
): Float = scaleRatioCircumscribed(mainW.toFloat(), mainH.toFloat(), supportW.toFloat(), supportH.toFloat())

fun scaleRatioCircumscribed(
        mainW: Float, mainH: Float, supportW: Float, supportH: Float
): Float = max(mainW / supportW, mainH / supportH)

fun scaleRatioInscribed(
        mainW: Int, mainH: Int, supportW: Int, supportH: Int
): Float = scaleRatioInscribed(mainW.toFloat(), mainH.toFloat(), supportW.toFloat(), supportH.toFloat())

fun scaleRatioInscribed(mainW: Float, mainH: Float, supportW: Float, supportH: Float): Float =
        min(mainW / supportW, mainH / supportH)

fun inscribedBitmap(defaultWidth: Int, defaultHeight: Int, bitmap: Bitmap): Bitmap {
    val scaleRatio = scaleRatioInscribed(defaultWidth, defaultHeight, bitmap.width, bitmap.height)
    val width = bitmap.width * scaleRatio
    val height = bitmap.height * scaleRatio

    return Bitmap.createScaledBitmap(bitmap, width.toInt(), height.toInt(), true)
}

fun captureView(view: View) = captureView(view.width, view.height, view)

fun captureViewMeasured(view: View) = captureView(view.measuredWidth, view.measuredHeight, view)

fun captureView(width: Int, height: Int, vararg views: View): Bitmap =
        with(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)) {
            val canvas = Canvas(this)
            views.forEach { it.draw(canvas) }
            this
        }

fun getCopyBitmap(originalBitmap: Bitmap, reqW: Int, reqH: Int): Bitmap =
        with(originalBitmap) {
            if (reqW <= 0 || reqH <= 0) return Bitmap.createBitmap(this)

            val point = calculateSizePicture(this, reqW, reqH)
            Bitmap.createScaledBitmap(this, point.x, point.y, true)
        }

private fun calculateSizePicture(src: Bitmap, reqW: Int, reqH: Int) =
        with(src) {
            var inSampleSize = 1
            if (height > reqH || width > reqW) {
                val tempHeight = height
                val tempWidth = width

                // Вычисляем наибольший inSampleSize, который будет кратным двум
                // и оставит полученные размеры больше, чем требуемые
                while (tempHeight / inSampleSize > reqH || tempWidth / inSampleSize > reqW) {
                    inSampleSize *= 2
                }
            }
            Point(width / inSampleSize, height / inSampleSize)
        }

fun getBitmapFromImageView(source: ImageView?) = source?.let { getBitmapFromDrawable(it.drawable) }

fun getBitmapFromDrawable(source: Drawable?) = (source as BitmapDrawable?)?.bitmap

@Throws(FileNotFoundException::class)
fun saveBitmapAsFile(context: Context?, originalBitmap: Bitmap, file: File): String =
        FileOutputStream(file).use { fos ->
            originalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            getInfoOfBitmap(originalBitmap)

            MediaScannerConnection.scanFile(
                    context, arrayOf(file.path), null
            ) { _: String?, _: Uri? -> Log.d(TAG, "onSuccess") }

            fos.flush()
            file.path
        }

fun readImage(context: Context, path: String): Bitmap {
    val contentUri = Uri.parse(path)
    var cursor: Cursor? = null
    var realPath = ""
    var orientation = 1
    try {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        cursor = context.contentResolver.query(contentUri, proj, null, null, null)
        if (cursor == null) throw IllegalArgumentException() // TODO Собственные исключения

        with(cursor) {
            val columnIndex = getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            moveToFirst()
            realPath = getString(columnIndex)
            orientation = ExifInterface(realPath).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        cursor?.close()
    }

    return with(getDegree(orientation)) {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        var bitmap = BitmapFactory.decodeFile(realPath, options)
        if (this != 0) bitmap = rotate(bitmap, this.toFloat())
        getInfoOfBitmap(bitmap)

        bitmap
    }
}

@Throws(IOException::class)
fun loadBitmapFromAssets(context: Context, path: String) {
    val inputStream = context.assets.open(path)
    BitmapFactory.decodeStream(inputStream)
}

fun saveImage(context: Context, bitmap: Bitmap): String =
        saveBitmapAsFile(context, bitmap, File(getAppDir(), getFileName()))



