package art.intel.soft.ui.edit.collage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.view.isVisible
import art.intel.soft.R
import art.intel.soft.base.adapter.BaseDslSection
import art.intel.soft.databinding.ItemImageBinding
import art.intel.soft.utils.ImageLoader
import com.angcyo.tablayout.DslTabLayout

class CollageDslSection(
        data: List<CollageDslAdapter.CollageDslAdapterData>,
) : BaseDslSection<CollageDslAdapter.CollageDslAdapterData>(data) {

    private companion object {
        const val IMAGE_SIDE = 400
    }

    override fun createView(context: Context, root: ViewGroup, data: CollageDslAdapter.CollageDslAdapterData): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image, root, false)
        view.layoutParams = DslTabLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)

        ItemImageBinding.bind(view).apply {
            ImageLoader.loadImageWithoutCache(
                    context,
                    data.collage,
                    IMAGE_SIDE,
                    IMAGE_SIDE,
                    ImageLoader.ScaleType.FIT_CENTER,
                    supportImageView::setImageBitmap
            )

            supportImageView.isVisible = true
        }

        return view
    }
}
