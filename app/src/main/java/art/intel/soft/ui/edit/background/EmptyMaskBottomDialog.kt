package art.intel.soft.ui.edit.background

import art.intel.soft.R
import art.intel.soft.base.dialogs.CommonBottomSheetDialog

class EmptyMaskBottomDialog : CommonBottomSheetDialog() {

    override fun getDialogTag(): String = "EmptyMaskBottomDialog"

    override val positiveButtonTextId: Int = R.string.cut_bottom_dialog_select
    override val negativeButtonTextId: Int = R.string.cut_bottom_dialog_back

    override fun onNegativeButtonClick() {
        (parentFragment as? Action)?.dialogActionNavigateBack()
        dismiss()
    }

    override fun onPositiveButtonClick() {
        (parentFragment as? Action)?.dialogActionManual()
        dismiss()
    }

    interface Action {

        fun dialogActionNavigateBack()
        fun dialogActionManual()
    }
}

