package art.intel.soft.ui.edit.text

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import art.intel.soft.R
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.implementation.ApplyItemEvent
import art.intel.soft.databinding.AddTextDialogBinding
import art.intel.soft.extention.withArgs
import art.intel.soft.utils.throttleClick

class TextEditorDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "TextEditorDialogFragment"
        const val EXTRA_INPUT_TEXT = "extra_input_text"

        fun show(fragmentManager: FragmentManager, inputText: String? = null) = TextEditorDialogFragment().withArgs {
            inputText?.let { putString(EXTRA_INPUT_TEXT, inputText) }
        }.show(fragmentManager, TAG)

    }

    private val binding: AddTextDialogBinding by lazy { AddTextDialogBinding.bind(requireView()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.add_text_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        showKeyboard()
    }

    private fun showKeyboard() {
        requireView().post {
            val inputMethod = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethod.showSoftInput(binding.addTextEditText, 0)
        }
    }

    private fun initView() {
        val text = arguments?.getString(EXTRA_INPUT_TEXT)?.also {
            binding.addTextEditText.setText(it)
            binding.addTextEditText.setSelection(it.length)
        }

        binding.addTextEditText.requestFocus()

        binding.backButton.throttleClick {
            hideKeyboard()
            dismiss()
        }

        binding.applyButton.throttleClick {
            val currentText = binding.addTextEditText.text.toString()
            if (currentText.isBlank()) return@throttleClick

            hideKeyboard()

            if (text == null) {
                AnalyticEventsUtil.logEvent(ApplyItemEvent.Text(ApplyItemEvent.Text.Item.KEYBOARD))
                (parentFragment as? TextEditorDialogAction)?.onAddText(currentText)
            } else {
                (parentFragment as? TextEditorDialogAction)?.onChangeText(currentText)
            }

            dismiss()
        }
    }

    private fun hideKeyboard() {
        val inputMethod = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethod.hideSoftInputFromWindow(binding.addTextEditText.windowToken, 0)
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(MATCH_PARENT, MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    interface TextEditorDialogAction {

        fun onChangeText(inputText: String)

        fun onAddText(inputText: String)

    }

}
