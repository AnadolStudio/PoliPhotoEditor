package art.intel.soft.ui.gallery

import android.view.View
import androidx.core.view.isVisible
import art.intel.soft.databinding.ItemGalleryBinding
import art.intel.soft.ui.edit.Action
import art.intel.soft.ui.edit.SimpleViewHolder
import art.intel.soft.ui.selectablecontroller.SelectableController
import art.intel.soft.utils.ImageLoader

class GalleryViewHolder(
        view: View,
        controller: SelectableController<SimpleViewHolder<String>>,
        detailAction: Action<String>,
        private val selectedAction: Action<String>
) : SimpleViewHolder<String>(view, controller, detailAction) {

    private val binding = ItemGalleryBinding.bind(itemView)

    init {
        binding.selectImage.setOnClickListener(this::onClick)
    }

    private var selectedMapIsFull = false

    fun setSelectedMapIsFull(isFull: Boolean) {
        selectedMapIsFull = isFull
    }

    override fun onBind(data: String, isSelected: Boolean) {
        onBind(data)
        onBind(isSelected)
    }

    override fun onBind(data: String) {
        this.data = data
        binding.imageView.tag = absoluteAdapterPosition

        // TODO не учитывает невидимые пиксели
        ImageLoader.loadImage(binding.imageView, data, ImageLoader.ScaleType.CENTER_CROP)
    }

    override fun onBind(isSelected: Boolean) {
        binding.selectImage.isSelected = isSelected
        binding.selectImage.isVisible = selectableMode && (!selectedMapIsFull || isSelected)
    }

    override fun onClick(view: View) = when (selectableMode) {
        true -> {
            if (!selectedMapIsFull) binding.selectImage.isSelected = !binding.selectImage.isSelected
            selectedAction.action(data)
        }
        false -> {
            detailable.action(data)
        }
    }
}
