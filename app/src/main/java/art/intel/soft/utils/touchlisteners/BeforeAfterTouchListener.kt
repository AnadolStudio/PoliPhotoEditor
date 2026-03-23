package art.intel.soft.utils.touchlisteners

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View

typealias TouchListener = (touch: Boolean) -> Unit

class BeforeAfterTouchListener(val listener: TouchListener) : View.OnTouchListener {

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        if (event == null) return false

        when (event.action and event.actionMasked) {
            MotionEvent.ACTION_DOWN -> listener(true)
            MotionEvent.ACTION_UP -> listener(false)
        }
        return true
    }
}
