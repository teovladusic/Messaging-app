package com.example.messagingapp.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.example.messagingapp.data.room.entities.User
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val TAG = "FirebaseRepository"

    val usersCollectionReference = FirebaseFirestore.getInstance().collection("users")
    val chatsCollectionReference = FirebaseFirestore.getInstance().collection("chats")

    val firebaseStorageImagesRef = FirebaseStorage.getInstance().reference.child("images/")

    suspend fun getUserByNumber(number: String) =
        usersCollectionReference.whereEqualTo("number", number).get().await()

    suspend fun updateUserField(userID: String, field: String, data: String) {
        usersCollectionReference.document(userID).update(field, data).await()
    }

    suspend fun getFirebaseChat(chatID: String) =
        chatsCollectionReference.document(chatID).get().await().toObject(FirebaseChat::class.java)!!

    suspend fun getFirebaseUser(userID: String) =
        usersCollectionReference.document(userID).get().await().toObject(User::class.java) ?: User()
}