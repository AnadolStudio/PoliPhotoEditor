package art.intel.soft.ui.edit.form

import android.graphics.Bitmap
import art.intel.soft.base.event.SingleCustomEvent

sealed class FormEvent : SingleCustomEvent() {

    object Loading : FormEvent()

    class ShowForm(val form: Bitmap, val mask: Bitmap) : FormEvent()

}
