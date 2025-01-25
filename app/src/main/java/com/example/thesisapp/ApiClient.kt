
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class ApiClient(context: Context?) {

    private val sharedPref: SharedPreferences? =
        context?.getSharedPreferences("ThesisAppPreferences", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            val sessionId = sharedPref?.getString("session_id", null)
            Log.d("ApiClient", "Current session-id: $sessionId")
            if (sessionId != null) {
                requestBuilder.addHeader("session-id", sessionId)
            }
            chain.proceed(requestBuilder.build())
        }
        .build()

    private val retrofit8000: Retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.108.97:8000") //adres serwera
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    open fun getApiService8000(): ApiService {
        return retrofit8000.create(ApiService::class.java)
    }
}
