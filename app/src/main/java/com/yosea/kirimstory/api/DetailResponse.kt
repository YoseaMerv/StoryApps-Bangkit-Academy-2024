package com.yosea.kirimstory.api

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class DetailResponse(
	@field:SerializedName("error")
	val error: Boolean?,

	@field:SerializedName("message")
	val message: String? ,

	@field:SerializedName("story")
	val story: Story?
) : Parcelable

@Parcelize
data class Story(
	@field:SerializedName("id")
	val id: String? ,

	@field:SerializedName("name")
	val name: String? ,

	@field:SerializedName("description")
	val description: String? ,

	@field:SerializedName("photoUrl")
	val photoUrl: String? ,

	@field:SerializedName("createdAt")
	val createdAt: String? ,

	@field:SerializedName("lat")
	val lat: Double? ,

	@field:SerializedName("lon")
	val lon: Double?
) : Parcelable
