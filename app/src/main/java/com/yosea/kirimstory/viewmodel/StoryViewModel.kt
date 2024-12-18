package com.yosea.kirimstory.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yosea.kirimstory.api.ListStoryItem
import com.yosea.kirimstory.helper.StoryRepository

class StoryViewModel(private val repository: StoryRepository) : ViewModel() {
    private var currentStoryResult: LiveData<PagingData<ListStoryItem>>? = null

    fun getStories(token: String): LiveData<PagingData<ListStoryItem>> {
        if (currentStoryResult == null) {
            currentStoryResult = repository.getStories(token).cachedIn(viewModelScope)
        }
        return currentStoryResult!!
    }

    fun refreshStories() {
        currentStoryResult = null
    }
} 