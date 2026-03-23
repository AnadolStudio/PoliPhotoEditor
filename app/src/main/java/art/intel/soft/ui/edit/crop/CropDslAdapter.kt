package art.intel.soft.ui.edit.crop

import art.intel.soft.base.adapter.BaseDslAdapter
import com.angcyo.dsladapter.DslAdapter

class CropDslAdapter(onSelect: (Int, RatioText) -> Unit) : BaseDslAdapter<RatioText, CropDslSection>(
        dataList = RatioText.values().toList(),
        onSelect = onSelect
) {

    override fun slidingItem(adapter: DslAdapter, init: CropDslSection.() -> Unit) {
        adapter.apply {
            CropDslSection(dataList).invoke(init::invoke)
        }
    }
}

