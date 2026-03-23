package art.intel.soft.base.firebase.events.implementation

import art.intel.soft.base.firebase.events.BaseAnalyticsEvent

abstract class ClickEvent(action: String) : BaseAnalyticsEvent(
    name = NAME_EVENT,
    action = action,
) {
    private companion object {
        const val NAME_EVENT = "click"
    }

    open class Edit(
        action: String? = null
    ) : ClickEvent(
        action = ACTION_NAME.concat(action),
    ) {
        private companion object {
            const val ACTION_NAME = "edit"
        }

        class Save : Edit(action = ACTION_NAME) {
            private companion object {
                const val ACTION_NAME = "save"
            }
        }
    }

}