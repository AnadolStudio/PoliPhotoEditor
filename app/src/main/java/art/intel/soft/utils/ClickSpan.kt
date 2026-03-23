package art.intel.soft.utils

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt

class ClickSpan(
        private val withUnderline: Boolean,
        private val listener: (() -> Unit)
) : ClickableSpan() {

    override fun onClick(widget: View) {
        listener.invoke()
    }

    override fun updateDrawState(paint: TextPaint) {
        super.updateDrawState(paint)
        paint.isUnderlineText = withUnderline
    }

    companion object {

        fun clickify(
                view: TextView,
                clickableText: String,
                withUnderline: Boolean,
                isBold: Boolean,
                @ColorInt color: Int,
                listener: (() -> Unit)
        ) {
            val charSequence = view.text

            val (start, end) = getIndices(charSequence.toString(), clickableText)
            if (start == -1) {
                return
            }

            val spannableString = if (charSequence is Spannable) {
                charSequence
            } else {
                SpannableString.valueOf(charSequence)
            }

            spannableString.setClickAction(ClickSpan(withUnderline, listener), start, end)
            spannableString.setColor(color, start, end)
            if (isBold) {
                spannableString.setBold(start, end)
            }

            view.text = spannableString
            val movementMethod = view.movementMethod
            if (movementMethod !is LinkMovementMethod) {
                view.movementMethod = LinkMovementMethod.getInstance()
            }
        }

        private fun getIndices(source: String, part: String): Pair<Int, Int> {
            val startIndex = source.indexOf(part)
            val endIndex = startIndex + part.length

            return startIndex to endIndex
        }

        private fun Spannable.setClickAction(clickableSpan: ClickableSpan, start: Int, end: Int) =
                setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        private fun Spannable.setColor(color: Int, start: Int, end: Int) =
                setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        private fun Spannable.setBold(start: Int, end: Int) =
                setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}
