package com.example.thesisapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val success: Boolean, val token: String)
data class RegisterRequest(val email: String, val password: String)
data class RegisterResponse(val success: Boolean, val message: String)

interface ApiService {
    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
    @POST("register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>
}
