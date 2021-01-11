package com.example.messagingapp.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Chat
    (
    @PrimaryKey(autoGenerate = true)
    val chatID: Int,
    val name: String,
    val lastMessageID: Int


    ){
}