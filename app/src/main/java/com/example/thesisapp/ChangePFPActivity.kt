package com.example.thesisapp

import ApiClient
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class ChangePFPActivity : BaseActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnUploadPicture: Button
    private lateinit var imgProfilePicture: ImageView

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pfp)

        apiClient = ApiClient(this)

        btnBack = findViewById(R.id.btnBack)
        btnUploadPicture = findViewById(R.id.btnUploadPicture)
        imgProfilePicture = findViewById(R.id.imgProfilePicture)

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
}
