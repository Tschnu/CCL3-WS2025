package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.db.profile.ProfileDatabase
import com.example.myapplication.db.profile.ProfileDao
import com.example.myapplication.ui.viewModel.PersonalViewModel
//import com.example.myapplication.ui.viewModel.PersonalViewModelFactory

@Composable
fun PersonalProfilePage(
    //database: ProfileDatabase
    viewModel: PersonalViewModel
) {
    /*val profileDao = database.profileDao()
    val viewModel: PersonalViewModel = viewModel(
        factory = PersonalViewModelFactory(profileDao)
    )*/

    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            profile?.let { currentProfile ->
                Text(text = "Current Name: ${currentProfile.name}")

                var nameInput by remember { mutableStateOf(currentProfile.name) }

                TextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Name") }
                )

                Button(
                    onClick = { viewModel.updateUserName(nameInput) }
                ) {
                    Text("Save Name")
                }
            }
        }
    }
}