package art.intel.soft.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import art.intel.soft.R
import art.intel.soft.databinding.ViewSelectebleImageButtonBinding

class SelectableImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewSelectebleImageButtonBinding =
            ViewSelectebleImageButtonBinding.inflate(LayoutInflater.from(context), this, true)

    var onSelectedListener: ((SelectableImageView) -> Unit)? = null

    var isImageSelected: Boolean = false
        set(value) {
            field = value
            binding.selectableDivider.visibility = if (value) VISIBLE else INVISIBLE
            if (value) onSelectedListener?.invoke(this)
        }

    init {
        initAttr(attrs, context)
    }

    private fun initAttr(attrs: AttributeSet?, context: Context) {
        if (attrs == null) return

        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.SelectableImageView)

        setImage(typeArray.getDrawable(R.styleable.SelectableImageView_src))
        isImageSelected = typeArray.getBoolean(R.styleable.SelectableImageView_isImageSelected, false)
        setSelectableColor(typeArray.getColor(R.styleable.SelectableImageView_selectedColor, Color.BLACK))
        setOnClickListener(null)

        typeArray.recycle()
    }

    fun setSelectableColor(color: Int) {
        binding.selectableDivider.backgroundTintList = ColorStateList.valueOf(color)
    }

    fun setImage(drawable: Drawable?) = binding.imageView.setImageDrawable(drawable)

    override fun setOnClickListener(l: OnClickListener?) = super.setOnClickListener {
        if (!isImageSelected) isImageSelected = !isImageSelected
        l?.onClick(this)
    }
}
