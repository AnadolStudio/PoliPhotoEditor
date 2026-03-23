package art.intel.soft.ui.edit.filter.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import art.intel.soft.R
import art.intel.soft.ui.edit.Action
import art.intel.soft.ui.edit.SimpleAdapter
import art.intel.soft.ui.edit.SimpleViewHolder

class FilterAdapter(
        data: List<FilterDataItem>,
        detailable: Action<FilterDataItem>,
        val thumbnailBitmap: Bitmap,
        private val intensityAction: () -> Unit
) : SimpleAdapter<FilterDataItem>(data, detailable) {

    override fun getViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder<FilterDataItem> =
            FilterViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                            R.layout.item_filter_list,
                            parent,
                            false
                    ),
                    selectableController,
                    action,
                    intensityAction,
                    thumbnailBitmap,
            )

    fun getGroupFromIndex(index: Int): FilterGroup = dataList[index].group
}
