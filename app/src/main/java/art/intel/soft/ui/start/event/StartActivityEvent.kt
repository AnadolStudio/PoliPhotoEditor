package art.intel.soft.ui.start.event

import art.intel.soft.base.event.SingleCustomEvent

sealed class StartActivityEvent : SingleCustomEvent() {

    object NavigateToMainActivityEvent : StartActivityEvent()

    object StartLoadInterstitial : StartActivityEvent()
}
