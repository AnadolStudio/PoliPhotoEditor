package art.intel.soft.utils.bitmaputils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import art.intel.soft.utils.getTime
import java.util.Locale

private const val TAG = "InfoBitmap"

fun getInfoOfBitmap(bitmap: Bitmap) {
    Log.d(
            TAG,
            "bitmap size = ${bitmap.width}x${bitmap.height}, byteCount = ${bitmap.byteCount}, total = ${bitmap.height * bitmap.width}"
    )

    Log.d(
            TAG,
            "bitmap size = ${bitmap.width}x${bitmap.height}, byteCount = ${bitmap.byteCount}, total = ${bitmap.height * bitmap.width}"
    )
    logMemory()
}

fun logMemory() {
    Log.i(
            TAG,
            "Used memory = %${
                ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
                        .freeMemory()) / 1024).toInt()
            }"
    )
    Log.i(TAG, "Total memory = ${(Runtime.getRuntime().totalMemory() / 1024).toInt()}")
}

fun getMimeType(activity: AppCompatActivity, uri: Uri): String? =
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) activity.contentResolver.getType(uri)
        else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(fileExtension.lowercase(Locale.getDefault()))
        }

fun getFileName(): String = "IMG_${getTime()}.jpeg"

fun getDegree(orientation: Int): Int = when (orientation) {
    ExifInterface.ORIENTATION_ROTATE_90 -> 90
    ExifInterface.ORIENTATION_ROTATE_180 -> 180
    ExifInterface.ORIENTATION_ROTATE_270 -> 270
    else -> 0
}




