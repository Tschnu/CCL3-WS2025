package com.example.myapplication.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.db.profile.ProfileDao
import com.example.myapplication.db.profile.ProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonalViewModel(
    private val profileDao: ProfileDao
) : ViewModel() {

    private val _profile = MutableStateFlow<ProfileEntity?>(null)
    val profile: StateFlow<ProfileEntity?> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                profileDao.getProfile().collect { profileEntity ->
                    _profile.value = profileEntity
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserName(newName: String) {
        viewModelScope.launch {
            _profile.value?.let { currentProfile ->
                val updatedProfile = currentProfile.copy(name = newName)
                profileDao.updateProfile(updatedProfile)
            }
        }
    }



    fun updateProfile(name: String, periodLength: Int) {
        viewModelScope.launch {
            _profile.value?.let { currentProfile ->
                val updatedProfile = currentProfile.copy(
                    name = name,
                )
                profileDao.updateProfile(updatedProfile)
            }
        }
    }
}