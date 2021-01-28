package com.example.messagingapp.ui.profile

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.messagingapp.db.room.Repository

class MyProfileViewModel @ViewModelInject constructor(
    val repository: Repository
) : ViewModel() {

    suspend fun getUserByID(userID: String) = repository.getUserByID(userID)

}