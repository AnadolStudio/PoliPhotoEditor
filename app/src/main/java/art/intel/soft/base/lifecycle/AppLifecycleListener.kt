package art.intel.soft.base.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class AppLifecycleListener(
        private val onAppMoveToForeground: (() -> Unit)? = null,
        private val onAppMoveToBackground: (() -> Unit)? = null
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        onAppMoveToForeground?.invoke()
    }

    override fun onStop(owner: LifecycleOwner) {
        onAppMoveToBackground?.invoke()
    }

}
