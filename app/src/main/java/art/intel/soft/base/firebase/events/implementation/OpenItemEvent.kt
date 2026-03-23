package art.intel.soft.base.firebase.events.implementation

import art.intel.soft.base.firebase.events.BaseAnalyticsEvent
import art.intel.soft.base.firebase.events.Screens

abstract class OpenItemEvent(action: String, itemName: String) : BaseAnalyticsEvent.WithItem(
        name = NAME_EVENT,
        action = action,
        itemName = itemName
) {

    constructor(action: Screens, itemName: String) : this(action = action.value, itemName = itemName)

    private companion object {
        const val NAME_EVENT = "open"
    }

    enum class SubItems(val value: String) {
        OWN_PHOTO("Own Photo"),
        COLOR("Color"),
    }

    class Frames(itemName: String) : OpenItemEvent(action = Screens.FRAMES, itemName = itemName)

    class Filters(itemName: String) : OpenItemEvent(action = Screens.FILTERS, itemName = itemName)

    class Forms(itemName: String) : OpenItemEvent(action = Screens.FORMS, itemName = itemName)

    class Improve(itemName: String) : OpenItemEvent(action = Screens.IMPROVE, itemName = itemName)

    class Effects(itemName: String) : OpenItemEvent(action = Screens.EFFECTS, itemName = itemName)

    class Body(itemName: String) : OpenItemEvent(action = Screens.BODY, itemName = itemName)

    class Collage(itemName: String) : OpenItemEvent(action = Screens.COLLAGE, itemName = itemName)

    class Crop(itemName: String) : OpenItemEvent(action = Screens.CROP, itemName = itemName)

    class EditMenu(itemName: String) : OpenItemEvent(action = Screens.EDIT_MENU, itemName = itemName)

    class Background(itemName: String) : OpenItemEvent(action = Screens.BACKGROUND, itemName = itemName)

    open class Stickers(
            itemName: String,
            action: String? = null
    ) : OpenItemEvent(action = Screens.STICKERS.value.concat(action), itemName = itemName) {

        class Category(itemName: String) : Stickers(itemName = itemName, action = "category")
    }

    open class Text(
            itemName: String,
            action: String? = null
    ) : OpenItemEvent(action = Screens.TEXT.value.concat(action), itemName = itemName) {

        constructor(itemName: Item, action: String? = null) : this(itemName = itemName.value, action = action)

        enum class Item(val value: String) {
            KEYBOARD("Keyboard"),
            PALETTE("Palette"),
            BACK("Back"),
            FONT("Font"),
        }

        class Font(itemName: String) : Text(itemName = itemName, action = "font")
    }

    class StartMenu(itemName: String) : OpenItemEvent(action = Screens.START_MENU, itemName = itemName)


}
