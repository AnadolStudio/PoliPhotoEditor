package art.intel.soft.base.adapter

import art.intel.soft.R
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.DslViewHolder
import com.angcyo.dsladapter.itemViewHolder
import com.angcyo.tablayout.DslTabIndicator
import com.angcyo.tablayout.DslTabLayout

abstract class BaseDslAdapter<Data : Any?, DslSection : DslAdapterItem>(
        protected val dataList: List<Data>,
        protected val onSelect: (index: Int, Data) -> Unit,
) : DslAdapter() {

    fun setup(): BaseDslAdapter<Data, DslSection> {
        render {
            slidingItem(this) {
                itemBindOverride = { itemHolder, _, _, _ ->
                    initDslTabLayout(itemHolder)
                }
            }
        }
        return this
    }

    protected var lastSelectIndex = 0

    protected abstract fun slidingItem(adapter: DslAdapter, init: DslSection.() -> Unit)

    protected open fun initDslTabLayout(
            itemHolder: DslViewHolder,
    ): DslTabLayout? = itemHolder.v<DslTabLayout>(R.id.tab_layout)?.apply {

        tabIndicator.indicatorStyle = DslTabIndicator.INDICATOR_STYLE_BOTTOM

        configTabLayoutConfig {

            onSelectItemView = { _, index, isSelect, fromUser ->

                onSelectItem(fromUser, isSelect, index)

                false
            }
        }
    }

    protected open fun onSelectItem(fromUser: Boolean, isSelect: Boolean, index: Int) {
        if (fromUser && isSelect && index != lastSelectIndex) {
            lastSelectIndex = index
            onSelect.invoke(index, dataList[index])
        }
    }

    fun getTabFromPosition(index: Int): DslTabLayout? = get(index)?.itemViewHolder()?.v<DslTabLayout>(R.id.tab_layout)

}
