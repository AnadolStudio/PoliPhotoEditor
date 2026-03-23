package art.intel.soft.extention

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.angcyo.dsladapter.className

fun Context.openUrl(@StringRes urlId: Int) = openUrl(getString(urlId))

fun Context.openUrl(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (exception: ActivityNotFoundException) {
        Log.e(exception.className(), "Can't open uri", exception)
    }
}

fun Context.compatDrawable(@DrawableRes id: Int): Drawable = requireNotNull(ContextCompat.getDrawable(this, id))
