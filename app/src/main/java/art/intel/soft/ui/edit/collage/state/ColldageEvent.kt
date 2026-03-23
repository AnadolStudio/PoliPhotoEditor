package art.intel.soft.ui.edit.collage.state

import android.graphics.Bitmap
import art.intel.soft.base.event.SingleCustomEvent

class InitCollageEvent(val collagePath: String, val maskPath: String) : SingleCustomEvent()

class LoadedPhotoEvent(val maskId: Int, val photo: Bitmap) : SingleCustomEvent()
