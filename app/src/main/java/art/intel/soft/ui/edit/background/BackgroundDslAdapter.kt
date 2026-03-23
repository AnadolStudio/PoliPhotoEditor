package art.intel.soft.ui.edit.background

import art.intel.soft.base.adapter.BaseDslAdapter
import com.angcyo.dsladapter.DslAdapter

class BackgroundDslAdapter(
        pathList: List<String>,
        onSelect: (Int, String) -> Unit,
) : BaseDslAdapter<String, BackgroundDslSection>(pathList, onSelect) {

    private companion object {
        const val CUSTOM = "custom"
        const val COLOR = "color"
    }

    override fun slidingItem(adapter: DslAdapter, init: BackgroundDslSection.() -> Unit) {
        adapter.apply {
            BackgroundDslSection(dataList).invoke(init::invoke)
        }
    }

    override fun onSelectItem(fromUser: Boolean, isSelect: Boolean, index: Int) {
        val data = dataList[index]
        if (fromUser && isSelect && (index != lastSelectIndex || data == CUSTOM || data == COLOR)) {
            onSelect.invoke(index, data)
            lastSelectIndex = index
        }
    }

}
