package art.intel.soft.ui.edit.text.font

import androidx.annotation.StyleRes
import art.intel.soft.R

enum class Fonts(val fontName: String, @StyleRes val id: Int) {
    RobotoRegular(fontName = "Roboto Regular", id = R.style.Roboto_Regular),
    RobotoBold(fontName = "Roboto Bold", id = R.style.Roboto_Bold),
    Exo2Bold(fontName = "Exo2 Bold", id = R.style.Exo2_Bold),
    Exo2Italic(fontName = "Exo2 Italic", id = R.style.Exo2_Italic),
    Exo2Regular(fontName = "Exo2 Regular", id = R.style.Exo2_Regular),
    FiraSansBold(fontName = "Fira Sans Bold", id = R.style.FiraSans_Bold),
    FiraSansItalic(fontName = "Fira Sans Italic", id = R.style.FiraSans_Italic),
    FiraSansRegular(fontName = "Fira Sans Regular", id = R.style.FiraSans_Regular),
    FrankRuhlLibreBold(fontName = "Frank Ruhl Libre Bold", id = R.style.FrankRuhlLibre_Bold),
    FrankRuhlLibreLight(fontName = "Frank Ruhl Libre Light", id = R.style.FrankRuhlLibre_Light),
    FrankRuhlLibreRegular(fontName = "Frank Ruhl Libre Regular", id = R.style.FrankRuhlLibre_Regular),
}
