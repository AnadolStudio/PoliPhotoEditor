package art.intel.soft.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import art.intel.soft.R
import art.intel.soft.databinding.ViewSimpleTextBinding

typealias Listener = () -> Unit

abstract class AbstractDialog(
        var positiveListener: Listener? = null, var negativeListener: Listener? = null
) : DialogFragment() {

    companion object {
        internal const val DIALOG_MESSAGE_ID = "dialog_message_id"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        val binding: ViewSimpleTextBinding = ViewSimpleTextBinding.inflate(layoutInflater)

        val messageId = arguments?.getInt(DIALOG_MESSAGE_ID)
        messageId?.let { binding.textView.text = getString(it) }

        alertDialogBuilder.setView(binding.root)

        initButtons(alertDialogBuilder)


        return alertDialogBuilder.create().apply {
            setOnShowListener { changeView(this) }
        }
    }

    abstract fun initButtons(alertDialogBuilder: AlertDialog.Builder)

    private fun changeView(dialog: AlertDialog) {
        val colorAccent = ContextCompat.getColor(requireContext(), R.color.colorAccentDark)
        val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        positiveButton.setTextColor(colorAccent)
        positiveButton.invalidate()

        val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        negativeButton.setTextColor(colorAccent)
        negativeButton.invalidate()
    }
}

abstract class BaseChoiceDialog(
        positiveListener: Listener?, negativeListener: Listener?
) : AbstractDialog(positiveListener, negativeListener) {

    override fun initButtons(alertDialogBuilder: AlertDialog.Builder) {
        alertDialogBuilder.setPositiveButton(android.R.string.ok) { _, _ ->
            positiveListener?.invoke()
        }
        alertDialogBuilder.setNegativeButton(android.R.string.cancel) { _, _ ->
            negativeListener?.invoke()
            dismiss()
        }
    }
}

abstract class BaseInfoDialog(positiveListener: Listener?) : AbstractDialog(positiveListener) {

    override fun initButtons(alertDialogBuilder: AlertDialog.Builder) {
        alertDialogBuilder.setPositiveButton(android.R.string.ok) { _, _ ->
            positiveListener?.invoke()
        }
    }
}
