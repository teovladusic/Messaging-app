package com.example.messagingapp.db.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.messagingapp.db.room.entities.Chat
import com.example.messagingapp.db.room.entities.Message
import com.example.messagingapp.db.room.entities.User


@Database(
    entities = [Chat::class, User::class, Message::class],
    version = 13
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun getDao(): ChatDao

}