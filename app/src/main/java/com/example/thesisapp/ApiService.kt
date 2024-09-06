import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

data class RegisterRequest(val username: String, val password: String)
data class RegisterResponse(val success: Boolean, val message: String)

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val success: Boolean, val message: String)

//data class LogoutRequest()
//data class LogoutResponse()

data class PasswordResetRequest(val username: String, val password_reset_code: String, val new_password: String)
data class PasswordResetResponse(val success: Boolean, val message: String)
interface ApiService {
    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<ResponseBody>

    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<ResponseBody>

//    @POST("auth/logout")
//    fun logout(@Body request: LogoutRequest): Call<LogoutResponse>

    @PUT("auth/reset_password")
    fun password_change(@Body request: PasswordResetRequest): Call<ResponseBody>
}
