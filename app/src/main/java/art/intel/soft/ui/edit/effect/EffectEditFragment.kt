package art.intel.soft.ui.edit.effect

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import art.intel.soft.R
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.Screens
import art.intel.soft.base.firebase.events.implementation.OpenItemEvent
import art.intel.soft.databinding.BottomListLayoutBinding
import art.intel.soft.model.AssetsDirections
import art.intel.soft.model.getPathList
import art.intel.soft.ui.edit.BaseEditFragment
import art.intel.soft.ui.edit.FragmentCreatedCallback
import art.intel.soft.ui.edit.crop.CropViewModel
import art.intel.soft.utils.ImageLoader
import art.intel.soft.utils.bitmaputils.scaleBitmap
import art.intel.soft.utils.setup
import art.intel.soft.utils.slider.RealFormatter
import art.intel.soft.view.BaseToolbar
import com.google.android.material.slider.LabelFormatter
import kotlin.math.roundToInt

class EffectEditFragment : BaseEditFragment() {

    override val toolbarTitleId: Int = R.string.edit_effects_title

    companion object {
        private const val DEFAULT_ALPHA = 255

        fun newInstance(callback: FragmentCreatedCallback): EffectEditFragment = EffectEditFragment().apply {
            this.callback = callback
        }
    }

    override fun provideScreenName(): Screens = Screens.EFFECTS

    private val viewModel: EffectViewModel by viewModels()
    private val binding by lazy(LazyThreadSafetyMode.NONE) { BottomListLayoutBinding.inflate(layoutInflater) }
    private var currentEffect: Drawable? = null
    private var currentState: EffectViewState = EffectViewState.LIST
        set(value) {
            field = value
            binding.recyclerView.isVisible = value == EffectViewState.LIST
            binding.supportLayout.root.isVisible = value == EffectViewState.SLIDER
        }

    private var currentAlpha = DEFAULT_ALPHA
        set(value) {
            field = value
            currentEffect?.alpha = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val thumbnail = scaleBitmap(400f, 400f, editor().currentBitmap(), false)

        val data = mutableListOf<String?>().apply {
            add(null)
            addAll(getPathList(requireContext(), AssetsDirections.EFFECTS_DIR))
        }

        binding.recyclerView.adapter = EffectAdapter(
                thumbnail = thumbnail,
                data = data,
                action = { path ->
                    loadEffect(path)
                },
                detailClick = {
                    currentState = EffectViewState.SLIDER
                }
        )

        initView()

        return binding.root
    }

    private fun initView() {
        with(binding.supportLayout) {
            denyButton.visibility = View.GONE
            acceptButton.visibility = View.GONE
            slider.setLabelFormatter(RealFormatter())
            slider.labelBehavior = LabelFormatter.LABEL_GONE
            slider.setup(0f, 255f, DEFAULT_ALPHA.toFloat())
            setupSlider(isEnable = false)
            slider.addOnChangeListener { _, value, _ ->
                currentAlpha = value.roundToInt()
            }
        }
    }

    private fun setupSlider(isEnable: Boolean) {
        binding.supportLayout.slider.isEnabled = isEnable
        binding.supportLayout.slider.value = DEFAULT_ALPHA.toFloat()
    }

    override fun onSetupToolbar(toolbar: BaseToolbar) {
        super.onSetupToolbar(toolbar)
        toolbar.setRightButtonAction { applyChanges() }
    }

    private fun applyChanges() = when {
        currentState != EffectViewState.LIST -> currentState = EffectViewState.LIST
        !hasChanges -> nothingIsSelectedToast()
        else -> showApplyDialog()
    }

    override fun onBackPressed(): Boolean {
        if (currentState != EffectViewState.LIST) {
            currentState = EffectViewState.LIST

            return false
        }

        if (!hasChanges) {

            return true
        }

        showCancelDialog()

        return false
    }

    private fun loadEffect(path: String?) = when (path == null) {
        true -> cleanEffect()
        false -> setEffect(path)
    }

    override fun dialogActionApply() {
        viewModel.applyItem()
        super.dialogActionApply()
    }

    private fun setEffect(path: String) {
        viewModel.onOpenItem(path)

        showLoadingDialog()
        ImageLoader.loadImageWithoutCache(requireContext(), path) { bitmap: Bitmap? ->
            currentEffect = BitmapDrawable(resources, bitmap).apply {
                alpha = currentAlpha
            }
            setChanges()
            setupSlider(isEnable = true)
            editor().setSupportImage(currentEffect)
            hideLoadingDialog()
        }
    }

    private fun cleanEffect() {
        currentEffect = null
        clearChanges()
        editor().setSupportImage(null as Drawable?)
        setupSlider(isEnable = false)
    }
}
