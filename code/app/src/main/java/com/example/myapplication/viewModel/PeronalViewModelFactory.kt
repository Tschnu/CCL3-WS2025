package com.example.myapplication.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.db.profile.ProfileDao

/*class PersonalViewModelFactory(
    private val profileDao: ProfileDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PersonalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PersonalViewModel(profileDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}*/