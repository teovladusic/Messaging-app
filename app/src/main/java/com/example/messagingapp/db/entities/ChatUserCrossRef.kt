package com.example.messagingapp.db.entities

import androidx.room.Entity

@Entity(primaryKeys = ["chatID", "userID"])
data class ChatUserCrossRef(
    val chatID: Int,
    val userID: String
)