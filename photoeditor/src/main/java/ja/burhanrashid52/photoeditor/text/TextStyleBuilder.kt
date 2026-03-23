package ja.burhanrashid52.photoeditor.text

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.widget.TextView
import java.util.EnumMap

class TextStyleBuilder {

    companion object {
        const val DEFAULT_SIZE = 18F
    }

    private val values: MutableMap<TextStyle, Any> = EnumMap(TextStyle::class.java)

    fun withTextSize(size: Float) = values.set(TextStyle.SIZE, size)

    fun withTextColor(color: Int) = values.set(TextStyle.COLOR, color)

    fun withTextFont(textTypeface: Typeface) = values.set(TextStyle.FONT_FAMILY, textTypeface)

    fun withGravity(gravity: Int) = values.set(TextStyle.GRAVITY, gravity)

    fun withBackgroundColor(background: Int) = values.set(TextStyle.BACKGROUND, background)

    fun withBackgroundDrawable(bgDrawable: Drawable) = values.set(TextStyle.BACKGROUND, bgDrawable)

    fun withTextAppearance(textAppearance: Int) = values.set(TextStyle.TEXT_APPEARANCE, textAppearance)

    fun withTextStyle(typeface: Int) = values.set(TextStyle.TEXT_STYLE, typeface)

    fun withTextFlag(paintFlag: Int) = values.set(TextStyle.TEXT_FLAG, paintFlag)

    fun withTextShadow(radius: Float, dx: Float, dy: Float, color: Int) = withTextShadow(TextShadow(radius, dx, dy, color))

    fun withTextShadow(textShadow: TextShadow) = values.set(TextStyle.SHADOW, textShadow)

    fun withTextBorder(textBorder: TextBorder) = values.set(TextStyle.BORDER, textBorder)

    fun applyStyle(textView: TextView) {
        for ((key, value) in values) {
            when {
                key == TextStyle.SIZE -> applyTextSize(textView, value as Float)
                key == TextStyle.COLOR -> applyTextColor(textView, value as Int)
                key == TextStyle.FONT_FAMILY -> applyFontFamily(textView, value as Typeface)
                key == TextStyle.GRAVITY -> applyGravity(textView, value as Int)
                key == TextStyle.BACKGROUND -> applyBackground(textView, value)
                key == TextStyle.TEXT_APPEARANCE && value is Int -> applyTextAppearance(textView, value)
                key == TextStyle.TEXT_STYLE -> applyTextStyle(textView, value as Int)
                key == TextStyle.TEXT_FLAG -> applyTextFlag(textView, value as Int)
                key == TextStyle.SHADOW && value is TextShadow -> applyTextShadow(textView, value)
                key == TextStyle.BORDER && value is TextBorder -> applyTextBorder(textView, value)
            }
        }
    }

    protected fun applyTextSize(textView: TextView, size: Float) = textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size)

    protected fun applyTextShadow(textView: TextView, radius: Float, dx: Float, dy: Float, color: Int) =
            textView.setShadowLayer(radius, dx, dy, color)

    protected fun applyTextColor(textView: TextView, color: Int) = textView.setTextColor(color)

    protected fun applyFontFamily(textView: TextView, typeface: Typeface?) = textView.setTypeface(typeface)

    protected fun applyGravity(textView: TextView, gravity: Int) = textView.setGravity(gravity)

    private fun applyBackground(textView: TextView, value: Any) {
        when (value) {
            is Drawable -> applyBackgroundDrawable(textView, value)
            is Int -> applyBackgroundColor(textView, value)
        }
    }

    protected fun applyBackgroundColor(textView: TextView, color: Int) = textView.setBackgroundColor(color)

    protected fun applyBackgroundDrawable(textView: TextView, background: Drawable?) = textView.setBackground(background)

    protected fun applyTextBorder(textView: TextView, textBorder: TextBorder) {
        textView.background = GradientDrawable().apply {
            cornerRadius = textBorder.corner
            setStroke(textBorder.strokeWidth, textBorder.strokeColor)
            setColor(textBorder.backGroundColor)
        }
    }

    protected fun applyTextShadow(textView: TextView, textShadow: TextShadow) =
            textView.setShadowLayer(textShadow.radius, textShadow.dx, textShadow.dy, textShadow.color)

    // bold or italic
    protected fun applyTextStyle(textView: TextView, typeface: Int) = textView.setTypeface(textView.typeface, typeface)

    // underline or strike
    protected fun applyTextFlag(textView: TextView, flag: Int) = textView.paint.setFlags(flag)

    protected fun applyTextAppearance(textView: TextView, styleAppearance: Int) = textView.setTextAppearance(styleAppearance)

}
