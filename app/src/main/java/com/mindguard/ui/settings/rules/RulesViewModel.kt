package com.mindguard.ui.settings.rules

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mindguard.data.model.UsageRule
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RulesViewModel @Inject constructor() : ViewModel() {
    private val _rules = MutableLiveData<List<UsageRule>>()
    val rules: LiveData<List<UsageRule>> = _rules
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun loadRules() {}
    fun toggleRule(id: String, enabled: Boolean) {}
    fun deleteRule(id: String) {}
    fun resetToDefaults() {}
}
