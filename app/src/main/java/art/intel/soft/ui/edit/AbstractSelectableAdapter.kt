package art.intel.soft.ui.edit

import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import art.intel.soft.R
import art.intel.soft.ui.selectablecontroller.SelectableController

abstract class AbstractListAdapter<E : Any?, T : AbstractViewHolder<E>>(
        protected val action: Action<E>
) : RecyclerView.Adapter<T>() {

    protected open var selectableMode = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    protected var dataList = mutableListOf<E>()

    protected val selectableController = SelectableController.Base(this)

    override fun onBindViewHolder(holder: T, position: Int) {
        holder.onBind(dataList[position], position == selectableController.getCurrentPosition(), selectableMode)
    }

    open fun clearSelectedItem() = selectableController.clear()

    open fun setData(list: List<E>) {
        dataList = list.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dataList.size

    fun toDetail(e: E) = action.action(e)
}

abstract class AbstractViewHolder<E>(view: View) : RecyclerView.ViewHolder(view) {
    protected var selectableMode: Boolean = true
    protected var isSelected: Boolean = false
    protected var data: E? = null

    open fun onBind(data: E, isSelected: Boolean, selectableMode: Boolean = true) {
        this.selectableMode = selectableMode
        onBind(data, isSelected)
    }

    open fun onBind(data: E, isSelected: Boolean) {
        onBind(data)
        if (selectableMode) onBind(isSelected)
    }

    open fun onBind(isSelected: Boolean) {
        this.isSelected = isSelected
        selectView(isSelected)
    }

    open fun onBind(data: E) {
        this.data = data
    }

    abstract fun getSelectableView(): View

    protected open fun selectView(isSelected: Boolean) {
        val view = getSelectableView()
        val color = getSelectableColor(isSelected)

        if (view is CardView) view.setCardBackgroundColor(color)
        else view.setBackgroundColor(color)
    }

    protected open fun getSelectableColor(isSelected: Boolean): Int = when (isSelected) {
        true -> ContextCompat.getColor(itemView.context, R.color.colorAccentDark)
        false -> ContextCompat.getColor(itemView.context, R.color.imageBackground)
    }
}
