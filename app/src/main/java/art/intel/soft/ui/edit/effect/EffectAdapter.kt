package art.intel.soft.ui.edit.effect

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import art.intel.soft.R
import art.intel.soft.ui.edit.Action
import art.intel.soft.ui.edit.SimpleAdapter
import art.intel.soft.ui.edit.SimpleViewHolder

class EffectAdapter(
        private val thumbnail: Bitmap,
        data: List<String?>,
        action: Action<String?>,
        private val detailClick: () -> Unit
) : SimpleAdapter<String?>(data, action) {

    override fun getViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder<String?> = EffectViewHolder(
            thumbnail,
            LayoutInflater.from(parent.context).inflate(R.layout.item_effects, parent, false),
            selectableController,
            action,
            detailClick
    )
}
