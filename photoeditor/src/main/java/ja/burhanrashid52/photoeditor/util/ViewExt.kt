package ja.burhanrashid52.photoeditor.util

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop

fun Int.dpToPx() = (this * Resources.getSystem().displayMetrics.density).toInt()
fun Int.pxToDp() = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Int.spToPx() = (this * Resources.getSystem().displayMetrics.scaledDensity).toInt()
fun Int.pxToSp() = (this / Resources.getSystem().displayMetrics.scaledDensity).toInt()

fun Float.spToPx() = (this * Resources.getSystem().displayMetrics.scaledDensity)
fun Float.pxToSp() = (this / Resources.getSystem().displayMetrics.scaledDensity)

fun Float.dpToPx() = (this * Resources.getSystem().displayMetrics.density)
fun Float.pxToDp() = (this / Resources.getSystem().displayMetrics.density)

fun View.setMarginsDp(start: Int? = marginStart, top: Int? = marginTop, end: Int? = marginEnd, bottom: Int? = marginBottom) =
        setMarginsPx(start = start?.dpToPx(), top = top?.dpToPx(), end = end?.dpToPx(), bottom = bottom?.dpToPx())

fun View.setMarginsPx(start: Int? = marginStart, top: Int? = marginTop, end: Int? = marginEnd, bottom: Int? = marginBottom) {
    val params = getMarginLayoutParams() ?: return

    params.setMargins(
            start ?: params.marginStart,
            top ?: params.topMargin,
            end ?: params.marginEnd,
            bottom ?: params.bottomMargin
    )
}

fun View.setMarginsPx(margin: Int) = setMarginsPx(start = margin, top = margin, end = margin, bottom = margin)

fun View.getMarginLayoutParams(): ViewGroup.MarginLayoutParams? = layoutParams as? ViewGroup.MarginLayoutParams
