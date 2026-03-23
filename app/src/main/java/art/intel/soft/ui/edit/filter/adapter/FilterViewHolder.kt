package art.intel.soft.ui.edit.filter.adapter

import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import art.intel.soft.databinding.ItemFilterListBinding
import art.intel.soft.extention.baseSubscribeWithoutSubscribeOn
import art.intel.soft.extention.singleFrom
import art.intel.soft.ui.edit.Action
import art.intel.soft.ui.edit.SimpleViewHolder
import art.intel.soft.ui.selectablecontroller.SelectableController
import io.reactivex.disposables.Disposable
import org.wysaid.nativePort.CGENativeLibrary
import java.util.Locale

class FilterViewHolder(
        view: View,
        selectableController: SelectableController<in SimpleViewHolder<FilterDataItem>>,
        detailAction: Action<FilterDataItem>,
        private val intensityAction: () -> Unit,
        private val thumbnailBitmap: Bitmap,
) : SimpleViewHolder<FilterDataItem>(view, selectableController, detailAction) {

    private val binding: ItemFilterListBinding = ItemFilterListBinding.bind(itemView)
    private var loadEffectDisposable: Disposable? = null

    override fun onBind(data: FilterDataItem, isSelected: Boolean) {
        binding.apply {
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageBitmap(thumbnailBitmap)
            textView.text = data.name.uppercase(Locale.getDefault())
            textView.visibility = View.VISIBLE
            textView.background = ColorDrawable(data.group.color.toColorInt())
        }

        loadEffectDisposable?.dispose()
        loadEffectDisposable = singleFrom {
            CGENativeLibrary.filterImage_MultipleEffects(thumbnailBitmap, data.effectConfig, 1.0f)
        }.baseSubscribeWithoutSubscribeOn(
                onSuccess = binding.imageView::setImageBitmap
        )

        super.onBind(data, isSelected)
    }

    override fun onBind(isSelected: Boolean) {
        super.onBind(isSelected)
        binding.intensitySliderContainer.isVisible = isSelected
    }

    override fun onClick(view: View) {
        if (controller.getCurrentPosition() == this.absoluteAdapterPosition) {
            intensityAction.invoke()
        }
        super.onClick(view)
    }
}
