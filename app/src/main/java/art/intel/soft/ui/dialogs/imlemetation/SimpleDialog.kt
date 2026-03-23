package art.intel.soft.ui.dialogs.imlemetation

import android.os.Bundle
import androidx.annotation.StringRes
import art.intel.soft.ui.dialogs.BaseChoiceDialog
import art.intel.soft.ui.dialogs.Listener

class ApplyDialog(
        positiveListener: Listener?, negativeListener: Listener?
) : BaseChoiceDialog(positiveListener, negativeListener) {

    companion object {
        fun newInstance(
                @StringRes id: Int,
                positiveListener: Listener? = null,
                negativeListener: Listener? = null
        ): ApplyDialog {
            val args = Bundle().apply { putInt(DIALOG_MESSAGE_ID, id) }
            return ApplyDialog(positiveListener, negativeListener).apply { arguments = args }
        }
    }
}

class CancelDialog(
        positiveListener: Listener?, negativeListener: Listener?
) : BaseChoiceDialog(positiveListener, negativeListener) {

    companion object {
        fun newInstance(
                @StringRes id: Int,
                positiveListener: Listener? = null,
                negativeListener: Listener? = null
        ): CancelDialog {
            val args = Bundle().apply { putInt(DIALOG_MESSAGE_ID, id) }
            return CancelDialog(positiveListener, negativeListener).apply { arguments = args }
        }
    }
}
