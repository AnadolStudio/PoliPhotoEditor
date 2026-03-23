package art.intel.soft.extention

import android.view.View
import art.intel.soft.BuildConfig

fun View.debugLongClick(action: () -> Unit) {
    if (!BuildConfig.DEBUG) return

    setOnLongClickListener { true.also { action.invoke() } }
}
