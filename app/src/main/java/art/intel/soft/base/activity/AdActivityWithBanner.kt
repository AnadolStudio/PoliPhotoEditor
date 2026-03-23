package art.intel.soft.base.activity

import android.os.Bundle

abstract class AdActivityWithBanner : AdActivityWithInterstitial() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAd()
    }

    protected open fun initAd() = Unit

    override fun onResume() {
        super.onResume()
        updateVisibleAd()
    }

    protected fun updateVisibleAd() = Unit

}
