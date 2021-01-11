package com.example.messagingapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.messagingapp.db.entities.Chat
import com.example.messagingapp.db.entities.ChatUserCrossRef
import com.example.messagingapp.db.entities.Message
import com.example.messagingapp.db.entities.User


@Database(
    entities = [Chat::class, User::class, Message::class, ChatUserCrossRef::class],
    version = 6
)
abstract class ChatDatabase: RoomDatabase() {
    abstract fun getDao() : ChatDao

    companion object{
        @Volatile
        private var instance: ChatDatabase? = null
        private var LOCK = Any()

        operator fun invoke(context: Context) = instance
            ?: synchronized(LOCK) {
                instance
                    ?: createDatabase(
                        context
                    ).also { instance = it }
            }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                ChatDatabase::class.java, "ChatDB.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}