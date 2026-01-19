package com.example.myapplication.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.db.profile.ProfileDatabase
import com.example.myapplication.db.profile.ProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ProfileDatabase.getDatabase(application).profileDao()

    private val _profile = MutableStateFlow(ProfileEntity())   // ✅ never null in UI
    val profile: StateFlow<ProfileEntity> = _profile

    init {
        viewModelScope.launch {
            dao.getProfile().collect { p ->
                if (p == null) {
                    // ✅ create default row once
                    dao.insertProfile(ProfileEntity())
                    _profile.value = ProfileEntity()
                } else {
                    _profile.value = p
                }
            }
        }
    }

    fun setName(name: String) {
        val current = _profile.value
        viewModelScope.launch {
            dao.insertProfile(current.copy(name = name.trim().ifBlank { "there" }))
        }
    }

    /** flowerIndex = 0..4 */
    fun setFlowerPicture(flowerIndex: Int) {
        val current = _profile.value
        val safeIndex = flowerIndex.coerceIn(0, 4)
        viewModelScope.launch {
            dao.insertProfile(current.copy(flowerPicture = safeIndex))
        }
    }

    fun setCycleLength(days: Int) {
        val current = _profile.value
        val safeDays = days.coerceIn(15, 60)
        viewModelScope.launch {
            dao.insertProfile(current.copy(cycleLength = safeDays))
        }
    }

    fun setPeriodLength(days: Int) {
        val current = _profile.value
        val safeDays = days.coerceIn(1, 15)
        viewModelScope.launch {
            dao.insertProfile(current.copy(periodLength = safeDays))
        }
    }
}
