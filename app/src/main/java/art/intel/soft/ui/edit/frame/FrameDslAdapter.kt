package art.intel.soft.ui.edit.frame

import android.graphics.Bitmap
import art.intel.soft.base.adapter.BaseDslAdapter
import com.angcyo.dsladapter.DslAdapter

class FrameDslAdapter(
        paths: List<String?>,
        private val thumbnail: Bitmap,
        onSelect: (Int, String?) -> Unit
) : BaseDslAdapter<String?, FrameDslSection>(paths, onSelect) {

    override fun slidingItem(adapter: DslAdapter, init: FrameDslSection.() -> Unit) {
        adapter.apply {
            FrameDslSection(thumbnail, dataList).invoke(init::invoke)
        }
    }

}
