import com.example.thesisapp.GraphResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {
    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<ResponseBody>

    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<ResponseBody>

    @PUT("auth/reset_password")
    fun password_change(@Body request: PasswordResetRequest): Call<ResponseBody>

    @POST("sensors/handle_sensor_data")
    fun sendSensorData(@Query("workoutType") userId: String,
                       @Body jsonBody: RequestBody): Call<ResponseBody>

    @POST("health/users")
    fun sendHealthData(@Query("userId") userId: String,
                       @Body requestBody: RequestBody): Call<ResponseBody>

    @PUT("health/users")
    fun editHealthData(@Query("userId") userId: String,
                       @Body requestBody: RequestBody): Call<ResponseBody>

    @GET("health/users/")
    fun getUserHealthById(@Query("userId") userId: String,
                          @Header("session-id") sessionId: String): Call<ResponseBody>

    @GET("health/users/")
    fun getUserMetrics(@Query("userId") userId: String,
                       @Header("session-id") sessionId: String): Call<ResponseBody>

    @GET("health/workouts")
    fun getTrainings(@Query("userId") userId: String,
                     @Query("page") page: Int): Call<ResponseBody>

    @POST("auth/logout")
    fun logout(): Call<ResponseBody>

    @GET("sensors/get_graph")
    fun getGraphData(@Query("user_id") userId: String,
                     @Query("date") date: String,
                     @Query("graph_type") graph_type: String): Call<GraphResponse>
}

data class RegisterRequest(
    val username: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class PasswordResetRequest(
    val username: String,
    val password_reset_code: String,
    val new_password: String
)
