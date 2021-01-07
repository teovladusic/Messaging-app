package com.example.messagingapp.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Message
    (
    @PrimaryKey(autoGenerate = true)
    val messageid: Int,
    val userID: User,
    val time: String,
    val date: String,
    val text: String,
    val chat: Chat
    ){
}