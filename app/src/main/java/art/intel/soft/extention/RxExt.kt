package art.intel.soft.extention

import art.intel.soft.BuildConfig
import art.intel.soft.base.BaseAction
import art.intel.soft.base.BaseErrorAction
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

fun <T : Any> singleFrom(action: () -> T): Single<T> = Single.create { emitter ->
    try {
        val result = action.invoke()
        emitter.onSuccess(result)
    } catch (ex: Exception) {
        emitter.onError(ex.debugLog())
    }
}

fun <T : Any> singleBy(action: SingleEmitter<T>.() -> Unit): Single<T> = Single.create { emitter ->
    try {
        action.invoke(emitter)
    } catch (ex: Exception) {
        emitter.onError(ex.debugLog())
    }
}

fun <T : Any> Single<T>.baseSubscribe(
        onSuccess: ((T) -> Unit)? = null,
        onError: BaseErrorAction? = null,
        onFinally: BaseAction? = null,
        onSubscribe: BaseAction? = null,
): Disposable = this
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { onSubscribe?.invoke() }
        .subscribe(
                { result ->
                    onSuccess?.invoke(result)
                    onFinally?.invoke()
                },
                { error ->
                    onError?.invoke(error.debugLog())
                    onFinally?.invoke()
                }
        )

fun <T : Any> Single<T>.baseSubscribeWithoutSubscribeOn(
        onSuccess: ((T) -> Unit)? = null,
        onError: BaseErrorAction? = null,
): Disposable = this
        .subscribe(
                { result -> onSuccess?.invoke(result) },
                { error -> onError?.invoke(error.debugLog()) },
        )

fun <T : Any> Observable<T>.baseSubscribe(
        onSuccess: ((T) -> Unit)? = null,
        onError: BaseErrorAction? = null,
        onComplete: BaseAction? = null,
): Disposable = this
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .baseSubscribeWithoutSubscribeOn(
                onSuccess = onSuccess,
                onError = onError,
                onComplete = onComplete,
        )

fun <T : Any> Observable<T>.baseSubscribeWithoutSubscribeOn(
        onSuccess: ((T) -> Unit)? = null,
        onError: BaseErrorAction? = null,
        onComplete: BaseAction? = null,
): Disposable = this
        .subscribe(
                { result -> onSuccess?.invoke(result) },
                { error -> onError?.invoke(error.debugLog()) },
                { onComplete?.invoke() }
        )

private fun Throwable.debugLog(): Throwable {
    if (BuildConfig.DEBUG) printStackTrace()

    return this
}
