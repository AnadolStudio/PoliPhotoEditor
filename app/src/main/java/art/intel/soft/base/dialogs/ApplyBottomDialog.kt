package art.intel.soft.base.dialogs

import art.intel.soft.R
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.Screens
import art.intel.soft.base.firebase.events.implementation.PopUpEvent
import art.intel.soft.extention.withArgs

class ApplyBottomDialog : CommonBottomSheetDialog() {

    companion object {
        fun newInstance(screens: Screens): ApplyBottomDialog = ApplyBottomDialog().withArgs {
            putString(SCREEN_CONTEXT_KEY, screens.value)
        }
    }

    override fun getDialogTag(): String = "ApplyBottomDialog"

    override val positiveButtonTextId: Int = R.string.common_apply_dialog_positive_button_title
    override val negativeButtonTextId: Int = R.string.common_apply_dialog_negative_button_title

    override fun onPositiveButtonClick() {
        (parentFragment as? Action)?.dialogActionApply()
        withOpenScreenContext { itemName ->
            AnalyticEventsUtil.logEvent(PopUpEvent.Save.Apply(itemName))
        }

        dismiss()
    }

    override fun onNegativeButtonClick() {
        withOpenScreenContext { itemName ->
            AnalyticEventsUtil.logEvent(PopUpEvent.Save.Cancel(itemName))
        }

        dismiss()
    }

    interface Action {

        fun dialogActionApply()
    }
}

