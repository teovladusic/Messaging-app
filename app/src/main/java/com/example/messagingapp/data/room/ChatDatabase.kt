package com.example.messagingapp.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.messagingapp.data.room.entities.Chat
import com.example.messagingapp.data.room.entities.Message
import com.example.messagingapp.data.room.entities.User


@Database(
    entities = [Chat::class, User::class, Message::class],
    version = 14
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun getDao(): ChatDao

}