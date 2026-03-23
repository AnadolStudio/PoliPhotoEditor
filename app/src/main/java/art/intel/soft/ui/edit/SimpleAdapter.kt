package art.intel.soft.ui.edit

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import art.intel.soft.R
import art.intel.soft.ui.selectablecontroller.SelectableController
import art.intel.soft.utils.ImageLoader
import art.intel.soft.utils.ImageLoader.ScaleType.FIT_CENTER

open class SimpleAdapter<E>(
        data: List<E>,
        detailable: Action<E>,
) : AbstractListAdapter<E, SimpleViewHolder<E>>(detailable) {

    init {
        dataList = data.toMutableList()
    }

    open fun getViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder<E> {
        return SimpleViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_image_list, parent, false),
                selectableController,
                action,
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder<E> =
            getViewHolder(parent, viewType)
}

open class SimpleViewHolder<T>(
        view: View,
        protected val controller: SelectableController<in SimpleViewHolder<T>>,
        protected val detailable: Action<T>,
) : AbstractViewHolder<T>(view) {

    var imageView: ImageView? = itemView.findViewById(R.id.imageView)
    var textView: TextView? = itemView.findViewById(R.id.textView)

    init {
        textView?.visibility = GONE
        itemView.setOnClickListener(::onClick)
    }

    open fun onClick(view: View) {
        if (!selectableMode) detailable.action(data)
        else if (!controller.selectableItemIsExist() || controller.getCurrentPosition() != this.absoluteAdapterPosition) {
            detailable.action(data)
            controller.setCurrentSelectedItem(this)
        }
    }

    override fun onBind(data: T, isSelected: Boolean) {
        imageView?.also {
            if (data is String) ImageLoader.loadImage(imageView, data, FIT_CENTER)
        }
        super.onBind(data, isSelected)
    }

    override fun getSelectableView(): View = itemView.findViewById(R.id.main_container) ?: itemView

    override fun equals(other: Any?): Boolean = other == data

    override fun hashCode(): Int = data.hashCode()
}
