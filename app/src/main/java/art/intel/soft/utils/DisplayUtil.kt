package art.intel.soft.utils

import android.app.Activity
import android.graphics.Insets
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowInsets
import androidx.core.view.isVisible
import kotlin.math.max

object DisplayUtil {

    fun getScreenSize(activity: Activity): Point = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = activity.windowManager.currentWindowMetrics
        val insets: Insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        val width = windowMetrics.bounds.width() - insets.left - insets.right
        val height = windowMetrics.bounds.height() - insets.top - insets.bottom

        Point(width, height)
    } else {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val titleBarHeight = getStatusBar(activity)

        Point(displayMetrics.widthPixels, displayMetrics.heightPixels + titleBarHeight)
    }

    private fun getStatusBar(activity: Activity): Int {
        val rectangle = Rect()
        val window: Window = activity.window
        window.decorView.getWindowVisibleDisplayFrame(rectangle)
        val statusBarHeight: Int = rectangle.top
        val contentViewTop: Int = window.findViewById<View>(Window.ID_ANDROID_CONTENT).top

        return contentViewTop - statusBarHeight
    }

    // TODO need Test
    fun workspaceSize(
            activity: Activity, vararg views: View, checkWidth: Boolean = false, checkHeight: Boolean = true
    ): Point = with(views) {
        val result = getScreenSize(activity)

        if (isEmpty()) return result

        var width = 0
        var height = 0

        forEach {
            if (!it.isVisible) return@forEach

            if (checkWidth) width += it.width
            if (checkHeight) height += it.height
        }

        result.apply {
            x = max(x - width, 0)
            y = max(y - height, 0)
        }
    }
}
