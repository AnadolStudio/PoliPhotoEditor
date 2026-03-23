package art.intel.soft.utils

import android.widget.TextView
import com.google.android.material.slider.Slider
import java.util.Collections

fun TextView.setTextAnimation(
        text: String,
        duration: Long = AnimateUtil.DURATION_SHORT,
        completion: (() -> Unit)? = null
) {
    if (this.text.equals(text)) return
    AnimateUtil.fadOutAnimation(this, duration) {
        this.text = text
        AnimateUtil.fadInAnimation(this, duration) { completion?.let { it() } }
    }
}

fun <E> MutableList<E>.swap(i: Int, j: Int) = Collections.swap(this, i, j)

fun Slider.setup(from: Float, to: Float, value: Float = (to + from) / 2F) {
    valueFrom = from
    valueTo = to
    setValue(value)
}

// TODO Поиск координат центра для View
