package com.example.messagingapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.messagingapp.entities.Chat
import com.example.messagingapp.entities.Message
import com.example.messagingapp.entities.User

@Dao
interface Dao {

    @Insert
    suspend fun insertChat(chat: Chat)

    @Insert
    suspend fun insertUser(user: User)

    @Insert
    suspend fun insertMessage(message: Message)

    @Delete
    suspend fun deleteChat(chat: Chat)

    @Delete
    suspend fun deleteUser(user: User)

    @Delete
    suspend fun deleteMessage(message: Message)

}