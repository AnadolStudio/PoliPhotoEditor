package art.intel.soft.base.dialogs

import art.intel.soft.R
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.Screens
import art.intel.soft.base.firebase.events.implementation.PopUpEvent
import art.intel.soft.extention.withArgs

class CancelBottomDialog : CommonBottomSheetDialog() {

    companion object {
        fun newInstance(screens: Screens): CancelBottomDialog = CancelBottomDialog().withArgs {
            putString(SCREEN_CONTEXT_KEY, screens.value)
        }
    }

    override fun getDialogTag(): String = "CancelBottomDialog"

    override val negativeButtonTextId: Int = R.string.common_cancel_dialog_positive_button_title
    override val positiveButtonTextId: Int = R.string.common_cancel_dialog_negative_button_title

    override fun onNegativeButtonClick() {
        (parentFragment as? Action)?.dialogActionNavigateBack()
        withOpenScreenContext { itemName ->
            AnalyticEventsUtil.logEvent(PopUpEvent.Back.Apply(itemName))
        }

        dismiss()
    }

    override fun onPositiveButtonClick() {
        withOpenScreenContext { itemName ->
            AnalyticEventsUtil.logEvent(PopUpEvent.Back.Cancel(itemName))
        }

        dismiss()
    }

    interface Action {

        fun dialogActionNavigateBack()
    }
}

