package com.example.messagingapp.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.messagingapp.entities.Message
import com.example.messagingapp.entities.User

data class UserWithMessges(
    @Embedded val user: User,
    @Relation(
        parentColumn = "userID",
        entityColumn = "userID"
    )
    val messages: List<Message>
)