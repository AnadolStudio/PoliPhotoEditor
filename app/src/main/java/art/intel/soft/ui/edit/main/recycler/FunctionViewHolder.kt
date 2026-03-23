package art.intel.soft.ui.edit.main.recycler

import android.view.View
import androidx.core.content.ContextCompat
import art.intel.soft.databinding.ItemMainFunctionBinding
import art.intel.soft.ui.edit.Action
import art.intel.soft.ui.edit.SimpleViewHolder
import art.intel.soft.ui.selectablecontroller.SelectableController

class FunctionViewHolder(
        view: View,
        controller: SelectableController<in SimpleViewHolder<FunctionItem>>,
        detailable: Action<FunctionItem>
) : SimpleViewHolder<FunctionItem>(view, controller, detailable) {

    private val binding: ItemMainFunctionBinding = ItemMainFunctionBinding.bind(itemView)

    override fun onBind(data: FunctionItem) {
        binding.icon.setImageDrawable(
                data.drawableId.let { ContextCompat.getDrawable(itemView.context, it) }
        )

        binding.text.setText(data.textId)
        super.onBind(data)
    }
}
