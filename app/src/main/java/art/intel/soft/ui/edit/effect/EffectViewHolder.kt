package art.intel.soft.ui.edit.effect

import android.graphics.Bitmap
import android.view.View
import androidx.core.view.isVisible
import art.intel.soft.databinding.ItemEffectsBinding
import art.intel.soft.ui.edit.Action
import art.intel.soft.ui.edit.SimpleViewHolder
import art.intel.soft.ui.selectablecontroller.SelectableController
import art.intel.soft.utils.ImageLoader

class EffectViewHolder(
        private val thumbnail: Bitmap,
        view: View,
        selectableController: SelectableController<SimpleViewHolder<String?>>,
        action: Action<String?>,
        private val detailClick: () -> Unit
) : SimpleViewHolder<String?>(view, selectableController, action) {

    private val binding: ItemEffectsBinding = ItemEffectsBinding.bind(itemView)

    init {
        binding.imageView.setImageBitmap(thumbnail)
        binding.supportImageView.visibility = View.VISIBLE
    }

    override fun onBind(data: String?, isSelected: Boolean) {
        ImageLoader.loadImageWithoutCache(
                itemView.context,
                data,
                thumbnail.width,
                thumbnail.height,
                ImageLoader.ScaleType.CENTER_CROP,
                binding.supportImageView::setImageBitmap
        )

        super.data = data
        onBind(isSelected)
    }

    override fun onClick(view: View) {
        if (controller.getCurrentPosition() == this.absoluteAdapterPosition && data != null) {
            detailClick.invoke()
        } else {
            super.onClick(view)
        }
    }

    override fun onBind(isSelected: Boolean) {
        binding.supportImageView.isVisible = data != null
        binding.sliderContainer.isVisible = isSelected && data != null
    }
}
