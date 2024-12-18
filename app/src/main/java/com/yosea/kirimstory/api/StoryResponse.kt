package com.yosea.kirimstory.api

import com.google.gson.annotations.SerializedName

data class StoryResponse(

	@field:SerializedName("listStory")
	val listStory: List<ListStoryItem?>? ,

	@field:SerializedName("error")
	val error: Boolean? ,

	@field:SerializedName("message")
	val message: String?
)

data class ListStoryItem(

	@field:SerializedName("photoUrl")
	val photoUrl: String?,

	@field:SerializedName("createdAt")
	val createdAt: String?,

	@field:SerializedName("name")
	val name: String?,

	@field:SerializedName("description")
	val description: String? ,

	@field:SerializedName("lon")
	val lon: Any? ,

	@field:SerializedName("id")
	val id: String? ,

	@field:SerializedName("lat")
	val lat: Any?
) {
	val latitude: Double?
		get() = (lat as? Number)?.toDouble()

	val longitude: Double?
		get() = (lon as? Number)?.toDouble()
}
