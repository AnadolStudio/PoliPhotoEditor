package art.intel.soft.ui.edit.improve

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.core.view.isVisible
import art.intel.soft.R
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.Screens
import art.intel.soft.base.firebase.events.implementation.ApplyItemEvent
import art.intel.soft.base.firebase.events.implementation.OpenItemEvent
import art.intel.soft.databinding.FragmentEditImproveBinding
import art.intel.soft.extention.onFalse
import art.intel.soft.ui.edit.FragmentCreatedCallback
import art.intel.soft.ui.edit.SurfaceEditFragment
import art.intel.soft.ui.edit.improve.ImproveContainer.FunctionMode
import art.intel.soft.ui.edit.improve.recycler.ImproveListAdapter
import art.intel.soft.utils.changeViewSize
import art.intel.soft.utils.slider.RealFormatter
import art.intel.soft.view.BaseToolbar
import com.anadolstudio.library.curvestool.listener.CurvesValuesChangeListener
import com.anadolstudio.library.curvestool.view.CurvesView
import com.anadolstudio.mapper.Function
import com.anadolstudio.mapper.implementation.adjust.BrightnessFunction
import com.anadolstudio.mapper.implementation.adjust.ContrastFunction
import com.anadolstudio.mapper.implementation.adjust.SaturationFunction
import com.anadolstudio.mapper.implementation.adjust.WarmFunction
import com.anadolstudio.mapper.implementation.curve.CurveFunction
import com.anadolstudio.mapper.implementation.selcolor.SelectiveColorFunction
import org.wysaid.nativePort.CGENativeLibrary
import kotlin.LazyThreadSafetyMode.NONE

class ImproveEditFragment : SurfaceEditFragment() {

    override val toolbarTitleId: Int = R.string.edit_improve_title

    companion object {
        fun newInstance(callback: FragmentCreatedCallback?) = ImproveEditFragment().apply {
            this.callback = callback
        }
    }

    override fun provideScreenName(): Screens = Screens.IMPROVE

    private var improveContainer = ImproveContainer()
    private var currentMode = FunctionMode.NONE
        set(value) {
            field = value

            improveContainer.getValue(value)
                    ?.let { adjustValue -> binding.slider.value = adjustValue }
                    ?: let { binding.slider.value = (binding.slider.valueTo - binding.slider.valueFrom) / 2 }
            viewState = value.toViewState()
        }

    private var currentValue = 0f

    private lateinit var adapter: ImproveListAdapter
    private lateinit var curvesView: CurvesView
    private val binding by lazy(NONE) { FragmentEditImproveBinding.inflate(layoutInflater) }

    protected var viewState: ImproveViewState = ImproveViewState.DEFAULT
        set(value) {
            field = value
            binding.mainLl.isVisible = value == ImproveViewState.DEFAULT
            editor().showSelectiveColor(value == ImproveViewState.SELECT_COLOR)
            binding.selectiveColorButtonsView.isVisible = value == ImproveViewState.SELECT_COLOR
            curvesView.isVisible = value == ImproveViewState.CURVE
            binding.curvesViewButtons.isVisible = value == ImproveViewState.CURVE
            binding.sliderContainer.isVisible = value == ImproveViewState.DETAIL
        }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() {
        adapter = ImproveListAdapter(ImproveItem.values().toMutableList()) { improveItem ->
            currentMode = improveItem.toFunctionMode()
            // TODO currentMode.name on obfuscate
            AnalyticEventsUtil.logEvent(OpenItemEvent.Improve(currentMode.name))
        }

        binding.mainRv.adapter = adapter
        initSupportLayout()

        editor().getSelectiveColor().setDataChangeListener { func ->
            if (currentMode != FunctionMode.SELECT_COLOR) return@setDataChangeListener
            previewImprove(currentMode, func)
        }
        binding.selectiveColorButtonsView.bindSelectiveColorView(editor().getSelectiveColor())
        editor().setOnTouchBeforeAfterListener { onTouch ->
            when (onTouch) {
                true -> surfaceView.setFilterWithConfig(Function.Empty.getFunctions())
                false -> surfaceView.setFilterWithConfig(improveContainer.getFunctions())
            }
        }
    }

    override fun initInOnResume() = initCurveView()

    private fun initCurveView() {
        curvesView = CurvesView(requireContext(), null, 0)
        curvesView.isVisible = false
        val workSpace = editor().workSpace()
        val bitmap = surfaceViewBitmap()
        val size = changeViewSize(bitmap.width, bitmap.height, workSpace.x, workSpace.y)
        curvesView.layoutParams = LayoutParams(size.x, size.y)
        curvesView.requestLayout()
        editor().addView(curvesView)

        curvesView.setChangeListener(
                CurvesValuesChangeListener.Save(
                        onChanged = { white, red, green, blue ->
                            if (currentMode != FunctionMode.CURVE) return@Save
                            previewImprove(currentMode, CurveFunction(white, red, green, blue))
                        },
                        onReset = {}
                )
        )

        binding.curvesViewButtons.bindCurvesView(curvesView)
    }

    private fun initSupportLayout() {
        with(binding.slider) {
            setLabelFormatter(RealFormatter())
            addOnChangeListener { _, value, fromUser ->
                if (currentValue.toInt() != value.toInt() && fromUser) {
                    currentValue = value
                    previewImprove(currentMode, currentMode.toFunction(value), currentValue)
                }
            }
        }
    }

    private fun previewImprove(
            mode: FunctionMode? = null,
            function: Function? = null,
            value: Float? = null
    ) {
        if (function != null && mode != null) {
            improveContainer.putTemp(
                    mode,
                    ImproveContainer.FunctionWrapper(value, function)
            )
        }

        surfaceView.queueEvent {
            surfaceView.setFilterWithConfig(improveContainer.getFunctions())
        }
    }

    override fun onChangeAction(hasChanges: Boolean) = editor().setBeforeAfterButtonVisible(hasChanges)

    override fun process(main: Bitmap, support: Bitmap?): Bitmap =
            CGENativeLibrary.filterImage_MultipleEffects(main, improveContainer.getFunctions(), 1.0f)

    private fun FunctionMode.toViewState(): ImproveViewState = when (this) {
        FunctionMode.SELECT_COLOR -> ImproveViewState.SELECT_COLOR
        FunctionMode.CURVE -> ImproveViewState.CURVE
        FunctionMode.NONE -> ImproveViewState.DEFAULT
        else -> ImproveViewState.DETAIL
    }

    private fun FunctionMode.toFunction(value: Float): Function? = when (this) {
        FunctionMode.BRIGHTNESS -> BrightnessFunction(value.mapNegative().toFiftyPercent())
        FunctionMode.CONTRAST -> ContrastFunction(value.mapPositive())
        FunctionMode.SATURATION -> SaturationFunction(value.mapDefault().toTwoHundredPercent())
        FunctionMode.WARMTH -> WarmFunction(value.mapNegative())
        else -> null
    }

    private fun Float.mapDefault(): Float = (this / 100F)

    private fun Float.mapPositive(): Float = ((this + 50) / 100F)

    private fun Float.mapNegative(): Float = ((this - 50) / 100F)

    private fun Float.toFiftyPercent(): Float = this * 0.5F

    private fun Float.toTwoHundredPercent(): Float = this * 2

    override fun onSetupToolbar(toolbar: BaseToolbar) {
        super.onSetupToolbar(toolbar)
        toolbar.setRightButtonAction(this::applyChanges)
    }

    private fun applyChanges() = when (currentMode) {
        FunctionMode.NONE -> {
            if (hasChanges) showApplyDialog() else nothingIsSelectedToast()
        }
        else -> improveContainer.apply().also {
            AnalyticEventsUtil.logEvent(ApplyItemEvent.Improve(currentMode.name))
            currentMode = FunctionMode.NONE
            adapter.clearSelectedItem()
            improveContainer.apply()
            setChanges()
        }
    }

    override fun onBackPressed(): Boolean = when (viewState) {
        ImproveViewState.DEFAULT -> {
            (!hasChanges).onFalse { showCancelDialog() }
        }
        ImproveViewState.DETAIL, ImproveViewState.CURVE, ImproveViewState.SELECT_COLOR -> {
            if (viewState == ImproveViewState.CURVE) {
                (improveContainer.getFunction(FunctionMode.CURVE) as? CurveFunction)
                        ?.let { curveFunc -> curvesView.resetTo(curveFunc.rgb, curveFunc.r, curveFunc.g, curveFunc.b) }
                        ?: let { curvesView.reset() }
            }

            if (viewState == ImproveViewState.SELECT_COLOR) {
                (improveContainer.getFunction(FunctionMode.SELECT_COLOR) as? SelectiveColorFunction)
                        ?.let(editor().getSelectiveColor()::resetTo)
                        ?: let { editor().getSelectiveColor().reset() }
            }

            improveContainer.clearTemp()
            previewImprove()
            currentMode = FunctionMode.NONE
            adapter.clearSelectedItem()

            false
        }
    }

}
