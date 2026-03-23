package art.intel.soft.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import art.intel.soft.R
import art.intel.soft.ui.edit.Action
import art.intel.soft.ui.edit.SimpleAdapter
import art.intel.soft.ui.edit.SimpleViewHolder

class GalleryListAdapter(
        photoList: List<String>,
        action: Action<String>,
        isSelectedMode: Boolean,
        private val loadMoreListener: ILoadMore,
        private val onSelectedListener: (cont: Int) -> Unit,
) : SimpleAdapter<String>(photoList, action) {

    init {
        selectableMode = isSelectedMode
    }

    private val selectedMap = mutableMapOf<String, Boolean>()
    private var selectedMapIsFull = false
        set(value) {
            if (field != value) notifyDataSetChanged()
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder<String> = GalleryViewHolder(
            view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery, parent, false),
            controller = selectableController,
            detailAction = action,
            selectedAction = { path ->
                when {
                    selectedMap.containsKey(path) -> selectedMap.remove(path)
                    selectedMap.size < 3 -> selectedMap[path] = true
                }

                selectedMapIsFull = selectedMap.size >= 3
                onSelectedListener.invoke(selectedMap.size)
            }
    )

    override fun onBindViewHolder(holder: SimpleViewHolder<String>, position: Int) {
        (holder as? GalleryViewHolder)?.setSelectedMapIsFull(selectedMapIsFull)
        holder.onBind(dataList[position], selectedMap.containsKey(dataList[position]), selectableMode)
    }

    override fun setData(list: List<String>) {
        val diffUtilCallback = DiffUtilCallback(dataList, list)
        val diffResult = DiffUtil.calculateDiff(diffUtilCallback, false)

        dataList = list.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }

    fun addData(list: List<String>) {
        dataList.addAll(list)
        notifyItemRangeInserted(dataList.size - list.size, list.size)
    }

    fun getSelectedPhotos(): List<String> = selectedMap.keys.toList()

    fun clearSelectedPhotos() {
        selectedMapIsFull = false
        selectedMap.clear()
        onSelectedListener.invoke(selectedMap.size)
        notifyDataSetChanged()
    }

    override fun onViewAttachedToWindow(holder: SimpleViewHolder<String>) {
        super.onViewAttachedToWindow(holder)
        val position = holder.absoluteAdapterPosition

        if (position == itemCount - 1) {
            loadMoreListener.loadMore()
        }
    }
}
