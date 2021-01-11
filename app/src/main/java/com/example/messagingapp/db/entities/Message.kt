package com.example.messagingapp.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Message
    (
    @PrimaryKey(autoGenerate = true)
    val messageid: Int,
    val userID: String,
    val chatID: Int,
    val time: String,
    val date: String,
    val text: String
)