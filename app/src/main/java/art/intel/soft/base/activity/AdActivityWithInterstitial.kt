package art.intel.soft.base.activity

import art.intel.soft.base.BaseAction

abstract class AdActivityWithInterstitial : BaseActivity() {

    protected fun loadInterstitial() = Unit

    protected fun showInterstitial(
            clickAction: BaseAction,
            showedAction: BaseAction,
            action: BaseAction
    ) = action.invoke()

}
