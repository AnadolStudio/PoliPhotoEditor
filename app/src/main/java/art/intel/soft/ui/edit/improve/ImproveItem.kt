package art.intel.soft.ui.edit.improve

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import art.intel.soft.R

enum class ImproveItem(
        @StringRes val textId: Int,
        @DrawableRes var drawableId: Int,
) {

    CURVE(R.string.improve_func_curves, R.drawable.ic_curve),
    SEL_COLOR(R.string.improve_func_selcolor, R.drawable.ic_selcolor),
    BRIGHTNESS(R.string.improve_func_brightness, R.drawable.ic_brightness),
    CONTRAST(R.string.improve_func_contrast, R.drawable.ic_contrast),
    SATURATION(R.string.improve_func_saturation, R.drawable.ic_saturation),
    WARMTH(R.string.improve_func_warmth, R.drawable.ic_thermometer);

}

fun ImproveItem.toFunctionMode(): ImproveContainer.FunctionMode = when (this) {
    ImproveItem.BRIGHTNESS -> ImproveContainer.FunctionMode.BRIGHTNESS
    ImproveItem.CONTRAST -> ImproveContainer.FunctionMode.CONTRAST
    ImproveItem.SATURATION -> ImproveContainer.FunctionMode.SATURATION
    ImproveItem.WARMTH -> ImproveContainer.FunctionMode.WARMTH
    ImproveItem.SEL_COLOR -> ImproveContainer.FunctionMode.SELECT_COLOR
    ImproveItem.CURVE -> ImproveContainer.FunctionMode.CURVE
}
