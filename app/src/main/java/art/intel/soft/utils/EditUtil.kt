package art.intel.soft.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Environment.DIRECTORY_PICTURES
import android.os.Environment.getExternalStoragePublicDirectory
import android.util.Log
import art.intel.soft.R
import java.io.File

private const val TAG: String = "EditUtil"
private const val APP_DIRECTORY = "PhotoEditor"
private const val MAX_HEIGHT_WM = 0.1f
private const val MAX_WIDTH_WM = 0.1f
private const val PADDING_WM = 0.025f

// TODO
//  1) хуево назван класс
//  2) Тут собрана логика разных сущностей

fun getAppDir(): File =
        with(File(getExternalStoragePublicDirectory())) {
            if (!exists() && !isDirectory)
                if (!mkdirs()) Log.e(TAG, "Unable to create app dir!")// create empty directory
            this
        }

fun addWaterMark(source: Bitmap, resources: Resources): Bitmap =
        with(source) {

            val result = Bitmap.createBitmap(width, height, config)
            val canvas = Canvas(result)
            canvas.drawBitmap(this, 0f, 0f, null)

            BitmapFactory.decodeResource(resources, R.drawable.watermark).let { mark ->
                var scale = height * MAX_HEIGHT_WM / mark.height

                if (mark.width * scale / width > MAX_WIDTH_WM) scale = width * MAX_WIDTH_WM / mark.width

                val scaledWidthWM = (mark.width * scale).toInt()
                val scaledHeightWM = (mark.height * scale).toInt()

                val padding = (width * PADDING_WM).toInt()

                canvas.drawBitmap(
                        Bitmap.createScaledBitmap(mark, scaledWidthWM, scaledHeightWM, true),
                        (width - scaledWidthWM - padding).toFloat(),
                        (height - scaledHeightWM - padding).toFloat(),
                        null
                )

                return@with result
            }
        }

private fun getExternalStoragePublicDirectory() =
        "${getExternalStoragePublicDirectory(DIRECTORY_PICTURES)}${File.separator}${APP_DIRECTORY}"
