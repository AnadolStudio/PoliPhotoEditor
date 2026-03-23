package art.intel.soft.ui.edit.background

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import art.intel.soft.R
import art.intel.soft.base.event.SingleEvent
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.Screens
import art.intel.soft.base.firebase.events.implementation.ClickItemEvent
import art.intel.soft.base.firebase.events.implementation.OpenItemEvent
import art.intel.soft.databinding.FragmentEditCutBinding
import art.intel.soft.ui.edit.BaseEditFragment
import art.intel.soft.ui.edit.background.BrushSettings.Companion.XLARGE
import art.intel.soft.ui.edit.background.BrushSettings.Companion.XSMALL
import art.intel.soft.ui.gallery.GalleryListActivity.GalleryResultContract
import art.intel.soft.utils.CustomPhotoEditListener
import art.intel.soft.utils.bitmaputils.CutUtils.createNullBackground
import art.intel.soft.utils.bitmaputils.CutUtils.getBitmapFromColor
import art.intel.soft.utils.bitmaputils.scaleRatioInscribed
import art.intel.soft.utils.setup
import art.intel.soft.utils.slider.ColorChangeListener
import art.intel.soft.utils.throttleClick
import art.intel.soft.utils.touchlisteners.ImageTouchListener
import art.intel.soft.view.BaseToolbar
import ja.burhanrashid52.photoeditor.graphic.GraphicBorderActions
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import ja.burhanrashid52.photoeditor.view.OnDrawListener

class BackgroundCutEditFragment : BaseEditFragment(), EmptyMaskBottomDialog.Action, RefreshBottomDialog.Action {

    override val toolbarTitleId: Int = R.string.edit_background_title

    companion object {

        fun newInstance(): BackgroundCutEditFragment = BackgroundCutEditFragment()
    }

    override fun provideScreenName(): Screens = Screens.BACKGROUND

    private sealed class State {

        object CutBackground : State()

        sealed class ChoiceBackground : State() {
            object Base : ChoiceBackground()
            object Color : ChoiceBackground()
        }
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) { FragmentEditCutBinding.inflate(layoutInflater) }
    private val sizeOriginal by lazy(LazyThreadSafetyMode.NONE) {
        val originalBitmap = editor().getOriginalBitmap()
        val mainePanel = editor().currentSizeOfMainePanel()
        val scale = scaleRatioInscribed(mainePanel.x, mainePanel.y, originalBitmap.width, originalBitmap.height)

        return@lazy Point((originalBitmap.width * scale).toInt(), (originalBitmap.height * scale).toInt())
    }
    private val viewModel: BackgroundViewModel by viewModels()
    private var currentState: State = State.CutBackground
        set(value) {
            field = value

            binding.cutPanel.isVisible = value is State.CutBackground
            binding.backgroundRecyclerView.isVisible = value is State.ChoiceBackground.Base
            binding.colorSeekBarContainer.isVisible = value is State.ChoiceBackground.Color
        }

    private val touchListener: ImageTouchListener by lazy(LazyThreadSafetyMode.NONE) {
        ImageTouchListener(editor().photoEditorView(), true, true)
    }
    private lateinit var galleryLauncher: ActivityResultLauncher<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.brushSettingsState.observe(viewLifecycleOwner, this::renderBrushState)
        viewModel.state.observe(viewLifecycleOwner, this::renderState)
        viewModel.event.observe(viewLifecycleOwner, this::handleEvent)

        setupCutState()
        setupChoiceState()
        editor().getPhotoEditor().setOnPhotoEditorListener(
                OnDrawListener {
                    if (hasChanges) return@OnDrawListener
                    if (editor().photoEditorView().drawingView.hasDrawingPath()) {
                        setChanges()
                        setRebootToolbarButton()
                    }
                }
        )

        viewModel.createMask(editor().getOriginalBitmap())
    }

    private fun setRebootToolbarButton() {
        editor().toolbar().setLeftButtonIcon(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_turn_left))
    }

    private fun handleEvent(event: SingleEvent) = when (event) {
        is BackgroundEvent.ClearEvent -> clearDrawPanel(event.mask)
        is BackgroundEvent.MaskNotExistEvent -> nothingIsSelectedToast()
        else -> Unit
    }

    private fun renderState(state: BackgroundState) = when (state) {
        is BackgroundState.Content -> renderContentState(state)
        BackgroundState.Error -> showErrorDialog()
        BackgroundState.Loading -> showLoadingDialog()
    }

    private fun showErrorDialog() {
        hideLoadingDialog()
        requireView().post { EmptyMaskBottomDialog().show(childFragmentManager) }
    }

    private fun renderContentState(content: BackgroundState.Content) = when (content) {
        is BackgroundState.Content.CutContent -> showCutContent(content)
        is BackgroundState.Content.ChoiceContent -> showChoiceContent(content)
        else -> Unit
    }.also { hideLoadingDialog() }

    private fun showChoiceContent(content: BackgroundState.Content.ChoiceContent) {
        clearChanges()
        currentState = State.ChoiceBackground.Base

        binding.backgroundRecyclerView.adapter = BackgroundDslAdapter(
                pathList = content.backgroundPathList,
                onSelect = { _, path ->
                    when (path) {
                        BackgroundViewModel.CUSTOM -> openGallery()
                        BackgroundViewModel.COLOR -> openColor()
                        else -> setBackground(path)
                    }
                }
        ).setup()

        with(editor()) {
            defaultDraw(null)
            getPhotoEditor().clearAllViews()
            getPhotoEditor().setBrushDrawingMode(false)
            setImage(createNullBackground(editor().currentSizeOfMainePanel()))
            getPhotoEditor().addImage(content.cutBitmap, GraphicBorderActions())
        }
    }

    private fun openGallery() {
        viewModel.onOpenItem(OpenItemEvent.SubItems.OWN_PHOTO.value)
        galleryLauncher.launch(CHOOSE_PHOTO_WITHOUT_AD)
    }

    private fun openColor() {
        viewModel.onOpenItem(OpenItemEvent.SubItems.COLOR.value)
        currentState = State.ChoiceBackground.Color
    }

    private fun setBackground(path: String) {
        AnalyticEventsUtil.getNameFromPath(path)?.let(viewModel::onOpenItem)
        setChanges()
        editor().setImage(path, ImageView.ScaleType.FIT_CENTER)
    }

    private fun showCutContent(content: BackgroundState.Content.CutContent) {
        currentState = State.CutBackground
        editor().getPhotoEditor().clearAllViews()
        editor().rebootToOriginalImage()
        editor().getPhotoEditor().setBrushDrawingMode(true)

        when (content.drawingBitmap) {
            null -> clearChanges().also { defaultDraw(content.maskBitmap) }
            else -> setChanges().also { defaultDraw(content.drawingBitmap, isClearable = true) }
        }

        if (hasChanges) setRebootToolbarButton()
    }

    private fun renderBrushState(settings: BrushSettings) {
        editor().getPhotoEditor().apply {
            setBrushDrawingMode(true)

            setShape(
                    ShapeBuilder()
                            .withShapeColor(settings.color)
                            .withShapeSize(settings.size)
            )

            if (settings.mode == BrushMode.ERASER) brushEraser()
        }
    }

    override fun onSetupToolbar(toolbar: BaseToolbar) {
        super.onSetupToolbar(toolbar)
        toolbar.setRightButtonAction {
            when (currentState) {
                State.CutBackground -> cutBackground()
                is State.ChoiceBackground -> applyChanges()
            }
        }
    }

    private fun applyChanges() {
        if (hasChanges) {
            showApplyDialog()
        } else {
            nothingIsSelectedToast()
        }
    }

    override fun dialogActionApply() {
        viewModel.applyItem()
        super.dialogActionApply()
    }

    private fun clearDrawPanel(mask: Bitmap?) {
        AnalyticEventsUtil.logEvent(ClickItemEvent.Background(ClickItemEvent.Background.ItemName.REFRESH_DRAWING))
        editor().getPhotoEditor().clearAllViews()
        clearChanges()
        defaultDraw(mask)
    }

    private fun defaultDraw(bitmap: Bitmap?, isClearable: Boolean = false) {
        defaultStateScalePanel()
        editor().toolbar().setLeftButtonIcon(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_arrow_back))

        val drawingView = editor().getPhotoEditor().drawingView

        if (isClearable) drawingView.setDrawableBitmap(bitmap) else drawingView.setDefaultBitmap(bitmap)

        drawingView.invalidate()
    }

    private fun defaultStateScalePanel() {
        touchListener.getDefaultState(editor().photoEditorView(), true)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupCutState() {
        editor().getPhotoEditor().drawingView.setPorterDuffXferMode(PorterDuff.Mode.SRC)
        binding.sizeSlider.setup(XSMALL, XLARGE)
        binding.sizeSlider.addOnChangeListener { _, value, _ -> viewModel.updateBrushSettings(size = value) }
        binding.brushButton.throttleClick { viewModel.updateBrushSettings(mode = BrushMode.DRAW) }
        binding.eraserButton.throttleClick { viewModel.updateBrushSettings(mode = BrushMode.ERASER) }

        editor().photoEditorView().setOnTouchListener(touchListener)
    }

    private fun setupChoiceState() {
        galleryLauncher = registerForActivityResult(GalleryResultContract(), this::setOwnBackground)

        binding.colorSeekBar.setOnColorChangeListener(ColorChangeListener { newColor ->
            editor().setImage(getBitmapFromColor(newColor, sizeOriginal.x, sizeOriginal.y))
            setChanges()
        })
    }

    private fun cutBackground() {
        AnalyticEventsUtil.logEvent(ClickItemEvent.Background(ClickItemEvent.Background.ItemName.NEXT))
        viewModel.cutBackground(
                mainViewBitmap = editor().getOriginalBitmap(),
                drawViewBitmap = editor().drawingViewBitmap(),
                hasChanges = hasChanges
        )
    }

    override fun onBackPressed(): Boolean = when {
        currentState is State.CutBackground && !hasChanges -> true
        currentState is State.CutBackground && hasChanges -> false.also { RefreshBottomDialog().show(childFragmentManager) }
        currentState is State.ChoiceBackground.Base -> false.also { viewModel.backToCut() }
        currentState is State.ChoiceBackground.Color -> false.also { currentState = State.ChoiceBackground.Base }
        else -> false.also { viewModel.clearDrawPanel() }
    }

    override fun dialogActionRefresh() = viewModel.clearDrawPanel()

    private fun setOwnBackground(path: String?) {
        if (path == null) return
        editor().setImage(path, null)
        setChanges()
    }

    override fun dialogActionNavigateBack() {
        AnalyticEventsUtil.logEvent(ClickItemEvent.Background.NotFound(ClickItemEvent.Background.NotFound.ItemName.BACK))
        callOnBackPressedContainer(this)
    }

    override fun dialogActionManual() {
        AnalyticEventsUtil.logEvent(ClickItemEvent.Background.NotFound(ClickItemEvent.Background.NotFound.ItemName.MANUAL))
    }

    override fun onDestroy() {
        super.onDestroy()
        defaultStateScalePanel()
        editor().photoEditorView().setOnTouchListener(null)

        editor().getPhotoEditor().setOnPhotoEditorListener(CustomPhotoEditListener())
    }
}
