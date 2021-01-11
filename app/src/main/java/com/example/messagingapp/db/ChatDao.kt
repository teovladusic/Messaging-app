package com.example.messagingapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Dao
import com.example.messagingapp.db.entities.Chat
import com.example.messagingapp.db.entities.ChatUserCrossRef
import com.example.messagingapp.db.entities.Message
import com.example.messagingapp.db.entities.User
import com.example.messagingapp.db.relations.UserWithChats

@Dao
interface ChatDao {
    @Transaction
    @Query("SELECT * FROM User WHERE userID = :userID")
    fun getAllChatsOfUser(userID: String): LiveData<List<UserWithChats>>

    @Insert(entity = User::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT User.userID FROM User WHERE User.number = :number")
    suspend fun getUserIDByNumber(number: String) : String

    @Insert(entity = Chat::class)
    suspend fun insertChat(chat: Chat)

    @Insert(entity = ChatUserCrossRef::class)
    suspend fun insertChatUserCrossRef(chatUserCrossRef: ChatUserCrossRef)

    @Query("SELECT * FROM Chat")
    fun getAllChats() : LiveData<List<Chat>>

    @Query ("SELECT * FROM Message WHERE chatID = :chatID ORDER BY Message.time")
    fun getAllMessagesOfChat(chatID: Int) : LiveData<List<Message>>

    @Query("SELECT * FROM Message WHERE chatID = :chatID AND MAX(date)")
    fun getLastMessageOfChat(chatID: Int) : LiveData<Message>

}