package art.intel.soft.ui.edit.body

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import art.intel.soft.BuildConfig
import art.intel.soft.R
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.Screens
import art.intel.soft.base.firebase.events.implementation.OpenItemEvent
import art.intel.soft.databinding.FragmentEditBodyBinding
import art.intel.soft.model.AssetsDirections
import art.intel.soft.model.getPathList
import art.intel.soft.ui.edit.FragmentCreatedCallback
import art.intel.soft.ui.edit.SurfaceEditFragment
import art.intel.soft.ui.edit.body.BodyEditFragment.DeformBodyMode.BREAST
import art.intel.soft.ui.edit.body.BodyEditFragment.DeformBodyMode.HIPS
import art.intel.soft.ui.edit.body.BodyEditFragment.DeformBodyMode.WAIST
import art.intel.soft.ui.edit.body.BodyViewBuilder.BreastBuilder
import art.intel.soft.ui.edit.body.BodyViewBuilder.HipsBuilder
import art.intel.soft.ui.edit.body.BodyViewBuilder.WaistBuilder
import art.intel.soft.utils.setup
import art.intel.soft.utils.throttleClick
import art.intel.soft.utils.touchlisteners.BodyTouchListener
import art.intel.soft.view.BaseToolbar
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider
import org.wysaid.nativePort.CGEDeformFilterWrapper
import org.wysaid.view.ImageGLSurfaceView
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

class BodyEditFragment : SurfaceEditFragment(), DebugBottomDialog.Action {

    override val toolbarTitleId: Int = R.string.edit_body_title

    companion object {
        const val DIAPASON = 30F

        fun newInstance(callback: FragmentCreatedCallback?): BodyEditFragment =
                BodyEditFragment().apply { this.callback = callback }
    }

    override fun provideScreenName(): Screens = when {
        currentState == StateBodyEdit.DETAIL && currentBodyMode == BREAST -> Screens.BODY_BREAST
        currentState == StateBodyEdit.DETAIL && currentBodyMode == WAIST -> Screens.BODY_WAIST
        currentState == StateBodyEdit.DETAIL && currentBodyMode == HIPS -> Screens.BODY_HIPS
        else /*currentState == StateBodyEdit.CHOICE*/ -> Screens.BODY
    }

    enum class DeformBodyMode { BREAST, WAIST, HIPS }
    enum class StateBodyEdit { CHOICE, DETAIL }

    private var currentState = StateBodyEdit.CHOICE
        set(value) {
            field = value
            if (!::binding.isInitialized) return

            binding.mainLl.isVisible = value == StateBodyEdit.CHOICE
            binding.sliderContainer.isVisible = value == StateBodyEdit.DETAIL
            editor().setBeforeAfterButtonVisible(false)

            if (value == StateBodyEdit.CHOICE) {
                restore()
                deleteViews()
            } else {
                hasDetailChanges = false
            }
        }

    private var currentValue: Int = 0
    private var hasDetailChanges = false
    private var photoIsBright = false
    private var settingData: DebugBottomDialog.SettingData? = null

    private lateinit var deformWrapper: CGEDeformFilterWrapper
    private lateinit var currentBodyMode: DeformBodyMode
    private lateinit var bodyCorrect: AbstractBodyCorrect
    private lateinit var binding: FragmentEditBodyBinding
    private lateinit var pathList: List<String>
    private lateinit var resultBitmap: Bitmap
    private val viewModel: BodyViewModel by viewModels()

    private val viewsContainer = mutableListOf<View>()

    override fun surfaceViewBitmap(): Bitmap = editor().getOriginalBitmap()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        viewModel.state.observe(viewLifecycleOwner, this::render)
        viewModel.analysisBrightnessPhoto(editor().getOriginalBitmap())

        binding = FragmentEditBodyBinding.inflate(inflater, container, false)
        initView()
        pathList = getPathList(requireContext(), AssetsDirections.BODY_DIR)

        return binding.root
    }

    private fun render(state: BodyState) = when (state) {
        is BodyState.Content -> {
            photoIsBright = state.photoIsBright
            hideLoadingDialog()
        }
        BodyState.Loading -> {
            showLoadingDialog()
        }
    }

    private fun initView() {
        val llListener = LinearLayoutClickListener()
        binding.chestBtn.setOnClickListener(llListener)
        binding.waistBtn.setOnClickListener(llListener)
        binding.hipsBtn.setOnClickListener(llListener)

        binding.slider.apply {
            labelBehavior = LabelFormatter.LABEL_GONE
            addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    editor().setBeforeAfterButtonVisible(value.roundToInt() != 0 && hasDetailChanges)
                    val correctValue = if (currentBodyMode == HIPS) -value else value
                    deform(correctValue.roundToInt())
                }
            })
        }
        editor().setOnTouchBeforeAfterListener { onTouch ->
            val intensity = if (onTouch) 1F else 0F
            surfaceView.flush(true) {
                deformWrapper.restoreWithIntensity(intensity)
            }
        }

        binding.settingsButton.isVisible = BuildConfig.DEBUG
        binding.settingsButton.throttleClick {
            DebugBottomDialog
                    .newInstance(bodyCorrect.intensity)
                    .show(childFragmentManager)
        }
    }

    override fun initSurfaceView(
            bitmap: Bitmap,
            surfaceCreatedCallback: ((ImageGLSurfaceView) -> Unit)?
    ): ImageGLSurfaceView = super.initSurfaceView(bitmap) { surfaceView ->
        surfaceView.queueEvent {
            var w = bitmap.width
            var h = bitmap.height
            val scaling = min(1280.0f / w, 1280.0f / h)
            if (scaling < 1.0f) {
                w = (w * scaling).toInt()
                h = (h * scaling).toInt()
            }

            deformWrapper = CGEDeformFilterWrapper.create(w, h, 10.0f).apply {
                val handler = surfaceView.imageHandler
                handler.setFilterWithAddres(nativeAddress)
                handler.processFilters()
            }
        }
    }

    override fun process(main: Bitmap, support: Bitmap?): Bitmap = resultBitmap

    private fun applyChanges() {
        surfaceView.getResultBitmap { result ->
            resultBitmap = result
            surfaceView.setImageBitmap(result)
            setChanges()
        }

        onApplyChanges()
        currentState = StateBodyEdit.CHOICE
    }

    private fun onApplyChanges() = when (currentBodyMode) {
        BREAST, WAIST, HIPS -> Unit
    }

    private fun restore() {
        surfaceView.flush(true) { deformWrapper.restore() }
        binding.slider.value = 0F
        currentValue = 0
    }

    private fun deform(value: Int) {
        if (abs(currentValue - value) < 1) return
        hasDetailChanges = true

        surfaceView.flush(true) {
            deformWrapper.restore()
            for (i in 0 until getCount(value, 0)) {
                viewsContainer.forEach { bodyCorrect.deform(it, deformWrapper, value, 0) }
            }
        }
        currentValue = value
    }

    private fun getCount(value: Int, current: Int): Int =
            if (value * current > 0) abs(value - current) else abs(value) + abs(current)

    override fun onSetupToolbar(toolbar: BaseToolbar) {
        super.onSetupToolbar(toolbar)
        toolbar.apply {
            setLeftButtonAction { requireActivity().onBackPressed() }
            setRightButtonAction { handleApplyAction() }
        }
    }

    override fun dialogActionApply() = when (currentState) {
        StateBodyEdit.CHOICE -> editor().applyProcess()
        StateBodyEdit.DETAIL -> applyChanges()
    }

    private fun handleApplyAction() = when {
        currentState == StateBodyEdit.CHOICE && !hasChanges -> nothingIsSelectedToast()
        else -> showApplyDialog()
    }

    override fun dialogActionNavigateBack() = when (currentState) {
        StateBodyEdit.CHOICE -> {
            super.dialogActionNavigateBack()
        }
        StateBodyEdit.DETAIL -> {
            restore()
            currentState = StateBodyEdit.CHOICE
        }
    }

    override fun onBackPressed(): Boolean = when (currentState) {
        StateBodyEdit.CHOICE -> {
            if (hasChanges) false.also { showCancelDialog() }
            else true
        }
        StateBodyEdit.DETAIL -> {
            if (hasDetailChanges) showCancelDialog()
            else currentState = StateBodyEdit.CHOICE

            false
        }
    }

    private fun deleteViews() {
        if (editor().container().childCount <= 1) return

        viewsContainer.forEach { editor().container().removeView(it) }
        viewsContainer.clear()
    }

    private fun getBodyView(): List<View> {
        val x = (surfaceView.renderViewport.width.toFloat()) / 2
        val y = (surfaceView.renderViewport.height.toFloat()) / 2

        return when (currentBodyMode) {
            BREAST -> BreastBuilder()
            WAIST -> WaistBuilder()
            HIPS -> HipsBuilder()
        }.create(requireContext(), x, y, BodyTouchListener(true), photoIsBright)
    }

    inner class LinearLayoutClickListener : View.OnClickListener {

        override fun onClick(view: View?) {
            if (view == null) return

            currentState = StateBodyEdit.DETAIL

            currentBodyMode = when (view.id) {
                R.id.chest_btn -> BREAST
                R.id.waist_btn -> WAIST
                else -> HIPS
            }

            // TODO currentBodyMode.name on obfuscate
            AnalyticEventsUtil.logEvent(OpenItemEvent.Body(currentBodyMode.name))

            val w = surfaceView.renderViewport.width.toFloat()
            val h = surfaceView.renderViewport.height.toFloat()

            viewsContainer.addAll(getBodyView())
            viewsContainer.forEach { editor().container().addView(it) }

            binding.slider.apply {
                when (currentBodyMode) {
                    BREAST -> setup(-DIAPASON, DIAPASON)
                    WAIST -> setup(0F, DIAPASON, 0F)
                    HIPS -> setup(0F, DIAPASON, 0F)
                }
            }

            bodyCorrect = when (currentBodyMode) {
                BREAST -> BreastCorrect(w, h)
                WAIST -> WaistCorrect(w, h)
                HIPS -> HipsCorrect(w, h)
            }
            settingData?.apply(this@BodyEditFragment::debugUpdateBodyCorrect)
        }
    }

    private fun debugUpdateBodyCorrect(settingData: DebugBottomDialog.SettingData) {
        bodyCorrect.intensity = settingData.intensity
    }

    override fun applySettings(settingData: DebugBottomDialog.SettingData) {
        this.settingData = settingData
        debugUpdateBodyCorrect(settingData)
    }
}
