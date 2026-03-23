package art.intel.soft.base.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import art.intel.soft.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.DslViewHolder
import com.angcyo.tablayout.DslTabLayout

abstract class BaseDslSection<Data : Any?>(private val dataList: List<Data>) : DslAdapterItem() {

    override var itemLayoutId = R.layout.item_frame_sliding_tab_layout

    override fun onItemBind(
            itemHolder: DslViewHolder,
            itemPosition: Int,
            adapterItem: DslAdapterItem,
            payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemHolder.v<DslTabLayout>(R.id.tab_layout)?.apply {
            dataList.forEach { data ->
                val view = createView(itemHolder.context, this, data)
                addView(view)
            }

            tabIndicator.indicatorDrawable =
                    ContextCompat.getDrawable(itemHolder.itemView.context, R.drawable.indicator_line)
        }
    }

    protected abstract fun createView(context: Context, root: ViewGroup, data: Data): View

}
