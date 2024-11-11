import com.example.thesisapp.HealthDataRequest
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

data class RegisterRequest(val username: String, val password: String)
data class RegisterResponse(val success: Boolean, val message: String)

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val success: Boolean, val message: String)

data class PasswordResetRequest(val username: String, val password_reset_code: String, val new_password: String)
data class PasswordResetResponse(val success: Boolean, val message: String)

interface ApiService {
    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<ResponseBody>

    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<ResponseBody>

    @PUT("auth/reset_password")
    fun password_change(@Body request: PasswordResetRequest): Call<ResponseBody>

    @POST("sensors/handle_sensor_data")
    fun sendSensorData(@Body jsonBody: RequestBody): Call<ResponseBody>

    @POST("health/users")
    fun sendHealthData(@Body healthDataRequest: HealthDataRequest): Call<ResponseBody>

    @GET("health/users")
    fun getUserHealthById(@Query("userId") userId: String,
                          @Header("session-id") sessionId: String): Call<ResponseBody>

    @POST("auth/logout")
    fun logout(): Call<ResponseBody>
}
