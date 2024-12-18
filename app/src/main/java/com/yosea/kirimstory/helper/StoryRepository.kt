package com.yosea.kirimstory.helper

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.yosea.kirimstory.api.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call

class StoryRepository(private val apiService: ApiService) {
    fun getStories(token: String): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                StoryPagingSource(apiService, token)
            }
        ).liveData
    }

    fun uploadStory(
        token: String,
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: RequestBody? ,
        lon: RequestBody?
    ): Call<AddStoryResponse> {
        return apiService.uploadStory(token, description, photo, lat, lon)
    }
}
