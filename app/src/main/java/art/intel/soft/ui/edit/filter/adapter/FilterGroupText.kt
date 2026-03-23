package art.intel.soft.ui.edit.filter.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import art.intel.soft.R
import art.intel.soft.databinding.ViewFilterGroupTextBinding

class FilterGroupText(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    val binding: ViewFilterGroupTextBinding = ViewFilterGroupTextBinding.bind(
            LayoutInflater.from(context).inflate(R.layout.view_filter_group_text, this, true)
    )

    val textView = binding.textView
}
