package com.example.messagingapp.data.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.messagingapp.data.room.entities.Chat
import com.example.messagingapp.data.room.entities.Message
import com.example.messagingapp.data.room.entities.User
import com.example.messagingapp.ui.chat.ChatWithLastMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

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

    @Query("SELECT * FROM User WHERE (name LIKE '%' || :searchQuery || '%' OR lastName LIKE '%' || :searchQuery || '%'  OR nickname LIKE '%' || :searchQuery || '%') AND userID != :currentUserID")
    fun searchDBForUsersWithoutYourself(searchQuery: String, currentUserID: String): Flow<List<User>>

    @Delete(entity = User::class)
    suspend fun removeUser(user: User)

    @Query("UPDATE User SET token = :token WHERE userID = :userID")
    suspend fun updateUserToken(token: String, userID: String)

    @Query("SELECT token FROM User WHERE userID = :userID")
    suspend fun getCurrentUserToken(userID: String): String

    @Query("SELECT chatID FROM Chat")
    fun getAllChatIDs(): Flow<List<String>>

    @Query("SELECT c.* FROM Chat AS C JOIN Message AS m ON c.lastMessageID = m.messageid WHERE name LIKE '%' || :searchQuery || '%' ORDER BY m.dateTime DESC")
    fun searchDBForChats(searchQuery: String): Flow<List<Chat>>

    @Query("SELECT * FROM Message WHERE chatID = :chatID ORDER BY dateTime")
    fun getAllMessagesOfChat(chatID: String): Flow<List<Message>>

    @Query("SELECT * FROM Message WHERE messageid = :lastMessageID ORDER BY dateTime")
    suspend fun getMessageByID(lastMessageID: String) : Message

    @Query("SELECT * FROM USER WHERE userID=:userID")
    fun getUserFlowByID(userID: String) : Flow<User>
}