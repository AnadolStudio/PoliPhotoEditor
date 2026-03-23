package art.intel.soft.ui.edit.text

import art.intel.soft.base.event.SingleCustomEvent
import art.intel.soft.ui.edit.text.font.Fonts
import ja.burhanrashid52.photoeditor.text.TextStyleBuilder

sealed class EditTextEvent : SingleCustomEvent() {

    class CreateTextEvent(val id: Int, val text: String, val textStyleBuilder: TextStyleBuilder) : EditTextEvent()

    class UpdateTextEvent(val id: Int, val text: String, val textStyleBuilder: TextStyleBuilder) : EditTextEvent()

    class SetupMenuEvent(
            val textAlpha: Int,
            val backAlpha: Int,
            val textColorSliderValue: Float,
            val backColorSliderValue: Float,
            val textFont: Fonts,
    ) : EditTextEvent()

}
