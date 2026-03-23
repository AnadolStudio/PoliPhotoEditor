package art.intel.soft.ui.edit.frame

import android.content.Context
import android.graphics.Bitmap
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
import art.intel.soft.utils.bitmaputils.centerCrop
import com.anadolstudio.library.curvestool.util.dpToPx
import com.angcyo.tablayout.DslTabLayout

class FrameDslSection(
        private val thumbnail: Bitmap,
        data: List<String?>,
) : BaseDslSection<String?>(data) {

    override fun createView(context: Context, root: ViewGroup, data: String?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image, root, false)
        view.layoutParams = DslTabLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)

        ItemImageBinding.bind(view).apply {

            centerCrop(thumbnail)
            imageView.setImageBitmap(thumbnail)
            ImageLoader.loadImageWithoutCache(
                    context,
                    data,
                    thumbnail.width,
                    thumbnail.height,
                    ImageLoader.ScaleType.FIT_CENTER,
                    supportImageView::setImageBitmap
            )

            if (data == null) {
                (imageView.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                    marginStart = 8.dpToPx()
                    marginEnd = 8.dpToPx()
                }
                imageView.layoutParams.width = WRAP_CONTENT
                imageView.requestLayout()
            }
            supportImageView.isVisible = data != null
        }

        return view
    }
}
