package art.intel.soft.ui.edit.sticker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import art.intel.soft.R
import art.intel.soft.base.adapter.BaseDslSection
import art.intel.soft.databinding.ItemStikerGroupBinding
import com.anadolstudio.library.curvestool.util.dpToPx
import com.angcyo.tablayout.DslTabLayout

class StickerGroupDslSection(
        data: List<Int>,
) : BaseDslSection<Int>(data) {

    override fun createView(context: Context, root: ViewGroup, data: Int): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_stiker_group, root, false)
        view.layoutParams = DslTabLayout.LayoutParams(40.dpToPx(), MATCH_PARENT)

        ItemStikerGroupBinding.bind(view).apply {
            imageView.setImageResource(data)
        }

        return view
    }
}
