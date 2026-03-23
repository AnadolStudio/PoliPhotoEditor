package art.intel.soft.ui.edit.text.font

import android.view.LayoutInflater
import android.view.ViewGroup
import art.intel.soft.R
import art.intel.soft.ui.edit.Action
import art.intel.soft.ui.edit.SimpleAdapter
import art.intel.soft.ui.edit.SimpleViewHolder

class FontAdapter(
        data: List<Fonts>,
        action: Action<Fonts>,
) : SimpleAdapter<Fonts>(data, action) {

    override fun getViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder<Fonts> = FontViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_font_text, parent, false),
            selectableController,
            action,
    )

    override fun onBindViewHolder(holder: SimpleViewHolder<Fonts>, position: Int) {
        if (!selectableController.selectableItemIsExist() && position == 0) {
            selectableController.setStartItem(holder)
        } else if (selectableController.getCurrentPosition() == position) {
            selectableController.setCurrentSelectedItem(holder)
        }

        super.onBindViewHolder(holder, position)
    }

    fun setSelectItem(font: Fonts): Int {
        val index = Fonts.values().indexOfFirst { it.id == font.id }

        if (index != -1) {
            selectableController.savePosition(index)
            notifyDataSetChanged()
        }

        return index
    }

}
