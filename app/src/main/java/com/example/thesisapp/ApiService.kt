import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(val username: String, val email: String, val password: String)
data class RegisterResponse(val success: Boolean, val message: String)

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val success: Boolean, val message: String)

interface ApiService {
    @POST("user/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("user/login")
    fun login(@Body loginRequest: LoginRequest): Call<ResponseBody>
}
