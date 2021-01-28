package com.example.messagingapp.ui.chat

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.messagingapp.db.firebase.FirebaseChat
import com.example.messagingapp.db.room.Repository
import com.example.messagingapp.db.room.entities.Chat
import com.example.messagingapp.db.room.entities.Message
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ChatsFragmentViewModel @ViewModelInject constructor(
    val repository: Repository
) : ViewModel() {

    private val TAG = "ChatsFragmentViewModel"

    val chatsCollectionRef = FirebaseFirestore.getInstance().collection("chats")

    val chats = repository.getAllChats().asLiveData()

    val lastMessages = repository.getLastMessages().asLiveData()

    val allChatIDs = repository.getAllChatIDs().asLiveData()

    fun searchDBForChats(searchQuery: String) =
        repository.searchDBForChats(searchQuery).asLiveData()

    fun insertMessage(message: Message) = viewModelScope.launch {
        repository.insertMessage(message)
    }

    fun subscribeToChatUpdates(userID: String) {
        viewModelScope.launch {
            chatsCollectionRef.whereArrayContains("userIDs", userID)
                .addSnapshotListener { value, error ->
                    error?.let {
                        Log.d(TAG, "${error.message}")
                        return@addSnapshotListener
                    }

                    value?.let {
                        for (document in it) {

                            val firebaseChat = document.toObject(FirebaseChat::class.java)
                            if (firebaseChat.chatID != "chatID") {
                                val chat =
                                    Chat(
                                        firebaseChat.chatID,
                                        firebaseChat.name,
                                        firebaseChat.lastMessageID
                                    )
                                insertChat(chat)

                            }
                        }
                    }
                }
        }
    }

    fun insertChat(chat: Chat) = viewModelScope.launch {
        repository.insertChat(chat)
    }

    fun subsribeToMessageUpdates(allChatIDs: List<String>) {
        viewModelScope.launch {
            for (chatID in allChatIDs) {
                getMessageUpdates(chatID)
            }
        }
    }

    suspend fun getMessageUpdates(chatID: String) {
        chatsCollectionRef.document(chatID).collection("messages")
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

    fun updateChatLastMessage(messageID: String, chatID: String) = viewModelScope.launch {
        repository.updateChatLastMessage(messageID, chatID)
    }


    suspend fun getMessageByID(messageID: String) = repository.getMessageByID(messageID)

    fun getAllMessages() = repository.getAllMessages()

    suspend fun updateChat(chat: Chat) = repository.updateChat(chat)

    suspend fun getChatByID(chatID: String) = repository.getChatByID(chatID)
}
