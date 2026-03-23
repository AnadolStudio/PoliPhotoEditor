package art.intel.soft.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.toImmutable() = this as LiveData<T>

fun <T> MutableLiveData<T>.onNext(t: T) {
    this.value = t
}
