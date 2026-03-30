package art.intel.soft.session

import art.intel.soft.data.api.ApiClient
import art.intel.soft.data.api.LogoutRequest
import art.intel.soft.data.api.RefreshRequest
import art.intel.soft.data.storage.TokenStorage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

object UserSession {

    // Features available to guest (no auth) — mirror of backend config
    private val GUEST_FEATURES = setOf(
        "filters_basic", "crop_rotate_flip", "improve_basic",
        "text_basic", "stickers_basic", "frames_basic",
        "collage_basic", "forms_basic"
    )

    private var tokenStorage: TokenStorage? = null
    private var featureAccess: Map<String, Boolean> = emptyMap()
    var isAuthenticated: Boolean = false
        private set

    fun init(storage: TokenStorage) {
        tokenStorage = storage
    }

    fun canAccess(feature: String): Boolean {
        // If we have a cached server response, use it
        if (featureAccess.isNotEmpty()) return featureAccess[feature] == true
        // Otherwise fall back to local guest rules
        return if (isAuthenticated) true else feature in GUEST_FEATURES
    }

    fun tryRestoreSession(onSuccess: () -> Unit, onFailure: () -> Unit) {
        val storage = tokenStorage ?: return onFailure()
        if (!storage.hasTokens()) return onFailure()

        ApiClient.service.refresh(RefreshRequest(storage.refreshToken!!))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ tokens ->
                storage.accessToken = tokens.access_token
                storage.refreshToken = tokens.refresh_token
                isAuthenticated = true
                refreshFeatureAccess()
                onSuccess()
            }, {
                storage.clear()
                isAuthenticated = false
                onFailure()
            })
    }

    fun onLoginSuccess(accessToken: String, refreshToken: String) {
        tokenStorage?.accessToken = accessToken
        tokenStorage?.refreshToken = refreshToken
        isAuthenticated = true
        refreshFeatureAccess()
    }

    fun logout() {
        val storage = tokenStorage ?: return
        val refreshToken = storage.refreshToken
        if (refreshToken != null) {
            ApiClient.service.logout(LogoutRequest(refreshToken))
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})
        }
        storage.clear()
        isAuthenticated = false
        featureAccess = emptyMap()
    }

    private fun refreshFeatureAccess() {
        ApiClient.service.getFeatureAccess()
            .subscribeOn(Schedulers.io())
            .subscribe({ response ->
                featureAccess = response.features
            }, {
                // Network error — fall back to local rules based on auth state
            })
    }
}
