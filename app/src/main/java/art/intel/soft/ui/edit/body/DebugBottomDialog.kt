package art.intel.soft.ui.edit.body

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import art.intel.soft.R
import art.intel.soft.base.dialogs.BaseBottomSheetDialog
import art.intel.soft.databinding.FragmentDebugBodyBottomDialogBinding
import art.intel.soft.extention.withArgs

class DebugBottomDialog : BaseBottomSheetDialog(R.layout.fragment_debug_body_bottom_dialog) {

    companion object {
        private const val INTENSITY_KEY = "INTENSITY_KEY"

        fun newInstance(intensity: Float): DebugBottomDialog = DebugBottomDialog().withArgs {
            putFloat(INTENSITY_KEY, intensity)
        }
    }

    override fun getDialogTag(): String = "DebugBottomDialog"

    override fun setStyle() {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetTheme)
    }

    private val binding: FragmentDebugBodyBottomDialogBinding by lazy {
        FragmentDebugBodyBottomDialogBinding.bind(
                requireView()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val intensity = requireArguments().getFloat(INTENSITY_KEY)

        binding.intensitySlider.setLabelFormatter { value -> "${value / 1000F}" }
        binding.intensitySlider.value = intensity * 1000F
    }

    override fun onDismiss(dialog: DialogInterface) {
        (parentFragment as? Action)?.applySettings(getSettingData())
        super.onDismiss(dialog)
    }

    private fun getSettingData(): SettingData = SettingData(
            intensity = binding.intensitySlider.value / 1000F
    )

    interface Action {

        fun applySettings(settingData: SettingData)
    }

    data class SettingData(
            val intensity: Float,
    )
}

