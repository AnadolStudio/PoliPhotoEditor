package art.intel.soft.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import art.intel.soft.R
import art.intel.soft.databinding.ViewBaseCropButtonBinding

class BaseCropButtonView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewBaseCropButtonBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        initAttr(attrs, context)
    }

    private fun initAttr(attrs: AttributeSet?, context: Context) {
        if (attrs == null) return

        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.BaseCropButtonView)

        binding.imageView.setImageDrawable(typeArray.getDrawable(R.styleable.BaseCropButtonView_src))
        binding.textView.text = typeArray.getString(R.styleable.BaseCropButtonView_text)

        typeArray.recycle()
    }

}
