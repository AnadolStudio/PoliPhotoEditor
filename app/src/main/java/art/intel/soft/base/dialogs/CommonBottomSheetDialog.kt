package art.intel.soft.base.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import art.intel.soft.R
import art.intel.soft.databinding.FragmentCommonBottomDialogBinding
import art.intel.soft.utils.AnimateUtil.scaleAnimationOnClick

abstract class CommonBottomSheetDialog : BaseBottomSheetDialog(R.layout.fragment_common_bottom_dialog) {

    protected companion object{
        const val SCREEN_CONTEXT_KEY = "SCREEN_CONTEXT_KEY"
    }

    protected abstract val positiveButtonTextId: Int
    protected abstract val negativeButtonTextId: Int

    protected abstract fun onPositiveButtonClick()
    protected abstract fun onNegativeButtonClick()

    override fun setStyle() {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.TransparentBottomSheetTheme)
    }

    private val binding: FragmentCommonBottomDialogBinding by lazy { FragmentCommonBottomDialogBinding.bind(requireView()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.positiveButton.setText(getString(positiveButtonTextId))
        binding.negativeButton.setText(getString(negativeButtonTextId))

        binding.positiveButton.scaleAnimationOnClick { onPositiveButtonClick() }
        binding.negativeButton.scaleAnimationOnClick { onNegativeButtonClick() }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false

        return super.onCreateDialog(savedInstanceState)
    }

    protected fun withOpenScreenContext(action: (String) -> Unit) {
        arguments?.getString(SCREEN_CONTEXT_KEY)?.let(action::invoke)
    }
}

