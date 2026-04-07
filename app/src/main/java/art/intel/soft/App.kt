package art.intel.soft

import android.app.Application
import art.intel.soft.data.api.ApiClient
import art.intel.soft.data.storage.TokenStorage
import art.intel.soft.session.UserSession
import com.google.firebase.FirebaseApp

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        val tokenStorage = TokenStorage(this)
        ApiClient.init(tokenStorage)
        UserSession.init(tokenStorage)
        UserSession.tryRestoreSession(onSuccess = {}, onFailure = {})
    }
}
