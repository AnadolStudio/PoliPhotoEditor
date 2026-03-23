package art.intel.soft.base.firebase.events.implementation

import art.intel.soft.base.firebase.events.BaseAnalyticsEvent

abstract class AdEvent(
        action: String,
        itemName: String
) : BaseAnalyticsEvent.WithItem(
        name = NAME_EVENT,
        itemName = itemName,
        action = action
) {
    private companion object {
        const val NAME_EVENT = "ad"
    }

    enum class EventName(val value: String) {
        BANNER("banner"),
        REWARDED("rewarded"),
        INTERSTITIAL("interstitial"),
    }

    enum class SubEventName(val value: String) {
        CLICK("click"),
        SHOWED("showed"),
    }

    abstract class Banner(action: String, itemName: ItemName) : AdEvent(
            action = EventName.BANNER.value.concat(action),
            itemName = itemName.value
    ) {
        enum class ItemName(val value: String) {
            BACKGROUND("Background"),
            BODY("Body"),
            BRUSH("Brush"),
            COLLAGE("Collage"),
            CROP("Crop"),
            EDIT_MENU("EditMenu"),
            EFFECTS("Effects"),
            FILTERS("Filters"),
            FORMS("Forms"),
            FRAMES("Frames"),
            GALLERY("Gallery"),
            IMPROVE("Improve"),
            SAVE("Save"),
            START_MENU("StartMenu"),
            STICKER("Sticker"),
            TEXT("Text");
        }

        class Click(itemName: ItemName) : Banner(SubEventName.CLICK.name, itemName)
        class Showed(itemName: ItemName) : Banner(SubEventName.SHOWED.name, itemName)
    }

    abstract class Rewarded(action: String, itemName: ItemName) : AdEvent(
            action = EventName.REWARDED.value.concat(action),
            itemName = itemName.value
    ) {
        enum class ItemName(val value: String) {
            BACKGROUND("Background"),
            BODY("Body")
        }

        class Showed(itemName: ItemName) : Rewarded(SubEventName.SHOWED.name, itemName)
    }

    abstract class Interstitial(action: String, itemName: ItemName) : AdEvent(
            action = EventName.INTERSTITIAL.value.concat(action),
            itemName = itemName.value
    ) {
        enum class ItemName(val value: String) {
            OPEN_APP("Open App"),
            SAVE("Save"),
            GALLERY("Gallery")
        }

        class Click(itemName: ItemName) : Interstitial(SubEventName.CLICK.name, itemName)
        class Showed(itemName: ItemName) : Interstitial(SubEventName.SHOWED.name, itemName)
    }
}
