package com.example.messagingapp.ui.chat

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.messagingapp.db.firebase.FirebaseRepository
import com.example.messagingapp.db.room.Repository
import com.example.messagingapp.db.room.entities.User

class ChatViewModel @ViewModelInject constructor(
    private val repository: Repository
) : ViewModel() {

    private val TAG = "ChatViewModel"
    private val firebaseRepository = FirebaseRepository()

    suspend fun insertUser(user: User) = repository.insertUser(user)

    suspend fun removeUser(user: User) = repository.removeUser(user)

    suspend fun updateUserToken(token: String, userID: String) =
        repository.updateUserToken(token, userID)

    suspend fun getCurrentUserToken(userID: String) = repository.getCurrentUserToken(userID)

    suspend fun updateFirestoreUserField(userID: String, field: String, data: String) =
        firebaseRepository.updateUserField(userID, field, data)

}