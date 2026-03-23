package art.intel.soft.ui.edit.collage

import android.graphics.Bitmap

sealed class BitmapWrapper

class DataBitmapWrapper(val bounds: FloatArray, var bitmap: Bitmap) : BitmapWrapper()

object EmptyBitmapWrapper : BitmapWrapper()
