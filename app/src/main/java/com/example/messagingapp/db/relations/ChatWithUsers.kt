package com.example.messagingapp.db.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.messagingapp.db.entities.Chat
import com.example.messagingapp.db.entities.ChatUserCrossRef
import com.example.messagingapp.db.entities.User

data class ChatWithUsers(
    @Embedded val chat: Chat,
    @Relation(
        parentColumn = "chatID",
        entityColumn = "userID",
        associateBy = Junction(ChatUserCrossRef::class)
    )
    val users: List<User>
)