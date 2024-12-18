package com.yosea.kirimstory.network

import javax.inject.Inject
import com.yosea.kirimstory.api.LoginRequest
import com.yosea.kirimstory.api.LoginResponse
import com.yosea.kirimstory.utils.EspressoIdlingResource

class NetworkService @Inject constructor(private val apiService: ApiService) {
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        EspressoIdlingResource.increment()
        return try {
            val response = apiService.login(LoginRequest(email, password))
            EspressoIdlingResource.decrement()
            Result.success(response)
        } catch (e: Exception) {
            EspressoIdlingResource.decrement()
            Result.failure(e)
        }
    }

    suspend fun logout() {
        EspressoIdlingResource.increment()
        try {
            EspressoIdlingResource.decrement()
        } catch (e: Exception) {
            EspressoIdlingResource.decrement()
            throw e
        }
    }
} 