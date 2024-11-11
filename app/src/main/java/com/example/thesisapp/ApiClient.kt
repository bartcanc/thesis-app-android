
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient(context: Context) {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("ThesisAppPreferences", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()

            // Pobierz `session-id` z SharedPreferences
            val sessionId = sharedPref.getString("session_id", null)
            Log.d("ApiClient", "Current session-id: $sessionId") // Loguj session-id

            // Dodaj `session-id` do nagłówków, jeśli jest dostępny
            if (sessionId != null) {
                requestBuilder.addHeader("session-id", sessionId)
            }

            chain.proceed(requestBuilder.build())
        }
        .build()

    private val retrofit8000: Retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.1.5:8000")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val retrofit8004: Retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.1.5:8004")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val retrofit3002: Retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.1.5:3002")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getApiService8000(): ApiService {
        return retrofit8000.create(ApiService::class.java)
    }

    fun getApiService8004(): ApiService {
        return retrofit8004.create(ApiService::class.java)
    }

    fun getApiService3002(): ApiService {
        return retrofit3002.create(ApiService::class.java)
    }
}
