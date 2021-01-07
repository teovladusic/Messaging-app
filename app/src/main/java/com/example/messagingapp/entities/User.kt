package com.example.messagingapp.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User
    (
    @PrimaryKey
    val userID: Int,
    val name: String,
    val lastName: String,
    val number: String
    ){
}