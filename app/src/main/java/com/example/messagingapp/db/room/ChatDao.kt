package com.example.messagingapp.db.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.messagingapp.db.room.entities.Chat
import com.example.messagingapp.db.room.entities.Message
import com.example.messagingapp.db.room.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Insert(entity = User::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(entity = Chat::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: Chat)

    @Query("SELECT c.* FROM Chat AS c JOIN Message AS m ON c.lastMessageID = m.messageid ORDER BY m.dateTime DESC")
    fun getAllChats(): Flow<List<Chat>>

    @Query("SELECT * FROM User WHERE User.userID = :userID")
    suspend fun getUserByID(userID: String): User

    @Insert(entity = Message::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("SELECT * FROM MESSAGE WHERE Message.userID = :userID AND Message.chatID = :chatID")
    suspend fun getMessageByUserAndChatID(userID: String, chatID: String): Message

    @Query("UPDATE Message SET chatID = :chatID WHERE messageid = :messageID")
    suspend fun updateMessageChatID(chatID: String, messageID: Int)

    @Query("SELECT * FROM USER")
    fun getAllUsers(): LiveData<List<User>>

    @Query("SELECT * FROM User WHERE name LIKE :searchQuery OR lastName LIKE :searchQuery OR nickname LIKE :searchQuery")
    fun searchDatbase(searchQuery: String): Flow<List<User>>

    @Delete(entity = User::class)
    suspend fun removeUser(user: User)

    @Query("UPDATE User SET token = :token WHERE userID = :userID")
    suspend fun updateUserToken(token: String, userID: String)

    @Query("SELECT token FROM User WHERE userID = :userID")
    suspend fun getCurrentUserToken(userID: String): String

    @Query("SELECT chatID FROM Chat")
    fun getAllChatIDs(): Flow<List<String>>

    @Query("SELECT * FROM Message WHERE messageid = :messageID")
    suspend fun getMessageByID(messageID: String): Message

    @Query("SELECT c.* FROM Chat AS C JOIN Message AS m ON c.lastMessageID = m.messageid WHERE name LIKE :searchQuery ORDER BY m.dateTime DESC")
    fun searchDBForChats(searchQuery: String): Flow<List<Chat>>

    @Query("SELECT * FROM Message WHERE chatID = :chatID ORDER BY dateTime")
    fun getAllMessagesOfChat(chatID: String): Flow<List<Message>>

    @Query("UPDATE Chat SET lastMessageID = :messageID WHERE chatID = :chatID")
    suspend fun updateChatLastMessage(messageID: String, chatID: String)

    @Query("SELECT * FROM Message ORDER BY dateTime")
    fun getAllMessages(): LiveData<List<Message>>

    @Update(entity = Chat::class)
    suspend fun updateChat(chat: Chat)

    @Query("SELECT * FROM CHAT WHERE chatID = :chatID")
    suspend fun getChatByID(chatID: String): Chat

    @Query("SELECT * FROM Message AS m JOIN Chat AS c ON m.messageid = c.lastMessageID ORDER BY m.dateTime")
    fun getLastMessages(): Flow<List<Message>>


}