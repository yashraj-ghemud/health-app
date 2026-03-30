package com.mindguard.ui.settings.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mindguard.data.model.AppCategory
import com.mindguard.data.model.AppCategoryMapping
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppCategoriesViewModel @Inject constructor() : ViewModel() {
    private val _appCategories = MutableLiveData<List<AppCategoryMapping>>()
    val appCategories: LiveData<List<AppCategoryMapping>> = _appCategories
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun loadAppCategories() {}
    fun updateAppCategory(packageName: String, category: AppCategory) {}
    fun filterCategories(query: String) {}
    fun resetToDefaults() {}
    fun exportCategories() {}
}
