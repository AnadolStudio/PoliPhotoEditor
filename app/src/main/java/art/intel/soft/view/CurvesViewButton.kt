package art.intel.soft.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import art.intel.soft.databinding.ViewCurveWithButtonBinding
import com.anadolstudio.library.curvestool.view.CurvesView

class CurvesViewButton @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewCurveWithButtonBinding = ViewCurveWithButtonBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
    )

    fun bindCurvesView(view: CurvesView) {
        binding.white.setOnClickListener { view.showWhiteState() }
        binding.red.setOnClickListener { view.showRedState() }
        binding.green.setOnClickListener { view.showGreenState() }
        binding.blue.setOnClickListener { view.showBlueState() }

        binding.white.callOnClick()
    }

}
