package com.example.messagingapp.db.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Message
    (
    @PrimaryKey(autoGenerate = false)
    var messageid: String,
    val userID: String,
    var chatID: String,
    val dateTime: String,
    val text: String,
    var read: Boolean
) {
    constructor() : this("", "", "", "", "", false)
}