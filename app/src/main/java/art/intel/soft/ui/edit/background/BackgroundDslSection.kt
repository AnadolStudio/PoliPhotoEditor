package art.intel.soft.ui.edit.background

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import art.intel.soft.R
import art.intel.soft.base.adapter.BaseDslSection
import art.intel.soft.databinding.ItemBackgoundImageBinding
import art.intel.soft.extention.compatDrawable
import art.intel.soft.utils.ImageLoader

class BackgroundDslSection(
        data: List<String>,
) : BaseDslSection<String>(data) {

    private companion object {
        const val IMAGE_SIDE = 400
        const val CUSTOM = "custom"
        const val COLOR = "color"
    }

    override fun createView(context: Context, root: ViewGroup, data: String): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_backgound_image, root, false)

        ItemBackgoundImageBinding.bind(view).apply {
            when (data) {
                CUSTOM -> {
                    supportImageView.setImageDrawable(context.compatDrawable(R.drawable.ic_background_own_button))
                }
                COLOR -> {
                    supportImageView.setImageDrawable(context.compatDrawable(R.drawable.ic_background_color_button))
                }
                else -> {
                    ImageLoader.loadImageWithoutCache(
                            context,
                            data,
                            IMAGE_SIDE,
                            IMAGE_SIDE,
                            ImageLoader.ScaleType.FIT_CENTER,
                            supportImageView::setImageBitmap
                    )
                }
            }

            supportImageView.isVisible = true
        }

        return view
    }
}
