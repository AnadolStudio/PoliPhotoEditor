package art.intel.soft.ui.edit.filter.group_filter_util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import art.intel.soft.base.adapter.BaseDslSection
import art.intel.soft.ui.edit.filter.adapter.FilterGroup
import art.intel.soft.ui.edit.filter.adapter.FilterGroupText
import com.angcyo.tablayout.DslTabLayout

class FilterGroupDslSection(groups: List<FilterGroup>) : BaseDslSection<FilterGroup>(groups) {

    override fun createView(context: Context, root: ViewGroup, group: FilterGroup): View = FilterGroupText(context)
            .also { filterGroupText ->
                filterGroupText.layoutParams = DslTabLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
                filterGroupText.textView.apply {
                    text = group.name
                }
            }
}
