package com.yosea.kirimstory.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RegisterResponse(

	@field:SerializedName("error")
	val error: Boolean? ,

	@field:SerializedName("message")
	val message: String?
) : Parcelable
