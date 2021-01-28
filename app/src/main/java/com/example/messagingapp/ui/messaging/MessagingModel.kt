package com.example.messagingapp.ui.messaging

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.messagingapp.db.firebase.FirebaseRepository
import com.example.messagingapp.db.firebase.sendmessages.NotificationData
import com.example.messagingapp.db.firebase.sendmessages.PushNotification
import com.example.messagingapp.db.firebase.sendmessages.RetrofitInstance
import com.example.messagingapp.db.room.Repository
import com.example.messagingapp.db.room.entities.Message
import com.example.messagingapp.db.room.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MessagingModel @ViewModelInject constructor(
    val repository: Repository
) : ViewModel() {

    private val TAG = "MessagingModel"

    private val firebaseRepository = FirebaseRepository()

    fun getAllMessagesOfChat(chatID: String) = repository.getAllMessagesOfChat(chatID).asLiveData()

    suspend fun getFirebaseChat(chatID: String) = firebaseRepository.getFirebaseChat(chatID)

    suspend fun getUsersByIDs(userIDs: List<String>): List<User> {
        val users = mutableListOf<User>()
        for (userID in userIDs) {
            users.add(getUserByID(userID))
        }

        return users
    }

    suspend fun getUserByID(userID: String): User {
        return repository.getUserByID(userID)
    }

    suspend fun sendMessage(
        usersToCreateChatWith: List<User>,
        message: Message,
        currentUserID: String
    ) {
        val notificationData = NotificationData("title", "message")

        CoroutineScope(Dispatchers.IO).launch {
            for (user in usersToCreateChatWith) {
                if (user.userID != currentUserID) {
                    PushNotification(
                        notificationData, user.token
                    ).also {
                        sendNotification(it)
                    }
                }
            }
        }.join()

        firebaseRepository.chatsCollectionReference.document(message.chatID)
            .update("lastMessageID", message.messageid).await()

    }

    fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postMessage(notification)
            if (response.isSuccessful) {
                Log.d(TAG, "$notification")
            } else {
                Log.d(TAG, response.errorBody().toString())
            }

        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }

    fun getDateTime(): String {
        val currentDateTime = LocalDateTime.now()
        val date = currentDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val time = currentDateTime.format(DateTimeFormatter.ISO_LOCAL_TIME)
        return ("$date $time")
    }


    fun createMessage(text: String, currentUserID: String, chatID: String) =
        Message("messageID", currentUserID, chatID, getDateTime(), text, false)


    suspend fun pushMessageToFirestore(message: Message): Message {
        CoroutineScope(Dispatchers.Default).launch {
            firebaseRepository.chatsCollectionReference.document(message.chatID)
                .collection("messages").add(message).await()

            val messageQuery = firebaseRepository.chatsCollectionReference.document(message.chatID)
                .collection("messages")
                .whereEqualTo("chatID", message.chatID)
                .whereEqualTo("messageid", message.messageid).whereEqualTo("userID", message.userID)
                .get().await()

            if (messageQuery.documents.size == 1) {
                val document = messageQuery.documents[0]

                message.messageid = document.id
                firebaseRepository.chatsCollectionReference.document(message.chatID)
                    .collection("messages")
                    .document(document.id).update("messageid", message.messageid).await()
            }
        }.join()

        return message
    }

}