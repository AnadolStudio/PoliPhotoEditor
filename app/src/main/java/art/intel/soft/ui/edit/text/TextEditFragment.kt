package art.intel.soft.ui.edit.text

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import art.intel.soft.R
import art.intel.soft.base.BaseAction
import art.intel.soft.base.event.SingleEvent
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.Screens
import art.intel.soft.base.firebase.events.implementation.ApplyItemEvent
import art.intel.soft.base.firebase.events.implementation.OpenItemEvent
import art.intel.soft.databinding.TextEditFragmentBinding
import art.intel.soft.ui.edit.BaseEditFragment
import art.intel.soft.ui.edit.text.TextEditFragment.State.BACK_COLOR
import art.intel.soft.ui.edit.text.TextEditFragment.State.FONT
import art.intel.soft.ui.edit.text.TextEditFragment.State.MAIN
import art.intel.soft.ui.edit.text.TextEditFragment.State.TEXT_COLOR
import art.intel.soft.ui.edit.text.font.FontAdapter
import art.intel.soft.ui.edit.text.font.Fonts
import art.intel.soft.utils.CustomPhotoEditListener
import art.intel.soft.utils.slider.ColorChangeListener
import art.intel.soft.utils.throttleClick
import art.intel.soft.view.BaseToolbar
import ja.burhanrashid52.photoeditor.PhotoEditorImpl
import ja.burhanrashid52.photoeditor.graphic.GraphicBorderActions
import kotlin.math.roundToInt

class TextEditFragment : BaseEditFragment(), TextEditorDialogFragment.TextEditorDialogAction {

    override val toolbarTitleId: Int = R.string.edit_text_title

    companion object {
        fun newInstance(): TextEditFragment = TextEditFragment()
    }

    override fun provideScreenName(): Screens = Screens.TEXT

    private enum class State { MAIN, TEXT_COLOR, BACK_COLOR, FONT }

    private val binding: TextEditFragmentBinding by lazy { TextEditFragmentBinding.bind(requireView()) }
    private val viewModel: EditTextViewModel by viewModels()
    private var currentState: State = MAIN
        set(value) {
            if (field == value) return

            field = value

            binding.mainPanel.isVisible = field == MAIN
            binding.textColorPanel.isVisible = field == TEXT_COLOR
            binding.backColorPanel.isVisible = field == BACK_COLOR
            binding.fontsRecycler.isVisible = field == FONT

            editor().toolbar().setTitle(
                    when (field) {
                        MAIN -> toolbarTitleId
                        TEXT_COLOR -> R.string.edit_text_title_text_color
                        BACK_COLOR -> R.string.edit_text_title_back_color
                        FONT -> R.string.edit_text_title_font
                    }
            )
        }

    private val borderData: GraphicBorderActions by lazy {
        GraphicBorderActions(
                leftTopButton = GraphicBorderActions.GraphicBorderData(
                        R.drawable.ic_border_delete,
                        isMovable = false
                ) { textWrapper, _, _ ->
                    currentState = MAIN
                    viewModel.remove(getCurrentTextContainer()?.id)
                    textWrapper.remove()
                    if (editor().getPhotoEditor().childIsEmpty()) clearChanges()
                },
                rightMiddleButton = GraphicBorderActions.GraphicBorderData(
                        R.drawable.ic_waist_end_button,
                        isMovable = true
                ) { textWrapper, x, y ->
                    textWrapper.changeSize(x, y)
                },
                rightBottomButton = GraphicBorderActions.GraphicBorderData(
                        R.drawable.ic_border_change_size,
                        isMovable = true
                ) { textWrapper, x, y ->
                    textWrapper.scale(x, y)
                }
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.text_edit_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.event.observe(viewLifecycleOwner, this::handleEvent)
        initView()
    }

    private fun handleEvent(event: SingleEvent) = when (event) {
        is EditTextEvent.CreateTextEvent -> createTextView(event)
        is EditTextEvent.UpdateTextEvent -> updateCurrentTextView(event)
        is EditTextEvent.SetupMenuEvent -> setupMenu(event)
        else -> Unit
    }

    private fun setupMenu(event: EditTextEvent.SetupMenuEvent) {
        binding.apply {
            backAlphaSlider.value = event.backAlpha.toFloat()
            backColorSeekBar.setSliderValue(event.backColorSliderValue)

            textAlphaSlider.value = event.textAlpha.toFloat()
            textColorSeekBar.setSliderValue(event.textColorSliderValue)

            (fontsRecycler.adapter as? FontAdapter)?.let { adapter ->
                val index = adapter.setSelectItem(event.textFont)
                if (index != -1) fontsRecycler.scrollToPosition(index)
            }
        }
    }

    private fun initView() {
        binding.apply {
            keyboardButton.throttleClick { keyboardButtonAction() }
            fontButton.throttleClick { fontButtonAction() }
            paletteButton.throttleClick { paletteButtonAction() }
            backButton.throttleClick { backButtonAction() }

            backAlphaSlider.addOnChangeListener { _, value, fromUser ->
                if (!fromUser) return@addOnChangeListener

                viewModel.updateText(id = getCurrentTextContainer()?.id, backAlpha = value.roundToInt())
            }
            backColorSeekBar.setOnColorChangeListener(ColorChangeListener { color ->
                viewModel.updateText(
                        id = getCurrentTextContainer()?.id,
                        backColor = color,
                        backColorSliderValue = backColorSeekBar.getSliderValue()
                )
            })

            textAlphaSlider.addOnChangeListener { _, value, fromUser ->
                if (!fromUser) return@addOnChangeListener

                viewModel.updateText(id = getCurrentTextContainer()?.id, textAlpha = value.roundToInt())
            }
            textColorSeekBar.setOnColorChangeListener(ColorChangeListener { color ->
                viewModel.updateText(
                        id = getCurrentTextContainer()?.id,
                        textColor = color,
                        textColorSliderValue = textColorSeekBar.getSliderValue()
                )
            })

            fontsRecycler.itemAnimator = null
            fontsRecycler.adapter = FontAdapter(
                    data = Fonts.values().toList(),
                    action = { font ->
                        AnalyticEventsUtil.logEvent(OpenItemEvent.Text.Font(font.fontName))
                        viewModel.updateText(id = getCurrentTextContainer()?.id, textFont = font)
                    }
            )
        }

        editor().getPhotoEditor().setOnPhotoEditorListener(TextPhotoEditListener())
        showAddTextPanel()
    }

    private fun keyboardButtonAction() {
        AnalyticEventsUtil.logEvent(OpenItemEvent.Text(OpenItemEvent.Text.Item.KEYBOARD))
        showAddTextPanel()
    }

    private fun fontButtonAction() {
        AnalyticEventsUtil.logEvent(OpenItemEvent.Text(OpenItemEvent.Text.Item.FONT))
        validateChangeState { currentState = FONT }
    }

    private fun paletteButtonAction() {
        AnalyticEventsUtil.logEvent(OpenItemEvent.Text(OpenItemEvent.Text.Item.PALETTE))
        validateChangeState { currentState = TEXT_COLOR }
    }

    private fun backButtonAction() {
        AnalyticEventsUtil.logEvent(OpenItemEvent.Text(OpenItemEvent.Text.Item.BACK))
        validateChangeState { currentState = BACK_COLOR }
    }

    private fun validateChangeState(action: BaseAction) {
        if (getCurrentTextContainer() != null) {
            action.invoke()
        } else {
            showToast(R.string.text_empty_text_container_toast)
        }
    }

    private fun showAddTextPanel(text: String? = null) = TextEditorDialogFragment.show(childFragmentManager, text)

    private fun createTextView(event: EditTextEvent.CreateTextEvent) {
        val maxWith = editor().photoEditorView().width
        setChanges()
        editor().getPhotoEditor().addText(event.id, event.text, event.textStyleBuilder, borderData, maxWith)
    }

    private fun updateCurrentTextView(event: EditTextEvent.UpdateTextEvent) {
        val view = getCurrentTextContainer() ?: return

        if (view.id != event.id) return

        editor().getPhotoEditor().editText(view, event.text, event.textStyleBuilder)
    }

    private fun getCurrentTextContainer(): View? = (editor().getPhotoEditor() as? PhotoEditorImpl)?.currentSelectedView

    override fun onSetupToolbar(toolbar: BaseToolbar) {
        super.onSetupToolbar(toolbar)
        toolbar.setRightButtonAction {
            when {
                hasChanges && currentState == MAIN -> showApplyDialog()
                currentState != MAIN -> applyCurrentChanges()
                else -> nothingIsSelectedToast()
            }
        }
    }

    override fun onBackPressed(): Boolean = when (currentState) {
        MAIN -> false.also { showCancelDialog() }
        else -> false.also { backToMainState(viewModel::cancelCurrentChanges) }
    }

    private fun backToMainState(action: BaseAction) {
        action.invoke()
        currentState = MAIN
    }

    override fun onChangeText(inputText: String) {
        viewModel.updateText(
                id = getCurrentTextContainer()?.id,
                text = inputText
        )
    }

    override fun onAddText(inputText: String) = viewModel.createText(inputText)

    inner class TextPhotoEditListener : CustomPhotoEditListener() {

        override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) = showAddTextPanel(text)

        override fun onTouchSourceImage(event: MotionEvent) = backToMainState(viewModel::applyCurrentChanges)

        override fun onChangeSelectedView(view: View?) {
            if (view == null) {
                currentState = MAIN

                return
            }

            viewModel.setupMenu(view.id)
        }
    }

    private fun applyCurrentChanges() {
        when (currentState) {
            TEXT_COLOR -> {
                AnalyticEventsUtil.logEvent(ApplyItemEvent.Text(ApplyItemEvent.Text.Item.PALETTE))
            }
            BACK_COLOR -> {
                AnalyticEventsUtil.logEvent(ApplyItemEvent.Text(ApplyItemEvent.Text.Item.BACK))
            }
            FONT -> {
                AnalyticEventsUtil.logEvent(ApplyItemEvent.Text(ApplyItemEvent.Text.Item.FONT))
                viewModel.analyticEventApplyCurrentFont()
            }
            MAIN -> Unit
        }

        backToMainState(viewModel::applyCurrentChanges)
    }
}
