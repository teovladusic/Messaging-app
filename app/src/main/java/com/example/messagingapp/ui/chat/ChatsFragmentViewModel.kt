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

    fun searchDBForChats(searchQuery: String) =
        repository.searchDBForChats(searchQuery).asLiveData()

    fun getAllChats() = repository.getAllChats()

    fun getAllChatsIDs() = repository.getAllChatIDs()

    suspend fun insertMessage(message: Message) = repository.insertMessage(message)

    suspend fun subscribeToChatUpdates(userID: String) {
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
                            viewModelScope.launch {
                                insertChat(chat)
                            }
                        }
                    }
                }
            }
    }

    suspend fun insertChat(chat: Chat) = repository.insertChat(chat)

    suspend fun subsribeToMessageUpdates(allChatIDs: List<String>) {
        Log.d(TAG, "$allChatIDs")
        for (chatID in allChatIDs) {
            getMessageUpdates(chatID)
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
                        viewModelScope.launch {
                            insertMessage(message)
                        }

                    }
                }
            }
    }

    suspend fun getMessageByID(messageID: String) = repository.getMessageByID(messageID)


}