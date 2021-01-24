package com.example.messagingapp.db.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Chat
    (
    @PrimaryKey(autoGenerate = false)
    var chatID: String,
    val name: String,
    val lastMessageID: String
)