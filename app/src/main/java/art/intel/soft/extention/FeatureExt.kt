package art.intel.soft.extention

import androidx.fragment.app.FragmentActivity
import art.intel.soft.session.UserSession
import art.intel.soft.ui.auth.AuthBottomSheet

/**
 * Check if a feature is accessible. If not, show the auth bottom sheet.
 * Calls [onAllowed] only if the user has access (or after successful login).
 */
fun FragmentActivity.requireFeature(featureKey: String, onAllowed: () -> Unit) {
    if (UserSession.canAccess(featureKey)) {
        onAllowed()
    } else {
        val sheet = AuthBottomSheet.newInstance()
        sheet.onAuthSuccess = { onAllowed() }
        sheet.show(supportFragmentManager, AuthBottomSheet.TAG)
    }
}
