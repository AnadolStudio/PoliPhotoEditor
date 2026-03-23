package art.intel.soft.ui.edit.form

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import art.intel.soft.R
import art.intel.soft.base.event.SingleEvent
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.Screens
import art.intel.soft.base.firebase.events.implementation.OpenItemEvent
import art.intel.soft.databinding.FragmentEditSplashBinding
import art.intel.soft.ui.edit.BaseEditFragment
import art.intel.soft.ui.edit.FragmentCreatedCallback
import art.intel.soft.ui.edit.form.FormEditFragment.State.MAIN
import art.intel.soft.ui.edit.form.FormEditFragment.State.SETTINGS
import art.intel.soft.ui.edit.form.FormsViewModel.BackgroundSettingsType
import art.intel.soft.ui.edit.form.FormsViewModel.SettingsState
import art.intel.soft.utils.slider.ColorChangeListener
import art.intel.soft.view.BaseToolbar
import art.intel.soft.view.FormView

class FormEditFragment : BaseEditFragment() {

    override val toolbarTitleId: Int = R.string.edit_form_title

    companion object {

        fun newInstance(callback: FragmentCreatedCallback): FormEditFragment = FormEditFragment().also {
            it.callback = callback
        }
    }

    override fun provideScreenName(): Screens = Screens.FORMS

    private enum class State { MAIN, SETTINGS }

    private var currentState = MAIN
        set(value) {
            field = value
            binding.settingsPanel.isVisible = value == SETTINGS
            binding.mainRv.visibility = if (value == MAIN) View.VISIBLE else View.GONE
        }

    private val formView: FormView by lazy(LazyThreadSafetyMode.NONE) {
        FormView(
                requireContext(),
                editor().workSpace(),
                FormView.COMMON_BACK
        )
    }
    private val binding by lazy(LazyThreadSafetyMode.NONE) { FragmentEditSplashBinding.inflate(layoutInflater) }
    private val viewModel: FormsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe(viewLifecycleOwner, this::renderState)
        viewModel.settingsState.observe(viewLifecycleOwner, this::renderSettingState)
        viewModel.event.observe(viewLifecycleOwner, this::handleEvent)

        formView.setBackgroundBitmap(editor().getOriginalBitmap())
        initSliders()
    }

    private fun handleEvent(event: SingleEvent) = when (event) {
        is FormEvent.Loading -> showLoadingDialog()
        is FormEvent.ShowForm -> showForm(event.form, event.mask)
        else -> Unit
    }

    private fun renderState(state: FormState) = when (state) {
        is FormState.Content -> showContent(state)
        FormState.Loading -> showLoadingDialog()
    }

    private fun renderSettingState(state: SettingsState) {
        binding.backgroundSlider.value = state.currentValue
        state.colorFrame ?: let { binding.colorSeekBar.reset(notifyListener = false) }

        formView.apply {
            blurRadius = state.blurRadius
            saturationRatio = state.saturation
            frameColor = state.colorFrame ?: Color.WHITE
        }
    }

    private fun showContent(state: FormState.Content) {
        binding.mainRv.itemAnimator = null
        val adapter = FormAdapter(
                data = state.formDataList,
                detailable = this::loadForm,
                settingsAction = { currentState = SETTINGS }
        )

        binding.mainRv.adapter = adapter
        if (!formView.isReady()) {
            //Select first form
            binding.mainRv.post {
                binding.mainRv.findViewHolderForAdapterPosition(0)?.itemView?.callOnClick()
            }
        }

        hideLoadingDialog()
    }

    private fun loadForm(formData: FormData) {
        viewModel.onOpenItem(formData.formPath)
        viewModel.loadForm(formData.formPath, formData.formMaskPath)
    }

    private fun initSliders() {
        binding.apply {
            backgroundSlider.addOnChangeListener { _, value, fromUser ->
                if (!fromUser) return@addOnChangeListener

                viewModel.changeBackgroundSliderValue(value)
            }

            colorSeekBar.setOnColorChangeListener(ColorChangeListener(viewModel::changeColorFrame))
            blurButton.setOnClickListener { viewModel.changeBackSliderType(BackgroundSettingsType.BLUR) }
            saturationButton.setOnClickListener { viewModel.changeBackSliderType(BackgroundSettingsType.SATURATION) }
        }
    }

    override fun onSetupToolbar(toolbar: BaseToolbar) {
        super.onSetupToolbar(toolbar)
        toolbar.setRightButtonAction(this::applyChanges)
    }

    private fun applyChanges() {
        if (!hasChanges) {
            nothingIsSelectedToast()

            return
        }

        showApplyDialog()
    }

    override fun dialogActionApply() {
        viewModel.applyItem()
        super.dialogActionApply()
    }

    override fun onBackPressed(): Boolean = when {
        binding.settingsPanel.visibility != View.VISIBLE -> false.also { showCancelDialog() }
        else -> false.also { currentState = MAIN }
    }

    private fun showForm(splashBitmap: Bitmap, maskBitmap: Bitmap) {
        binding.saturationButton.isImageSelected = true

        with(formView) {
            if (!isReady()) {
                editor().addView(formView)
                editor().setVisibleMainPanel(false)
            }

            setSplashBitmap(splashBitmap, maskBitmap)
        }

        setChanges()
        hideLoadingDialog()
    }

    override fun process(main: Bitmap, support: Bitmap?): Bitmap = formView.process()

}
