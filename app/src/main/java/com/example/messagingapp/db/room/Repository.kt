package com.example.messagingapp.db.room

import androidx.lifecycle.LiveData
import com.example.messagingapp.db.room.entities.Chat
import com.example.messagingapp.db.room.entities.Message
import com.example.messagingapp.db.room.entities.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class Repository @Inject constructor(
    private val db: ChatDatabase
) {
    suspend fun insertUser(user: User) = db.getDao().insertUser(user)

    suspend fun insertChat(chat: Chat) = db.getDao().insertChat(chat)

    fun getAllChats() = db.getDao().getAllChats()

    suspend fun getUserByID(userID: String): User = db.getDao().getUserByID(userID)

    suspend fun insertMessage(message: Message) = db.getDao().insertMessage(message)

    suspend fun getMessageByUserAndChatID(userID: String, chatID: String): Message =
        db.getDao().getMessageByUserAndChatID(userID, chatID)

    suspend fun updateMessageChatID(chatID: String, messageID: Int) =
        db.getDao().updateMessageChatID(chatID, messageID)

    fun getAllUsers(): LiveData<List<User>> = db.getDao().getAllUsers()

    fun searchDatabase(searchQuery: String): Flow<List<User>> =
        db.getDao().searchDatbase(searchQuery)

    suspend fun removeUser(user: User) = db.getDao().removeUser(user)

    suspend fun updateUserToken(token: String, userID: String) =
        db.getDao().updateUserToken(token, userID)

    suspend fun getCurrentUserToken(userID: String): String =
        db.getDao().getCurrentUserToken(userID)

    suspend fun deleteAllChats() = db.getDao().deleteAllChats()

    fun getAllChatIDs(): LiveData<List<String>> = db.getDao().getAllChatIDs()

    suspend fun updateLastMessage(lastMessageID: String, chatID: String) =
        db.getDao().updateLastMessage(lastMessageID, chatID)

    suspend fun getMessageByID(messageID: String): Message = db.getDao().getMessageByID(messageID)

    fun searchDBForChats(searchQuery: String): Flow<List<Chat>> =
        db.getDao().searchDBForChats(searchQuery)

}