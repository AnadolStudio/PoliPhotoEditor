package art.intel.soft.ui.edit.sticker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import art.intel.soft.R
import art.intel.soft.databinding.ItemStickerBinding
import art.intel.soft.ui.edit.Action
import art.intel.soft.ui.edit.SimpleAdapter
import art.intel.soft.ui.edit.SimpleViewHolder
import art.intel.soft.ui.selectablecontroller.SelectableController
import art.intel.soft.utils.ImageLoader
import art.intel.soft.utils.ImageLoader.ScaleType.FIT_CENTER

class StickerAdapter(data: List<String>, detailable: Action<String>) : SimpleAdapter<String>(data, detailable) {

    init {
        selectableMode = false
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder<String> {
        return StickerViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_sticker, parent, false),
                selectableController, action
        )
    }

    private class StickerViewHolder(
            view: View,
            controller: SelectableController<in SimpleViewHolder<String>>,
            detailable: Action<String>
    ) : SimpleViewHolder<String>(view, controller, detailable) {

        private val binding: ItemStickerBinding = ItemStickerBinding.bind(itemView)

        override fun onBind(data: String) {
            ImageLoader.loadImage(binding.imageView, data, FIT_CENTER)
            super.onBind(data)
        }
    }
}
