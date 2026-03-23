package art.intel.soft.ui.edit.improve.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import art.intel.soft.R
import art.intel.soft.databinding.ItemImproveBinding
import art.intel.soft.ui.edit.Action
import art.intel.soft.ui.edit.SimpleAdapter
import art.intel.soft.ui.edit.SimpleViewHolder
import art.intel.soft.ui.edit.improve.ImproveItem
import art.intel.soft.ui.selectablecontroller.SelectableController

class ImproveListAdapter(
        dataList: MutableList<ImproveItem>,
        detailable: Action<ImproveItem>
) : SimpleAdapter<ImproveItem>(dataList, detailable) {

    override fun getViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): SimpleViewHolder<ImproveItem> = AdjustmentViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_improve, parent, false),
            selectableController, action
    )

    private class AdjustmentViewHolder(
            view: View,
            controller: SelectableController<in SimpleViewHolder<ImproveItem>>,
            detailable: Action<ImproveItem>
    ) : SimpleViewHolder<ImproveItem>(view, controller, detailable) {

        private val binding = ItemImproveBinding.bind(view)

        override fun onBind(data: ImproveItem, isSelected: Boolean) {
            binding.icon.setImageDrawable(ContextCompat.getDrawable(itemView.context, data.drawableId))
            binding.text.setText(data.textId)
            super.onBind(data, isSelected)
        }

        override fun selectView(isSelected: Boolean) = Unit
    }

}
