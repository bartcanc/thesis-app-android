package com.example.thesisapp

import ApiClient
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ChangePFPActivity : BaseActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnUploadPicture: Button
    private lateinit var imgProfilePicture: ImageView

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pfp)

        val rootLayout = findViewById<LinearLayout>(R.id.frameLayout)

        sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)
        val selectedTheme = sharedPref.getString("theme", "sea")

        if (selectedTheme == "post") {
            rootLayout.setBackgroundResource(R.drawable.gradient_post_modern)
        } else {
            rootLayout.setBackgroundResource(R.drawable.gradient_sea_breeze)
        }

        apiClient = ApiClient(this)

        btnBack = findViewById(R.id.btnBack)
        btnUploadPicture = findViewById(R.id.btnUploadPicture)
        imgProfilePicture = findViewById(R.id.imgProfilePicture)

        val sharedPref = getSharedPreferences("ThesisAppPreferences", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", "")
        val sessionId = sharedPref.getString("session_id", "")

        if (!userId.isNullOrEmpty() && !sessionId.isNullOrEmpty()) {
            fetchUserAvatar(userId, sessionId)
        }


        btnBack.setOnClickListener {
            finish()
        }

        btnUploadPicture.setOnClickListener {
            openFileChooser()
        }
    }
    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            imgProfilePicture.setImageURI(selectedImageUri)
            selectedImageUri?.let { uploadImage(it) }
        }
    }

    private fun uploadImage(imageUri: Uri) {
        val sharedPref = getSharedPreferences("ThesisAppPreferences", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", null)
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "Błąd: Brak User ID!", Toast.LENGTH_SHORT).show()
            return
        }

        val file = uriToFile(imageUri) ?: return

        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("avatar", file.name, requestFile)
        val userIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), userId)

        val apiService = apiClient.getApiService8000()

        apiService.uploadAvatar(body, userIdPart).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ChangePFPActivity, "Avatar przesłany!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ChangePFPActivity, "Błąd przesyłania!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ChangePFPActivity, "Błąd sieci!", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun uriToFile(uri: Uri): File? {
        val file = File(cacheDir, "temp_avatar.jpg")
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun fetchUserAvatar(userId: String?, sessionId: String?) {
        val apiClient = ApiClient(this)
        val apiService = apiClient.getApiService8000()

        if (userId != null && sessionId != null) {
            apiService.getUserAvatar(userId, sessionId).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    Log.d("fetchUserAvatar", "Full response: ${response.raw()}")
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            try {
                                val responseData = responseBody.string()
                                Log.d("fetchUserAvatar", "Response body: $responseData")
                                val jsonResponse = JSONObject(responseData)
                                var avatarUrl = jsonResponse.optString("avatar_link", "")

                                if (avatarUrl.isNotEmpty()) {
                                    if (avatarUrl.contains("localhost")) {
                                        avatarUrl = avatarUrl.replace("localhost", "192.168.108.97")
                                        Log.d("fetchUserAvatar", "Updated Avatar URL: $avatarUrl")
                                    }
                                    downloadAndDisplayImage(avatarUrl)
                                } else {
                                    Log.e("fetchUserAvatar", "Avatar URL is empty")
                                }
                            } catch (e: Exception) {
                                Log.e("fetchUserAvatar", "Error parsing response: ${e.message}")
                            }
                        } ?: Log.e("fetchUserAvatar", "Empty response body")
                    } else {
                        Log.e("fetchUserAvatar", "Error response: ${response.errorBody()?.string()}")
                        Toast.makeText(this@ChangePFPActivity, "Failed to fetch avatar", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("fetchUserAvatar", "Request failed: ${t.message}")
                    Toast.makeText(this@ChangePFPActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun downloadAndDisplayImage(imageUrl: String) {
        Thread {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    runOnUiThread {
                        findViewById<ImageView>(R.id.imgProfilePicture).setImageBitmap(bitmap)
                    }
                } else {
                    Log.e("downloadAndDisplayImage", "Failed to download image: ${connection.responseMessage}")
                }
            } catch (e: Exception) {
                Log.e("downloadAndDisplayImage", "Error downloading image: ${e.message}")
            }
        }.start()
    }
}
