package art.intel.soft.base.firebase.events.implementation

import art.intel.soft.base.firebase.events.BaseAnalyticsEvent
import art.intel.soft.base.firebase.events.Screens

abstract class ClickItemEvent(action: String, itemName: String) : BaseAnalyticsEvent.WithItem(
        name = NAME_EVENT,
        action = action,
        itemName = itemName
) {
    private companion object {
        const val NAME_EVENT = "click"
    }

    open class Background(
            itemName: String,
            action: String? = null
    ) : ClickItemEvent(
            action = Screens.BACKGROUND.value.concat(action),
            itemName = itemName
    ) {

        constructor(itemName: ItemName) : this(itemName = itemName.value)

        enum class Actions(val value: String) {
            NOT_FOUND("not_found"),
        }

        enum class ItemName(val value: String) {
            NEXT("Next"),
            REFRESH_DRAWING("Refresh Drawing"),
        }

        class NotFound(itemName: ItemName) : Background(itemName = itemName.value, action = Actions.NOT_FOUND.value) {
            enum class ItemName(val value: String) {
                BACK("Back"),
                MANUAL("Manual"),
            }
        }
    }

    open class SaveScreen(itemName: ItemName) : ClickItemEvent(
            action = Screens.SAVE_SCREEN.value,
            itemName = itemName.value
    ) {

        enum class ItemName(val value: String) {
            BACK("Back"),
            SHARE("Share"),
            START_AGAIN("Start Again"),
        }
    }

}
