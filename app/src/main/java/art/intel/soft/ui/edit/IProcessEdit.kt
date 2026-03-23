package art.intel.soft.ui.edit

import android.graphics.Bitmap

interface IProcessEdit {

    fun process(main: Bitmap, support: Bitmap?): Bitmap
}
