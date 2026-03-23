package art.intel.soft.ui.edit.brush

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import art.intel.soft.R
import art.intel.soft.base.firebase.events.Screens
import art.intel.soft.databinding.FragmentEditBrushBinding
import art.intel.soft.ui.edit.BrushBaseEditFragment
import art.intel.soft.utils.setup
import art.intel.soft.utils.slider.ColorChangeListener
import art.intel.soft.utils.slider.RealFormatter
import art.intel.soft.view.BaseToolbar
import kotlin.LazyThreadSafetyMode.NONE

class BrushEditFragment : BrushBaseEditFragment() {

    override val toolbarTitleId: Int = R.string.edit_brush_title

    companion object {
        fun newInstance(): BrushEditFragment = BrushEditFragment()
    }

    override fun provideScreenName(): Screens = Screens.BRUSH

    private val binding by lazy(NONE) { FragmentEditBrushBinding.inflate(layoutInflater) }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(BRUSH_MODE, isBrush)
        outState.putFloat(CURRENT_SIZE, currentSize)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isBrush = savedInstanceState == null || savedInstanceState.getBoolean(BRUSH_MODE)
        currentSize = savedInstanceState?.getFloat(CURRENT_SIZE) ?: NORMAL

        initView()
        color = binding.colorSeekBar.getColor()
        changeMode(isBrush = true)
    }

    private fun initView() {
        editor().getPhotoEditor().drawingView.setPorterDuffXferMode(PorterDuff.Mode.SRC_OVER)

        binding.colorSeekBar.setOnColorChangeListener(
                ColorChangeListener { newColor ->
                    color = newColor
                    setupBrush()
                }
        )
        binding.opacitySliderBrush.setLabelFormatter(RealFormatter())
        binding.opacitySliderBrush.setup(XSMALL, XLARGE, currentSize)

        binding.opacitySliderBrush.addOnChangeListener { _, size, _ -> setupBrush(size) }

        binding.brushButton.setOnClickListener { changeMode(isBrush = true) }
        binding.eraserButton.setOnClickListener { changeMode(isBrush = false) }
    }

    private fun changeMode(isBrush: Boolean) {
        setupSeekBar(isBrush)
        setBrushMode(isBrush)
        setupBrush()
    }

    private fun setupSeekBar(isBrush: Boolean) {
        binding.colorSeekBar.isEnabled = isBrush
        binding.colorSeekBar.alpha = if (isBrush) 1F else 0.5F

        when (isBrush) {
            true -> binding.colorSeekBar.setDefaultColorSeed()
            false -> binding.colorSeekBar.setColorSeed(Color.LTGRAY, Color.DKGRAY, Color.BLACK)
        }
    }

    override fun onSetupToolbar(toolbar: BaseToolbar) {
        super.onSetupToolbar(toolbar)
        toolbar.setRightButtonAction { showApplyDialog() }
    }

    override fun onBackPressed(): Boolean {
        val hasChanges = !editor().getPhotoEditor().isCacheEmpty

        return if (hasChanges) {
            showCancelDialog()

            false
        } else {
            true
        }
    }

}
