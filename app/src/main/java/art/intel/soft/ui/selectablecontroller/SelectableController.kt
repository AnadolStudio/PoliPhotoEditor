package art.intel.soft.ui.selectablecontroller

import androidx.recyclerview.widget.RecyclerView
import art.intel.soft.ui.edit.AbstractViewHolder

interface SelectableController<Holder : RecyclerView.ViewHolder> {

    fun setCurrentSelectedItem(holder: Holder?)

    fun updateView(holder: Holder, isSelected: Boolean, state: Int)

    fun savePosition()

    fun savePosition(index: Int)

    fun getCurrentPosition(): Int

    fun selectableItemIsExist(): Boolean

    fun setStartItem(holder: Holder)

    fun clear()

    abstract class Abstract<Holder : AbstractViewHolder<*>> : SelectableController<Holder> {

        protected var selectedItem: Holder? = null
        protected var state = -1

        abstract override fun updateView(holder: Holder, isSelected: Boolean, state: Int)

        override fun savePosition() = savePosition(selectedItem?.absoluteAdapterPosition ?: -1)

        override fun savePosition(index: Int) {
            state = index
        }

        override fun getCurrentPosition(): Int = state

        override fun setCurrentSelectedItem(holder: Holder?) {
            selectedItem?.also { updateView(it, false, state) }

            selectedItem = holder
            savePosition()
            selectedItem ?: return

            selectedItem?.also {
                updateView(it, true, state)
            }
        }

        override fun clear() {
            setCurrentSelectedItem(null)
        }

        override fun setStartItem(holder: Holder) {
            selectedItem = holder
            savePosition()
        }

        override fun selectableItemIsExist() = selectedItem != null
    }

    class Base<Holder : AbstractViewHolder<*>>(
            private val adapter: RecyclerView.Adapter<Holder>
    ) : Abstract<Holder>() {

        override fun updateView(holder: Holder, isSelected: Boolean, state: Int) {
            try {
                adapter.notifyItemChanged(state)
            } catch (ex: IllegalStateException) {
                holder.onBind(isSelected)
            }
        }

    }

}

