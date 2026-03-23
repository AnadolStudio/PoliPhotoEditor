package art.intel.soft.ui.edit.background

import android.graphics.Bitmap
import art.intel.soft.base.event.SingleCustomEvent

sealed class BackgroundEvent : SingleCustomEvent() {

    class ClearEvent(val mask: Bitmap?) : BackgroundEvent()

    object MaskNotExistEvent : BackgroundEvent()

}
