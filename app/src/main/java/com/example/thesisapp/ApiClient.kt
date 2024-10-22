
import android.content.Context
import android.content.SharedPreferences
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient(context: Context) {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("ThesisAppPreferences", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()

            val sessionId = sharedPref.getString("session_id", null)
            if (sessionId != null) {
                requestBuilder.addHeader("session_id", sessionId)
            }

            chain.proceed(requestBuilder.build())
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://25.68.49.21:8000/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getApiService(): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
