package art.intel.soft.ui.start

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import art.intel.soft.base.activity.AdActivityWithInterstitial
import art.intel.soft.base.event.SingleEvent
import art.intel.soft.ui.main.MainActivity
import art.intel.soft.ui.start.event.StartActivityEvent

class StartActivity : AdActivityWithInterstitial() {

    private val viewModel: StartActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.event.observe(this, this::handleEvent)
    }

    override fun handleEvent(event: SingleEvent) = when (event) {
        is StartActivityEvent.StartLoadInterstitial -> {
            viewModel.stopTimer()
            viewModel.onAdComplete()
        }
        is StartActivityEvent.NavigateToMainActivityEvent -> navigateToMain()
        else -> super.handleEvent(event)
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}
