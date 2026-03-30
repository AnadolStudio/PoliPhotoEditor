package art.intel.soft.data.api

import io.reactivex.Single
import retrofit2.http.*

interface ApiService {
    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Single<TokenResponse>

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Single<TokenResponse>

    @POST("auth/refresh")
    fun refresh(@Body request: RefreshRequest): Single<TokenResponse>

    @POST("auth/logout")
    fun logout(@Body request: LogoutRequest): Single<MessageResponse>

    @GET("user/me")
    fun getMe(): Single<UserResponse>

    @PUT("user/me")
    fun updateMe(@Body request: UpdateUserRequest): Single<UserResponse>

    @GET("features/access")
    fun getFeatureAccess(): Single<FeatureAccessResponse>

    @GET("features/filters")
    fun getFilters(): Single<FiltersResponse>
}
