package art.intel.soft.ui.edit.crop

import androidx.annotation.DrawableRes
import art.intel.soft.R

enum class RatioText(
        val value: String,
        @DrawableRes var drawableUnselectedId: Int,
        val aspectRatio: AspectRatio
) {

    FREE("Custom", R.drawable.ic_aspect_ratio_own, AspectRatio()),
    RATIO_1_1("Size_1x1", R.drawable.ic_aspect_ratio_1_1, AspectRatio(1, 1)),
    RATIO_9_16("Size_9x16", R.drawable.ic_aspect_ratio_9_16, AspectRatio(9, 16)),
    RATIO_16_9("Size_1x_9", R.drawable.ic_aspect_ratio_16_9, AspectRatio(16, 9)),
    RATIO_3_4("Size_3x4", R.drawable.ic_aspect_ratio_3_4, AspectRatio(3, 4)),
    RATIO_4_3("Size_4x3", R.drawable.ic_aspect_ratio_4_3, AspectRatio(4, 3))
}

data class AspectRatio(
        val aspectX: Int = 0,
        val aspectY: Int = 0
)
