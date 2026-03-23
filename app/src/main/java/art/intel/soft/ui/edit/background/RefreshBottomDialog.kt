package art.intel.soft.ui.edit.background

import art.intel.soft.R
import art.intel.soft.base.dialogs.CommonBottomSheetDialog

class RefreshBottomDialog : CommonBottomSheetDialog() {

    override fun getDialogTag(): String = "EmptyMaskBottomDialog"

    override val positiveButtonTextId: Int = R.string.cut_bottom_dialog_refresh_cancel
    override val negativeButtonTextId: Int = R.string.cut_bottom_dialog_refresh_apply

    override fun onNegativeButtonClick() {
        (parentFragment as? Action)?.dialogActionRefresh()
        dismiss()
    }

    override fun onPositiveButtonClick() {
        dismiss()
    }

    interface Action {

        fun dialogActionRefresh()
    }
}

