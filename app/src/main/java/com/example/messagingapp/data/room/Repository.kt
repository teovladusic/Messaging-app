package com.example.messagingapp.data.room

import com.example.messagingapp.data.room.entities.Chat
import com.example.messagingapp.data.room.entities.Message
import com.example.messagingapp.data.room.entities.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class Repository @Inject constructor(
    private val db: ChatDatabase
) {
    suspend fun insertUser(user: User) = db.getDao().insertUser(user)

    suspend fun insertChat(chat: Chat) = db.getDao().insertChat(chat)

    suspend fun getUserByID(userID: String) = db.getDao().getUserByID(userID)

    suspend fun insertMessage(message: Message) = db.getDao().insertMessage(message)

    fun searchDBForUsersWithoutYourself(searchQuery: String, currentUserID: String) = db.getDao().searchDBForUsersWithoutYourself(searchQuery, currentUserID)

    suspend fun removeUser(user: User) = db.getDao().removeUser(user)

    suspend fun updateUserToken(token: String, userID: String) =
        db.getDao().updateUserToken(token, userID)

    suspend fun getCurrentUserToken(userID: String): String =
        db.getDao().getCurrentUserToken(userID)

    fun getAllChatIDs() = db.getDao().getAllChatIDs()

    fun searchDBForChats(searchQuery: String): Flow<List<Chat>> =
        db.getDao().searchDBForChats(searchQuery)

    fun getAllMessagesOfChat(chatID: String) =
        db.getDao().getAllMessagesOfChat(chatID)

    suspend fun getMessageByID(lastMessageID: String) = db.getDao().getMessageByID(lastMessageID)

    fun getUserFlowByID(userID: String) = db.getDao().getUserFlowByID(userID)

}