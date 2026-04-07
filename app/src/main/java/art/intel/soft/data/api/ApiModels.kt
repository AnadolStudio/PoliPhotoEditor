package art.intel.soft.data.api

data class RegisterRequest(val email: String, val password: String, val username: String? = null)
data class LoginRequest(val email: String, val password: String)
data class TokenResponse(val access_token: String, val refresh_token: String, val token_type: String)
data class RefreshRequest(val refresh_token: String)
data class LogoutRequest(val refresh_token: String)
data class UserResponse(val id: Int, val email: String, val username: String?, val role: String, val created_at: String)
data class UpdateUserRequest(val username: String? = null, val email: String? = null)
data class FeatureAccessResponse(val features: Map<String, Boolean>)
data class FilterItem(val id: Int, val name: String, val group: String, val available: Boolean)
data class FiltersResponse(val filters: List<FilterItem>)
data class MessageResponse(val message: String)
