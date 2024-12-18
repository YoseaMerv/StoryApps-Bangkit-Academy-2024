package com.yosea.kirimstory.network

import com.yosea.kirimstory.api.LoginRequest
import com.yosea.kirimstory.api.LoginResponse
import retrofit2.http.POST
import retrofit2.http.Body

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
} 