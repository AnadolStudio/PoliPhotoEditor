package art.intel.soft.base.activity

import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import art.intel.soft.R
import art.intel.soft.base.OnBackPressed
import art.intel.soft.base.OnBackPressedContainer
import art.intel.soft.base.event.SingleEvent
import art.intel.soft.ui.dialogs.imlemetation.LoadingDialog

abstract class BaseActivity : AppCompatActivity(), OnBackPressedContainer {

    private companion object {
        const val LOADING_DIALOG_TAG = "LoadingDialog"
    }

    open val darkStatusBarIcon: Boolean = true

    protected val backPressedInnerListeners = LinkedHashSet<OnBackPressed>()

    override fun onResume() {
        super.onResume()
        if (darkStatusBarIcon) {
            setDarkStatusBarIcon()
        }
    }

    fun showLoadingDialog() {
        LoadingDialog().show(supportFragmentManager, LOADING_DIALOG_TAG)
    }

    fun hideLoadingDialog() {
        (supportFragmentManager.findFragmentByTag(LOADING_DIALOG_TAG) as? LoadingDialog)
                ?.dismiss()
    }

    protected fun showToast(@StringRes stringId: Int) = showToast(getString(stringId))

    protected fun showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

    protected fun setDarkStatusBarIcon() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorWhite);
        val currentFlags = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = currentFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    protected fun addBackPressedListener(listener: OnBackPressed) {
        backPressedInnerListeners.add(listener)
    }

    protected fun removeBackPressedListener(listener: OnBackPressed) {
        backPressedInnerListeners.remove(listener)
    }

    override fun onBackPressed(listener: OnBackPressed) = Unit

    protected open fun handleEvent(event: SingleEvent) = Unit
}

