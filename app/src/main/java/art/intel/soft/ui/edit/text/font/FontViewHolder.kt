package art.intel.soft.ui.edit.text.font

import android.util.TypedValue
import android.view.View
import art.intel.soft.databinding.ItemFontTextBinding
import art.intel.soft.ui.edit.Action
import art.intel.soft.ui.edit.SimpleViewHolder
import art.intel.soft.ui.selectablecontroller.SelectableController

class FontViewHolder(
        view: View,
        selectableController: SelectableController<SimpleViewHolder<Fonts>>,
        action: Action<Fonts>,
) : SimpleViewHolder<Fonts>(view, selectableController, action) {

    private companion object {
        const val DEFAULT_SIZE = 18F
        const val SELECTED_SIZE = 28F
    }

    private val binding: ItemFontTextBinding = ItemFontTextBinding.bind(itemView)

    override fun onBind(data: Fonts, isSelected: Boolean) {
        super.data = data

        binding.fontTextView.setTextAppearance(data.id)
        binding.fontTextView.text = data.fontName

        onBind(isSelected)
    }

    override fun onBind(isSelected: Boolean) {
        binding.fontTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, if (isSelected) SELECTED_SIZE else DEFAULT_SIZE)
    }

    override fun onClick(view: View) {
        if (controller.getCurrentPosition() == this.absoluteAdapterPosition && data != null) {
            detailable.action(data)
        } else {
            super.onClick(view)
        }
    }
}
