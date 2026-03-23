package art.intel.soft.ui.edit.body

import android.app.Application
import android.graphics.Bitmap
import art.intel.soft.base.view_model.BaseAndroidViewModel
import art.intel.soft.extention.baseSubscribe
import art.intel.soft.extention.singleFrom
import art.intel.soft.utils.bitmaputils.getPixels
import art.intel.soft.utils.bitmaputils.isBrightColor
import art.intel.soft.utils.bitmaputils.totalPixels
import art.intel.soft.utils.onNext

class BodyViewModel(application: Application) : BaseAndroidViewModel<BodyState>(application) {

    fun analysisBrightnessPhoto(bitmap: Bitmap) {
        _state.onNext(BodyState.Loading)

        singleFrom {
            var count = 0

            bitmap.getPixels().forEach { pixel ->
                if (isBrightColor(pixel)) count++
            }

            return@singleFrom count >= bitmap.totalPixels() / 2
        }.baseSubscribe(
                { photoIsBright ->
                    _state.onNext(BodyState.Content(photoIsBright = photoIsBright))
                },
                { _ ->
                    _state.onNext(BodyState.Content(photoIsBright = true))
                }
        ).disposeOnViewModelDestroy()
    }


}
