package art.intel.soft.ui.edit.collage

import art.intel.soft.base.adapter.BaseDslAdapter
import com.angcyo.dsladapter.DslAdapter

class CollageDslAdapter(
        collageList: List<String>,
        maskList: List<String>,
        onSelect: (Int, CollageDslAdapterData) -> Unit
) : BaseDslAdapter<CollageDslAdapter.CollageDslAdapterData, CollageDslSection>(
        CollageDslAdapterData.toList(collageList, maskList),
        onSelect
) {

    override fun slidingItem(adapter: DslAdapter, init: CollageDslSection.() -> Unit) {
        adapter.apply {
            CollageDslSection(dataList).invoke(init::invoke)
        }
    }

    data class CollageDslAdapterData(
            val collage: String,
            val mask: String
    ) {
        companion object {
            fun toList(collageList: List<String>, maskList: List<String>): List<CollageDslAdapterData> {
                if (collageList.size != maskList.size) return emptyList()

                return collageList.mapIndexed { index, collage -> CollageDslAdapterData(collage, maskList[index]) }
            }
        }
    }
}
