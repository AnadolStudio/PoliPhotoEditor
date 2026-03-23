package art.intel.soft.ui.edit.text

import android.graphics.Color
import art.intel.soft.ui.edit.text.font.Fonts

data class TextItem(
        val text: String,
        val textColor: Int = Color.WHITE,
        val textAlpha: Int = 255,
        val backColor: Int = Color.WHITE,
        val backAlpha: Int = 0,
        val textColorSliderValue: Float = 0F,
        val backColorSliderValue: Float = 0F,
        val textFont: Fonts = Fonts.RobotoRegular,
)
