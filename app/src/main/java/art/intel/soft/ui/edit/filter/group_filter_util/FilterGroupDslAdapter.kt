package art.intel.soft.ui.edit.filter.group_filter_util

import art.intel.soft.base.adapter.BaseDslAdapter
import art.intel.soft.ui.edit.filter.adapter.FilterGroup
import com.angcyo.dsladapter.DslAdapter

class FilterGroupDslAdapter(onSelect: (Int, FilterGroup) -> Unit) : BaseDslAdapter<FilterGroup, FilterGroupDslSection>(
        dataList = FilterGroup.values().filter { it != FilterGroup.ORIGINAL },
        onSelect = onSelect
) {

    override fun slidingItem(adapter: DslAdapter, init: FilterGroupDslSection.() -> Unit) {
        adapter.apply {
            FilterGroupDslSection(dataList).invoke(init::invoke)
        }
    }

}
