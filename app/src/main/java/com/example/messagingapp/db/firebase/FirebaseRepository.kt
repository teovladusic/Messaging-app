package com.example.messagingapp.db.firebase

import com.example.messagingapp.db.room.entities.Message
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val TAG = "FirebaseRepository"

    private val usersCollectionReference = FirebaseFirestore.getInstance().collection("users")
    private val chatsCollectionReference = FirebaseFirestore.getInstance().collection("chats")
    private val messagesCollectionReference = FirebaseFirestore.getInstance().collection("messages")

    suspend fun getUserByNumber(number: String) =
        usersCollectionReference.whereEqualTo("number", number).get().await()

    suspend fun updateUserField(userID: String, field: String, data: String) {
        usersCollectionReference.document(userID).update(field, data).await()
    }

    suspend fun subscribeToChatUpdates(chatID: String): FirebaseChat {
        lateinit var chat: FirebaseChat

        chatsCollectionReference.document(chatID).get().addOnSuccessListener {
            chat = it.toObject(FirebaseChat::class.java)!!
        }.await()

        return chat
    }

    suspend fun getMessageById(messageID: String) =
        messagesCollectionReference.document(messageID).get().await()
            .toObject(Message::class.java)!!

    suspend fun updateLastMessage(message: Message) {
        chatsCollectionReference.document(message.chatID).update("lastMessageID", message.messageid)
    }
}