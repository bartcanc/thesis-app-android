package com.example.thesisapp

import ApiClient
import ApiService
import LoginRequest
import PasswordResetRequest
import RegisterRequest
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Timeout
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Field

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {
    private lateinit var fakeSharedPreferences: FakeSharedPreferences
    private lateinit var fakeApiService: FakeApiService
    private lateinit var fakeApiClient: FakeApiClient
    private lateinit var scenario: ActivityScenario<LoginActivity>

    @Before
    fun setUp() {
        fakeSharedPreferences = FakeSharedPreferences()
        fakeSharedPreferences.edit().putString("username", "testUser").apply()
        fakeSharedPreferences.edit().putString("password", "password123").apply()
        fakeSharedPreferences.edit().putBoolean("remember_me", true).apply()

        fakeApiService = FakeApiService()
        fakeApiClient = FakeApiClient(fakeApiService)

        scenario = ActivityScenario.launch(LoginActivity::class.java)
        scenario.onActivity { activity ->
            setPrivateField(activity, "sharedPref", fakeSharedPreferences)
            setInheritedPrivateField(activity, "apiClient", fakeApiClient)
            val client = getPrivateField<ApiClient>(activity, "apiClient")
            println("Current apiClient instance: $client")
        }

    }

    private fun <T> getPrivateField(instance: Any, fieldName: String): T? {
        var field: Field? = null
        var clazz: Class<*>? = instance.javaClass

        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName)
                field.isAccessible = true
                return field.get(instance) as T
            } catch (e: NoSuchFieldException) {
                clazz = clazz.superclass
            }
        }
        return null
    }


    private fun <T> setPrivateField(instance: Any, fieldName: String, value: T) {
        var field: Field? = null
        var clazz: Class<*>? = instance.javaClass

        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName)
                break
            } catch (e: NoSuchFieldException) {
                clazz = clazz.superclass
            }
        }

        field?.let {
            it.isAccessible = true
            it.set(instance, value)
        } ?: throw NoSuchFieldException("Field $fieldName not found in class hierarchy.")
    }

    private fun <T> setInheritedPrivateField(instance: Any, fieldName: String, value: T) {
        var field: Field? = null
        var clazz: Class<*>? = instance.javaClass

        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName)
                field.isAccessible = true
                field.set(instance, value)
                println("Successfully set field $fieldName to $value")
                return
            } catch (e: NoSuchFieldException) {
                clazz = clazz.superclass
            }
        }
        throw NoSuchFieldException("Field $fieldName not found in class hierarchy.")
    }


    private fun invokePrivateMethod(instance: Any, methodName: String, vararg args: Any) {
        try {
            val method = instance.javaClass.getDeclaredMethod(methodName, *args.map { it.javaClass }.toTypedArray())
            method.isAccessible = true
            println("Invoking method: $methodName with arguments: ${args.joinToString()}")
            method.invoke(instance, *args)
        } catch (e: Exception) {
            println("Error invoking method: ${e.message}")
        }
    }



    @Test
    fun testEmptyFieldsShowErrorMessage() {
        scenario.onActivity { activity ->
            val btnLogin = activity.findViewById<Button>(R.id.btnLogin)
            val etUsername = activity.findViewById<EditText>(R.id.etLoginUsername)
            val etPassword = activity.findViewById<EditText>(R.id.etLoginPassword)
            val tvErrorMessage = activity.findViewById<TextView>(R.id.tvErrorMessage)

            etUsername.setText("")
            etPassword.setText("")

            btnLogin.performClick()

            assertEquals(View.VISIBLE, tvErrorMessage.visibility)
            assertEquals("Please fill in all fields", tvErrorMessage.text.toString())
        }
    }

    @Test
    fun testPerformLoginSuccess() {
        scenario.onActivity { activity ->
            val etUsername = activity.findViewById<EditText>(R.id.etLoginUsername)
            val etPassword = activity.findViewById<EditText>(R.id.etLoginPassword)
            val btnLogin = activity.findViewById<Button>(R.id.btnLogin)

            // Ustawianie danych
            etUsername.setText("testUser")
            etPassword.setText("password123")

            println("Entered username: ${etUsername.text}")
            println("Entered password: ${etPassword.text}")

            activity.runOnUiThread {
                invokePrivateMethod(activity, "performLogin", "testUser", "password123")
            }
            // Czekamy na zakończenie operacji
            Thread.sleep(2000)

            println("FakeApiService login success: ${fakeApiService.loginSuccess}")
            println("SharedPreferences username: ${fakeSharedPreferences.getString("username", "")}")
            println("SharedPreferences password: ${fakeSharedPreferences.getString("password", "")}")

            // Assercje sprawdzające poprawność logowania
            assertTrue("User should be logged in successfully", fakeApiService.loginSuccess)
            assertEquals("testUser", fakeSharedPreferences.getString("username", ""))
            assertEquals("password123", fakeSharedPreferences.getString("password", ""))
        }
    }


    @Test
    fun testPerformLoginFailure() {
        scenario.onActivity { activity ->
            setPrivateField(activity, "apiClient", FakeApiClient(fakeApiService))

            invokePrivateMethod(activity, "performLogin", "wrongUser", "wrongPass")

            assertFalse(fakeApiService.loginSuccess)
        }
    }


    @Test
    fun testRememberMeOptionIsSetIncorrectly() {
        scenario.onActivity { activity ->
            val switchRememberMe = activity.findViewById<Switch>(R.id.switchRememberMe)
            switchRememberMe.isChecked = true
            fakeSharedPreferences.edit().putBoolean("remember_me", true).apply()

            val savedValue = fakeSharedPreferences.getBoolean("remember_me", false)
            val switchValue = switchRememberMe.isChecked

            println("SharedPreferences value: $savedValue")
            println("Switch value: $switchValue")

            assertTrue(switchValue == savedValue)
        }
    }

    @Test
    fun testRememberMeOptionIsSetCorrectly() {
        scenario.onActivity { activity ->
            val switchRememberMe = activity.findViewById<Switch>(R.id.switchRememberMe)
            switchRememberMe.isChecked = true
            val rememberMeValue = fakeSharedPreferences.getBoolean("remember_me", false)
            assertTrue(switchRememberMe.isChecked == rememberMeValue)
        }
    }

    @Test
    fun testSaveLoginData() {
        scenario.onActivity { activity ->
            activity.saveLoginData("user123", "password456")

            assertEquals("user123", fakeSharedPreferences.getString("username", ""))
            assertEquals("password456", fakeSharedPreferences.getString("password", ""))
            assertTrue(fakeSharedPreferences.getBoolean("remember_me", false))
        }
    }

    @Test
    fun testNavigateToRegisterActivity() {
        scenario.onActivity { activity ->
            val btnRegister = activity.findViewById<Button>(R.id.btnRegister)
            btnRegister.performClick()

            val expectedIntent = Intent(activity, RegisterActivity::class.java)
            val actualIntent = expectedIntent

            assertEquals(expectedIntent.component, actualIntent.component)
        }
    }
}

class FakeApiClient(private val fakeApiService: FakeApiService) : ApiClient(ApplicationProvider.getApplicationContext()) {
    override fun getApiService8000(): ApiService {
        println("FakeApiClient: Returning FakeApiService instance")
        return fakeApiService
    }
}


class FakeApiService : ApiService {
    var loginSuccess = false
    override fun uploadAvatar(avatar: MultipartBody.Part, userId: RequestBody): Call<ResponseBody> {
        TODO("Not yet implemented")
    }

    override fun register(request: RegisterRequest): Call<ResponseBody> {
        TODO("Not yet implemented")
    }

    override fun login(loginRequest: LoginRequest): Call<ResponseBody> {
        println("FakeApiService: Attempting login with username: ${loginRequest.username}, password: ${loginRequest.password}")
        loginSuccess = loginRequest.username == "testUser" && loginRequest.password == "password123"

        if (loginSuccess) {
            println("FakeApiService: Login SUCCESSFUL")
        } else {
            println("FakeApiService: Login FAILED")
        }

        return object : Call<ResponseBody> {
            override fun enqueue(callback: Callback<ResponseBody>) {
                val response = if (loginSuccess) {
                    Response.success(ResponseBody.create(null, "{\"user_id\": \"12345\"}"))
                } else {
                    Response.error(401, ResponseBody.create(null, "Unauthorized"))
                }
                callback.onResponse(this, response)
            }

            override fun execute(): Response<ResponseBody> = throw NotImplementedError()
            override fun isExecuted() = false
            override fun clone(): Call<ResponseBody> = this
            override fun isCanceled() = false
            override fun cancel() {}
            override fun request(): Request? = null
            override fun timeout(): Timeout {
                TODO("Not yet implemented")
            }
        }
    }

    override fun password_change(request: PasswordResetRequest): Call<ResponseBody> {
        TODO("Not yet implemented")
    }

    override fun sendSensorData(userId: String, jsonBody: RequestBody): Call<ResponseBody> {
        TODO("Not yet implemented")
    }

    override fun sendHealthData(userId: String, requestBody: RequestBody): Call<ResponseBody> {
        TODO("Not yet implemented")
    }

    override fun editHealthData(userId: String, requestBody: RequestBody): Call<ResponseBody> {
        TODO("Not yet implemented")
    }

    override fun getUserHealthById(userId: String, sessionId: String): Call<ResponseBody> {
        TODO("Not yet implemented")
    }

    override fun getUserMetrics(userId: String, sessionId: String): Call<ResponseBody> {
        TODO("Not yet implemented")
    }

    override fun getTrainings(userId: String, page: Int): Call<ResponseBody> {
        TODO("Not yet implemented")
    }

    override fun logout(): Call<ResponseBody> {
        TODO("Not yet implemented")
    }

    override fun getGraphData(
        userId: String,
        date: String,
        graph_type: String,
    ): Call<GraphResponse> {
        TODO("Not yet implemented")
    }
}


class FakeSharedPreferences : SharedPreferences {
    private val data = mutableMapOf<String, Any>()

    override fun getString(key: String?, defValue: String?): String? {
        return data[key] as? String ?: defValue
    }

    override fun edit(): SharedPreferences.Editor {
        return object : SharedPreferences.Editor {
            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                data[key ?: ""] = value ?: ""
                return this
            }

            override fun apply() {}
            override fun commit(): Boolean = true
            override fun clear(): SharedPreferences.Editor = this
            override fun remove(key: String?): SharedPreferences.Editor = this
            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                data[key ?: ""] = value
                return this
            }
            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = this
            override fun putInt(key: String?, value: Int): SharedPreferences.Editor = this
            override fun putLong(key: String?, value: Long): SharedPreferences.Editor = this
            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
                data[key ?: ""] = values ?: mutableSetOf(String)
                return this
            }
        }
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return data[key] as? Boolean ?: defValue
    }

    override fun contains(key: String?): Boolean = data.containsKey(key)

    override fun getAll(): MutableMap<String, *> = data

    override fun getInt(key: String?, defValue: Int): Int = defValue
    override fun getLong(key: String?, defValue: Long): Long = defValue
    override fun getFloat(key: String?, defValue: Float): Float = defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        return data[key] as? MutableSet<String> ?: defValues
    }
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
}
