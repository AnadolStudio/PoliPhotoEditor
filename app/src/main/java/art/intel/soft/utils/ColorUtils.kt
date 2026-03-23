package art.intel.soft.utils

import android.graphics.Color

object ColorUtils {

    fun getColorWithAlpha(color: Int, alpha: Int): Int =
            Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

}
