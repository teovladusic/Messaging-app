package com.example.messagingapp.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User
    (
    @PrimaryKey(autoGenerate = false)
    var userID: String = "",
    val name: String,
    val lastName: String,
    val number: String,
    val nickname: String
) {

}