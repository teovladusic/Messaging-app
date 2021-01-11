package com.example.messagingapp.db

import com.example.messagingapp.db.entities.Chat
import com.example.messagingapp.db.entities.ChatUserCrossRef
import com.example.messagingapp.db.entities.User

class Repository(
    private val db: ChatDatabase
){
    fun getAllChatsOfUser(userID: String) = db.getDao().getAllChatsOfUser(userID)

    suspend fun insertUser(user: User) = db.getDao().insertUser(user)

    suspend fun getUserIdByNumber(number: String) : String = db.getDao().getUserIDByNumber(number)

    suspend fun insertChat(chat: Chat) = db.getDao().insertChat(chat)

    suspend fun insertChatUserCrossRef(chatUserCrossRef: ChatUserCrossRef) = db.getDao().insertChatUserCrossRef(chatUserCrossRef)

    fun getAllChats() = db.getDao().getAllChats()

    fun getAllMessagesOfChat(chatID: Int) = db.getDao().getAllMessagesOfChat(chatID)

    fun getLastMessageOfChat(chatID: Int) = db.getDao().getLastMessageOfChat(chatID)
}