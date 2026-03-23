package art.intel.soft.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import art.intel.soft.R
import art.intel.soft.databinding.ViewCommonBottomDialogButtonBinding

class CommonBottomDialogButton @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewCommonBottomDialogButtonBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        initAttr(attrs, context)
    }

    fun setText(text: String?) {
        binding.title.setText(text)
    }

    private fun initAttr(attrs: AttributeSet?, context: Context) {
        if (attrs == null) return

        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.CommonBottomDialogButton)

        setText(typeArray.getString(R.styleable.CommonBottomDialogButton_text))

        typeArray.recycle()
    }

}
