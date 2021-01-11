package com.example.messagingapp.ui

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.lifecycle.ViewModel
import com.example.messagingapp.db.Repository
import com.example.messagingapp.db.entities.Chat
import com.example.messagingapp.db.entities.ChatUserCrossRef
import com.example.messagingapp.db.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: Repository
) : ViewModel() {

    fun getAllChatsOfUser(userID: String) = repository.getAllChatsOfUser(userID)

    fun getUserIdByNumber(number: String) : String {
        var id = "x"
        GlobalScope.launch {
            id = repository.getUserIdByNumber(number)
        }
        return id
    }

    fun insertUser(user: User) = GlobalScope.launch {
        repository.insertUser(user)
    }

    fun getCurrentUserNumber(sharedPreferences: SharedPreferences) = sharedPreferences.getString("phoneNum", "x").toString()

    fun insertChatUserCrossRef(chatUserCrossRef: ChatUserCrossRef) = GlobalScope.launch {
        repository.insertChatUserCrossRef(chatUserCrossRef)
    }

    fun insertChat(chat: Chat) = GlobalScope.launch {
        repository.insertChat(chat)
    }

    fun getAllChats() = repository.getAllChats()

    fun getAllMessagesOfChat(chatID: Int) = repository.getAllMessagesOfChat(chatID)

    fun getLastMessageOfChat(chatID: Int) = repository.getLastMessageOfChat(chatID)

}