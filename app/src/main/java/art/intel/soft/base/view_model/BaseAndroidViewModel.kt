package art.intel.soft.base.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import art.intel.soft.App
import art.intel.soft.base.event.SingleEvent
import art.intel.soft.base.event.SingleLiveEvent
import art.intel.soft.utils.toImmutable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseAndroidViewModel<State : Any>(application: Application) : AndroidViewModel(application) {

    val app: Application by lazy { getApplication<App>() }

    protected val _singleEvent = SingleLiveEvent<SingleEvent>()
    val event = _singleEvent.toImmutable()

    protected val _state = MutableLiveData<State>()
    val state = _state.toImmutable()

    private val compositeDisposable by lazy { CompositeDisposable() }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    protected fun Disposable.disposeOnViewModelDestroy(): Disposable {
        compositeDisposable.add(this)
        return this
    }
}
