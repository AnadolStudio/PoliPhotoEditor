package art.intel.soft.ui.edit.form

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import art.intel.soft.R
import art.intel.soft.databinding.ItemFormListBinding
import art.intel.soft.ui.edit.Action
import art.intel.soft.ui.edit.SimpleAdapter
import art.intel.soft.ui.edit.SimpleViewHolder
import art.intel.soft.ui.selectablecontroller.SelectableController
import art.intel.soft.utils.ImageLoader

class FormAdapter(
        private val settingsAction: () -> Unit,
        data: List<FormData>,
        detailable: Action<FormData>
) : SimpleAdapter<FormData>(data, detailable) {

    override fun getViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder<FormData> = FormViewHolder(
            settingsAction,
            LayoutInflater.from(parent.context).inflate(R.layout.item_form_list, parent, false),
            selectableController,
            action
    )

    private class FormViewHolder(
            private val settingsAction: () -> Unit,
            view: View,
            controller: SelectableController<in SimpleViewHolder<FormData>>,
            detailable: Action<FormData>
    ) : SimpleViewHolder<FormData>(view, controller, detailable) {

        private val binding: ItemFormListBinding = ItemFormListBinding.bind(itemView)

        init {
            selectableMode = true
            binding.supportImageView.visibility = View.VISIBLE
        }

        override fun onBind(data: FormData, isSelected: Boolean) {
            ImageLoader.loadImage(binding.imageView, data.previewPath, ImageLoader.ScaleType.FIT_CENTER)

            textView?.visibility = View.GONE
            super.onBind(data, isSelected)
        }

        override fun onBind(isSelected: Boolean) {
            binding.sliderContainer.isVisible = isSelected
            super.onBind(isSelected)
        }

        override fun onClick(view: View) = when (isSelected) {
            true -> settingsAction.invoke()
            false -> super.onClick(view)
        }

        override fun selectView(isSelected: Boolean) = Unit
    }
}
