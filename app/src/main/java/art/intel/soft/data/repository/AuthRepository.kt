package art.intel.soft.data.repository

import art.intel.soft.data.api.ApiClient
import art.intel.soft.data.api.LoginRequest
import art.intel.soft.data.api.RegisterRequest
import art.intel.soft.data.api.TokenResponse
import art.intel.soft.session.UserSession
import io.reactivex.Single

object AuthRepository {

    fun register(email: String, password: String, username: String? = null): Single<TokenResponse> =
        ApiClient.service.register(RegisterRequest(email, password, username))
            .doOnSuccess { tokens ->
                UserSession.onLoginSuccess(tokens.access_token, tokens.refresh_token)
            }

    fun login(email: String, password: String): Single<TokenResponse> =
        ApiClient.service.login(LoginRequest(email, password))
            .doOnSuccess { tokens ->
                UserSession.onLoginSuccess(tokens.access_token, tokens.refresh_token)
            }
}
