package art.intel.soft.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RadioGroup
import art.intel.soft.databinding.ViewSelectiveColorButtonBinding

class SelectiveColorButtonsView constructor(
        context: Context,
        attrs: AttributeSet?,
) : RadioGroup(context, attrs) {

    private val binding: ViewSelectiveColorButtonBinding = ViewSelectiveColorButtonBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
    )

    fun bindSelectiveColorView(view: SelectiveColorView) {
        binding.radioGroup.setOnChangeListener { selectView ->
            when (selectView.id) {
                binding.red.id -> view.selectRed()
                binding.green.id -> view.selectGreen()
                binding.blue.id -> view.selectBlue()
                binding.cyan.id -> view.selectCyan()
                binding.magenta.id -> view.selectMagenta()
                binding.yellow.id -> view.selectYellow()
                binding.white.id -> view.selectWhite()
                binding.gray.id -> view.selectGray()
                else -> view.selectBlack()
            }
        }
    }

}
