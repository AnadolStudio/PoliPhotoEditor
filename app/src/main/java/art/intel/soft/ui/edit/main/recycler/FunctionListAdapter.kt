package art.intel.soft.ui.edit.main.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import art.intel.soft.R
import art.intel.soft.ui.edit.Action
import art.intel.soft.ui.edit.SimpleAdapter
import art.intel.soft.ui.edit.SimpleViewHolder

class FunctionListAdapter(
        data: List<FunctionItem>, detailable: Action<FunctionItem>
) : SimpleAdapter<FunctionItem>(data, detailable) {

    init {
        selectableMode = false
    }

    override fun getViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): SimpleViewHolder<FunctionItem> = FunctionViewHolder(
            LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_main_function, parent, false),
            selectableController,
            action
    )
}
