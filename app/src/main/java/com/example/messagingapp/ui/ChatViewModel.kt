package com.example.messagingapp.ui

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.messagingapp.data.PreferencesManager
import com.example.messagingapp.data.firebase.FirebaseChat
import com.example.messagingapp.data.firebase.FirebaseRepository
import com.example.messagingapp.data.room.Repository
import com.example.messagingapp.data.room.entities.Chat
import com.example.messagingapp.data.room.entities.Message
import com.example.messagingapp.data.room.entities.User
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class ChatViewModel @ViewModelInject constructor(
    private val repository: Repository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val TAG = "ChatViewModel"
    private val firebaseRepository = FirebaseRepository()



    val allChatIDs = repository.getAllChatIDs().asLiveData()


    fun getCurrentUserToken(userID: String): String = runBlocking {
        repository.getCurrentUserToken(userID)
    }

    suspend fun insertUser(user: User) = repository.insertUser(user)

    suspend fun removeUser(user: User) = repository.removeUser(user)

    suspend fun updateUserToken(token: String, userID: String) =
        repository.updateUserToken(token, userID)


    suspend fun updateFirestoreUserField(userID: String, field: String, data: String) =
        firebaseRepository.updateUserField(userID, field, data)


    fun insertMessage(message: Message) = viewModelScope.launch {
        repository.insertMessage(message)
    }


    fun subsribeToMessageUpdates(allChatIDs: List<String>) = viewModelScope.launch {
        for (chatID in allChatIDs) {
            getMessageUpdates(chatID)
        }
    }

    fun getMessageUpdates(chatID: String) {
        firebaseRepository.chatsCollectionReference.document(chatID).collection("messages")
            .addSnapshotListener { value, error ->
                error?.let {
                    Log.d(TAG, "$it")
                    return@addSnapshotListener
                }

                value?.let {
                    for (document in value.documents) {
                        val message = document.toObject(Message::class.java)!!
                        if (message.messageid != "messageID") {
                            insertMessage(message)
                        }
                    }
                }
            }
    }

}