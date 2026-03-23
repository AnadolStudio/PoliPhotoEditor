package art.intel.soft.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import art.intel.soft.R
import art.intel.soft.base.BaseAction
import art.intel.soft.databinding.ViewBaseToolbarBinding
import art.intel.soft.utils.throttleClick

class BaseToolbar @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewBaseToolbarBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        initAttr(attrs, context)
    }

    private fun initAttr(attrs: AttributeSet?, context: Context) {
        if (attrs == null) return

        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.BaseToolbar)

        val title = typeArray.getString(R.styleable.BaseToolbar_text)
        title?.let(this::setTitle)
        val rightIcon = typeArray.getDrawable(R.styleable.BaseToolbar_rightIcon)
        rightIcon?.let(this::setRightButtonIcon)

        typeArray.recycle()
    }

    fun setTitle(text: String?) {
        binding.titleText.text = text
    }

    fun setTitle(textRes: Int?) {
        binding.titleText.text = textRes?.let { context.getString(it) }
    }

    fun setLeftButtonAction(action: BaseAction) = binding.leftButton.throttleClick { action.invoke() }

    fun setRightButtonAction(action: BaseAction) = binding.rightButton.throttleClick { action.invoke() }

    fun setLeftButtonIcon(drawable: Drawable?) = binding.leftButton.setImageDrawable(drawable)

    fun setRightButtonIcon(drawable: Drawable?) = binding.rightButton.setImageDrawable(drawable)
}
