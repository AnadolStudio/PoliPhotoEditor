package art.intel.soft.ui.edit.sticker

import art.intel.soft.base.adapter.BaseDslAdapter
import com.angcyo.dsladapter.DslAdapter

class StickerGroupDslAdapter(
        paths: List<Int>,
        onSelect: (Int, Int) -> Unit
) : BaseDslAdapter<Int, StickerGroupDslSection>(paths, onSelect) {

    override fun slidingItem(adapter: DslAdapter, init: StickerGroupDslSection.() -> Unit) {
        adapter.apply {
            StickerGroupDslSection(dataList).invoke(init::invoke)
        }
    }

    override fun onSelectItem(fromUser: Boolean, isSelect: Boolean, index: Int) {
        if (isSelect && index != lastSelectIndex) {
            lastSelectIndex = index
            onSelect.invoke(index, dataList[index])
        }
    }

}
