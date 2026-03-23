package art.intel.soft.ui.start

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import art.intel.soft.base.event.SingleEvent
import art.intel.soft.base.event.SingleLiveEvent
import art.intel.soft.extention.baseSubscribeWithoutSubscribeOn
import art.intel.soft.ui.start.event.StartActivityEvent
import art.intel.soft.utils.onNext
import art.intel.soft.utils.timer.ReverseTimerUtil
import art.intel.soft.utils.toImmutable
import io.reactivex.disposables.Disposable

class StartActivityViewModel(application: Application) : AndroidViewModel(application) {

    private companion object {
        const val TIME_WAITING_INIT_AD = 5L
    }

    private val _singleEvent = SingleLiveEvent<SingleEvent>()
    val event = _singleEvent.toImmutable()

    private var timerDisposable: Disposable? = null

    init {
        startTimer()
    }

    private fun startTimer() {
        _singleEvent.onNext(StartActivityEvent.StartLoadInterstitial)

        timerDisposable = ReverseTimerUtil.createReverseTimer(TIME_WAITING_INIT_AD)
                .baseSubscribeWithoutSubscribeOn(
                        onComplete = {
                            stopTimer()
                            sendNavigateEvent()
                        }
                )
    }

    private fun sendNavigateEvent() {
        _singleEvent.onNext(StartActivityEvent.NavigateToMainActivityEvent)
    }

    fun stopTimer() {
        timerDisposable?.dispose()
        timerDisposable = null
    }

    fun onAdComplete() {
        sendNavigateEvent()
    }
}
