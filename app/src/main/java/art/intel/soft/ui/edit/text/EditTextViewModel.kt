package art.intel.soft.ui.edit.text

import android.app.Application
import android.view.View
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.implementation.ApplyItemEvent
import art.intel.soft.base.view_model.BaseAndroidViewModel
import art.intel.soft.ui.edit.text.font.Fonts
import art.intel.soft.utils.ColorUtils
import art.intel.soft.utils.onNext
import ja.burhanrashid52.photoeditor.text.TextStyleBuilder

class EditTextViewModel(application: Application) : BaseAndroidViewModel<Unit>(application) {

    private val textItems = mutableMapOf<Int, TextItem>()
    private var currentTextItem: CurrentItem? = null

    fun createText(text: String) {
        val baseTextItem = TextItem(text = text)
        val textStyleBuilder = createTextStyleBuilder(baseTextItem)

        val id = View.generateViewId()
        textItems[id] = baseTextItem
        _singleEvent.onNext(EditTextEvent.CreateTextEvent(id = id, text = text, textStyleBuilder = textStyleBuilder))

        currentTextItem = CurrentItem(id, baseTextItem.copy())
    }

    private fun createTextStyleBuilder(textItem: TextItem): TextStyleBuilder = TextStyleBuilder().apply {
        updateColor(color = textItem.textColor, alpha = textItem.textAlpha)
        updateBackground(color = textItem.backColor, alpha = textItem.backAlpha)
        updateTextAppearance(textAppearance = textItem.textFont.id)

        withTextSize(TextStyleBuilder.DEFAULT_SIZE)
    }

    private fun TextStyleBuilder.updateTextAppearance(textAppearance: Int) = withTextAppearance(textAppearance)

    private fun TextStyleBuilder.updateColor(color: Int, alpha: Int) =
            withTextColor(ColorUtils.getColorWithAlpha(color, alpha))

    private fun TextStyleBuilder.updateBackground(color: Int, alpha: Int) =
            withBackgroundColor(ColorUtils.getColorWithAlpha(color, alpha))

    fun setupMenu(id: Int) {
        val currentTextItem = this.currentTextItem
                ?.let {
                    if (id == it.id) {
                        it.item
                    } else {
                        applyCurrentChanges()
                        null
                    }
                }
                ?: textItems[id]
                ?: return

        _singleEvent.onNext(
                EditTextEvent.SetupMenuEvent(
                        textAlpha = currentTextItem.textAlpha,
                        backAlpha = currentTextItem.backAlpha,
                        textColorSliderValue = currentTextItem.textColorSliderValue,
                        backColorSliderValue = currentTextItem.backColorSliderValue,
                        textFont = currentTextItem.textFont,
                )
        )
    }

    fun updateText(
            id: Int?,
            text: String? = null,
            textColor: Int? = null,
            textAlpha: Int? = null,
            backColor: Int? = null,
            backAlpha: Int? = null,
            textColorSliderValue: Float? = null,
            backColorSliderValue: Float? = null,
            textFont: Fonts? = null,
    ) {
        if (id == null) return

        val previousTextItem = currentTextItem?.item ?: textItems[id]?.copy() ?: return

        val newTextItem = previousTextItem.copy(
                text = text ?: previousTextItem.text,
                textColor = textColor ?: previousTextItem.textColor,
                textAlpha = textAlpha ?: previousTextItem.textAlpha,
                backColor = backColor ?: previousTextItem.backColor,
                backAlpha = backAlpha ?: previousTextItem.backAlpha,
                textColorSliderValue = textColorSliderValue ?: previousTextItem.textColorSliderValue,
                backColorSliderValue = backColorSliderValue ?: previousTextItem.backColorSliderValue,
                textFont = textFont ?: previousTextItem.textFont,
        )

        currentTextItem = CurrentItem(id, newTextItem)

        val textStyleBuilder = createTextStyleBuilder(newTextItem)

        _singleEvent.onNext(
                EditTextEvent.UpdateTextEvent(
                        id = id,
                        text = newTextItem.text,
                        textStyleBuilder = textStyleBuilder
                )
        )
    }

    fun remove(id: Int?) {
        if (id == null) return
        textItems.remove(id)
    }

    fun analyticEventApplyCurrentFont() {
        val textItem = currentTextItem ?: return
        AnalyticEventsUtil.logEvent(ApplyItemEvent.Text.Font(textItem.item.textFont.fontName))
    }

    fun applyCurrentChanges() {
        val currentTextItem = this.currentTextItem ?: return

        textItems[currentTextItem.id] = currentTextItem.item

        this.currentTextItem = null
    }

    fun cancelCurrentChanges() {
        val currentTextItem = this.currentTextItem ?: return
        val previousItem = textItems[currentTextItem.id] ?: return

        val textStyleBuilder = createTextStyleBuilder(previousItem)
        this.currentTextItem = currentTextItem.copy(item = previousItem.copy())

        _singleEvent.onNext(
                EditTextEvent.UpdateTextEvent(
                        id = currentTextItem.id,
                        text = previousItem.text,
                        textStyleBuilder = textStyleBuilder
                )
        )
        setupMenu(currentTextItem.id)
    }

    private data class CurrentItem(val id: Int, val item: TextItem)

}
