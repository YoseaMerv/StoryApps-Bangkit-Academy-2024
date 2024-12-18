package com.yosea.kirimstory.api

import com.google.gson.annotations.SerializedName

data class MapsResponse(

    @field:SerializedName("error")
    val error: Boolean?,

    @field:SerializedName("message")
    val message: String?
)
