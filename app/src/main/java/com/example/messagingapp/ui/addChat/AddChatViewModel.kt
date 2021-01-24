package com.example.messagingapp.ui.addChat

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.messagingapp.db.firebase.FirebaseChat
import com.example.messagingapp.db.firebase.sendmessages.NotificationData
import com.example.messagingapp.db.firebase.sendmessages.PushNotification
import com.example.messagingapp.db.firebase.sendmessages.RetrofitInstance
import com.example.messagingapp.db.room.Repository
import com.example.messagingapp.db.room.entities.Message
import com.example.messagingapp.db.room.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AddChatViewModel @ViewModelInject constructor(
    private val repository: Repository
) : ViewModel() {

    private val TAG = "AddChatViewModel"
    private val chatsCollectionRef = FirebaseFirestore.getInstance().collection("chats")
    private val usersCollectionRef = FirebaseFirestore.getInstance().collection("users")

    fun getAllUsers(): LiveData<List<User>> = repository.getAllUsers()

    fun getDateTime(): String {
        val currentDateTime = LocalDateTime.now()
        val date = currentDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val time = currentDateTime.format(DateTimeFormatter.ISO_LOCAL_TIME)
        return ("$date $time")
    }

    suspend fun getUserByID(userID: String) = repository.getUserByID(userID)

    suspend fun pushMessageToFirestore(message: Message): Message {
        CoroutineScope(Dispatchers.Default).launch {
            chatsCollectionRef.document(message.chatID).collection("messages").add(message).await()

            val messageQuery = chatsCollectionRef.document(message.chatID).collection("messages")
                .whereEqualTo("chatID", message.chatID)
                .whereEqualTo("messageid", message.messageid).whereEqualTo("userID", message.userID)
                .get().await()

            if (messageQuery.documents.size == 1) {
                val document = messageQuery.documents[0]

                message.messageid = document.id
                chatsCollectionRef.document(message.chatID).collection("messages")
                    .document(document.id).update("messageid", message.messageid).await()
            }
        }.join()

        return message
    }

    suspend fun getFirestoreUserById(userID: String): User {
        lateinit var user: User
        CoroutineScope(Dispatchers.IO).launch {
            val documentSnapshot = usersCollectionRef.document(userID).get().await()
            user = documentSnapshot.toObject(User::class.java)!!
        }.join()

        return user
    }

    fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postMessage(notification)
            if (response.isSuccessful) {

            } else {
                Log.d(TAG, response.errorBody().toString())
            }

        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }


    suspend fun createChat(userIDs: List<String>, name: String, currentUserID: String) =
        CoroutineScope(Dispatchers.IO).launch {
            var message = Message(
                "messageID",
                currentUserID,
                "chatID",
                getDateTime(),
                "The chat was created by ${getUserByID(currentUserID).nickname}",
                false
            )

            val firebaseChat = FirebaseChat(name, "chatID", userIDs, message.messageid)
            chatsCollectionRef.add(firebaseChat).await()

            val chatsQuery = chatsCollectionRef
                .whereEqualTo("chatID", "chatID")
                .whereArrayContainsAny("userIDs", userIDs)
                .get()
                .await()

            if (chatsQuery.size() == 1) {
                try {
                    val document = chatsQuery.documents[0]
                    firebaseChat.chatID = document.id

                    message.chatID = firebaseChat.chatID
                    pushMessageToFirestore(message)

                    firebaseChat.lastMessageID = message.messageid

                    chatsCollectionRef.document(firebaseChat.chatID).set(firebaseChat).await()

                    val usersToCreateChatWith = mutableListOf<User>()

                    for (userId in userIDs) {
                        val user = getFirestoreUserById(userId)
                        Log.d(TAG, "$user")
                        usersToCreateChatWith.add(user)
                    }

                    val notificationData = NotificationData(message.userID, message.text)

                    val sendNotifications = CoroutineScope(Dispatchers.IO).launch {
                        for (user in usersToCreateChatWith) {
                            if (user.userID != currentUserID) {
                                PushNotification(
                                    notificationData, user.token
                                ).also {
                                    sendNotification(it)
                                }
                            }
                        }
                    }

                    sendNotifications.join()


                } catch (e: Exception) {
                    Log.d(TAG, e.message.toString())
                }
            }
        }

}