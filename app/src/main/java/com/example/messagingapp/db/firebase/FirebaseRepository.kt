package com.example.messagingapp.db.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val TAG = "FirebaseRepository"

    val usersCollectionReference = FirebaseFirestore.getInstance().collection("users")
    val chatsCollectionReference = FirebaseFirestore.getInstance().collection("chats")
    val messagesCollectionReference = FirebaseFirestore.getInstance().collection("messages")

    suspend fun getUserByNumber(number: String) =
        usersCollectionReference.whereEqualTo("number", number).get().await()

    suspend fun updateUserField(userID: String, field: String, data: String) {
        usersCollectionReference.document(userID).update(field, data).await()
    }


    suspend fun getFirebaseChat(chatID: String) =
        chatsCollectionReference.document(chatID).get().await().toObject(FirebaseChat::class.java)!!
}