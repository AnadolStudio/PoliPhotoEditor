package art.intel.soft.base.firebase.events.implementation

import art.intel.soft.base.firebase.events.BaseAnalyticsEvent
import art.intel.soft.base.firebase.events.Screens

abstract class ApplyItemEvent(action: String, itemName: String) : BaseAnalyticsEvent.WithItem(
        name = NAME_EVENT,
        action = action,
        itemName = itemName
) {

    constructor(action: Screens, itemName: String) : this(action = action.value, itemName = itemName)

    private companion object {
        const val NAME_EVENT = "apply"
    }

    enum class SubItems(val value: String) {
        OWN_PHOTO("Own Photo"),
        COLOR("Color"),
    }

    class Frames(itemName: String) : ApplyItemEvent(action = Screens.FRAMES, itemName = itemName)

    class Filters(itemName: String) : ApplyItemEvent(action = Screens.FILTERS, itemName = itemName)

    class Forms(itemName: String) : ApplyItemEvent(action = Screens.FORMS, itemName = itemName)

    class Improve(itemName: String) : ApplyItemEvent(action = Screens.IMPROVE, itemName = itemName)

    class Effects(itemName: String) : ApplyItemEvent(action = Screens.EFFECTS, itemName = itemName)

    class Body(itemName: String) : ApplyItemEvent(action = Screens.BODY, itemName = itemName)

    class Collage(itemName: String) : ApplyItemEvent(action = Screens.COLLAGE, itemName = itemName)

    class Crop(itemName: String) : ApplyItemEvent(action = Screens.CROP, itemName = itemName)

    class EditMenu(itemName: String) : ApplyItemEvent(action = Screens.EDIT_MENU, itemName = itemName)

    class Background(itemName: String) : ApplyItemEvent(action = Screens.BACKGROUND, itemName = itemName)

    open class Stickers(
            itemName: String,
            action: String? = null
    ) : ApplyItemEvent(action = Screens.STICKERS.value.concat(action), itemName = itemName) {

        class Category(itemName: String) : Stickers(itemName = itemName, action = "category")
    }

    open class Text(
            itemName: String,
            action: String? = null
    ) : ApplyItemEvent(action = Screens.TEXT.value.concat(action), itemName = itemName) {

        constructor(itemName: Item, action: String? = null) : this(itemName = itemName.value, action = action)

        enum class Item(val value: String) {
            KEYBOARD("Keyboard"),
            PALETTE("Palette"),
            BACK("Back"),
            FONT("Font"),
        }

        class Font(itemName: String) : Text(itemName = itemName, action = "font")
    }

    class StartMenu(itemName: String) : ApplyItemEvent(action = Screens.START_MENU, itemName = itemName)


}
