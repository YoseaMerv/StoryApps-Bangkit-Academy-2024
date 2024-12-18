package com.yosea.kirimstory.api

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody

data class AddStoryRequest(
    @field:SerializedName("description") val description: RequestBody,
    @field:SerializedName("photo") val photo: MultipartBody.Part,
    @field:SerializedName("lat") val lat: RequestBody?,
    @field:SerializedName("lon") val lon: RequestBody?
)
