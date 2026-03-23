package art.intel.soft.ui.edit.crop

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.core.content.ContextCompat
import art.intel.soft.R
import art.intel.soft.base.adapter.BaseDslSection
import art.intel.soft.databinding.ItemCropBinding
import com.angcyo.tablayout.DslTabLayout

class CropDslSection(
        data: List<RatioText>,
) : BaseDslSection<RatioText>(data) {

    override fun createView(context: Context, root: ViewGroup, data: RatioText): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_crop, root, false)
        view.layoutParams = DslTabLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)

        ItemCropBinding.bind(view).imageView.setImageDrawable(
                ContextCompat.getDrawable(context, data.drawableUnselectedId)
        )

        return view
    }
}
