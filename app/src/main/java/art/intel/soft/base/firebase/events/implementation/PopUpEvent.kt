package art.intel.soft.base.firebase.events.implementation

import art.intel.soft.base.firebase.events.BaseAnalyticsEvent
import art.intel.soft.base.firebase.events.Screens

abstract class PopUpEvent(action: String, itemName: String) : BaseAnalyticsEvent.WithItem(
        name = NAME_EVENT,
        action = action,
        itemName = itemName
) {
    private companion object {
        const val NAME_EVENT = "pop_up"
    }

    enum class EventName(val value: String) {
        BACK("back"),
        SAVE("save"),
        APPLY("apply"),
        CANCEL("cancel"),
    }

    open class Back(itemName: String, action: String) : PopUpEvent(
            action = EventName.BACK.value.concat(action),
            itemName = itemName,
    ) {
        class Apply(itemName: String) : Back(action = EventName.APPLY.value, itemName = itemName)
        class Cancel(itemName: String) : Back(action = EventName.CANCEL.value, itemName = itemName)
    }

    open class Save(itemName: String, action: String) : PopUpEvent(
            action = EventName.SAVE.value.concat(action),
            itemName = itemName,
    ) {
        class Apply(itemName: String) : Save(action = EventName.APPLY.value, itemName = itemName)
        class Cancel(itemName: String) : Save(action = EventName.CANCEL.value, itemName = itemName)
    }
}
