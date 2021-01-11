package com.example.messagingapp.db.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.messagingapp.db.entities.Chat
import com.example.messagingapp.db.entities.ChatUserCrossRef
import com.example.messagingapp.db.entities.User

data class UserWithChats(
    @Embedded val user: User,
    @Relation(
        parentColumn = "userID",
        entityColumn = "chatID",
        associateBy = Junction(ChatUserCrossRef::class)
    )
    val chats: List<Chat>
)