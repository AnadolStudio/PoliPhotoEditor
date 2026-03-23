package art.intel.soft.ui.edit.collage

import android.content.Context
import android.graphics.Bitmap
import art.intel.soft.view.collage.ItemCollageView

class ItemCollageController(
        private val addPhotoAction: ((maskId: Int) -> Unit),
        private val removePhotoAction: ((maskId: Int) -> Unit),
        private val onSelectAction: (isSelect: Boolean) -> Unit,
) {

    private val itemCollageViewList: MutableList<ItemCollageView> = mutableListOf()
    private var selectedItem: ItemCollageView? = null

    fun clear() {
        selectedItem = null
        itemCollageViewList.forEach { it.clear() }
        itemCollageViewList.clear()
    }

    fun createMasks(context: Context, bitmapWrapperList: List<DataBitmapWrapper>) {
        clear()
        for (i in bitmapWrapperList.indices) {
            val bitmapWrapper = bitmapWrapperList[i]

            val item = ItemCollageView(context, null).apply {
                setup(i, bitmapWrapper.bitmap, bitmapWrapper.bounds)
                setOnAddLister(addPhotoAction::invoke)
                setOnSelectLister(this@ItemCollageController::selectItem)
            }

            itemCollageViewList.add(item)
        }
    }

    private fun selectItem(id: Int) {
        for (item in itemCollageViewList) {
            if (item.getMaskId() == id) {
                selectedItem = item
                item.select()
            } else {
                item.bringToFront()
                item.unselect()
            }
        }
        onSelectAction.invoke(true)
    }

    fun previewCollage() {
        itemCollageViewList.forEach { it.preview() }
    }

    fun setPhotoList(bitmapList: List<Bitmap>) {
        bitmapList.take(itemCollageViewList.size).forEachIndexed { index, bitmap ->
            itemCollageViewList[index].setPhoto(bitmap)
        }
    }

    fun editSelectPhoto(command: ItemCollageView.EditCommand) {
        selectedItem?.let { item ->
            item.edit(command)
            if (command is ItemCollageView.EditCommand.Delete) {
                removePhotoAction.invoke(item.getMaskId())
                onSelectAction.invoke(false)
            }
        }
    }

    fun addPhoto(maskId: Int, bitmap: Bitmap) {
        itemCollageViewList.firstOrNull { it.getMaskId() == maskId }?.setPhoto(photoBitmap = bitmap, needSelect = true)
    }

    fun getItems(): List<ItemCollageView> = itemCollageViewList

    fun collageIsReady(): Boolean {
        var count = 0
        itemCollageViewList.forEach { if (it.isNotEmpty()) count++ }

        return itemCollageViewList.size == count
    }
}
