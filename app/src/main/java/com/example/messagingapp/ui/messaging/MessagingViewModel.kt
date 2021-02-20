package com.example.messagingapp.ui.messaging

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.messagingapp.data.PreferencesManager
import com.example.messagingapp.data.firebase.FirebaseRepository
import com.example.messagingapp.data.firebase.sendmessages.NotificationData
import com.example.messagingapp.data.firebase.sendmessages.PushNotification
import com.example.messagingapp.data.firebase.sendmessages.RetrofitInstance
import com.example.messagingapp.data.room.Repository
import com.example.messagingapp.data.room.entities.Chat
import com.example.messagingapp.data.room.entities.Message
import com.example.messagingapp.data.room.entities.User
import com.example.messagingapp.ui.userprofile.UserProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MessagingViewModel @ViewModelInject constructor(
    private val repository: Repository,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val TAG = "MessagingModel"

    val chat = state.get<Chat>("chat")!!

    val messages = repository.getAllMessagesOfChat(chat.chatID).asLiveData()

    private val messagingEventsChannel = Channel<MessagingEvent>()
    val messagingEvent = messagingEventsChannel.receiveAsFlow()

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>>
        get() {
            getFirebaseChat()
            return _users
        }

    val userPreferences = preferencesManager.preferencesFlow.asLiveData()


    fun getFirebaseChat() = viewModelScope.launch {
        val firebaseChat = firebaseRepository.getFirebaseChat(chat.chatID)
        getUsersByIDs(firebaseChat.userIDs)
    }

    fun getUsersByIDs(userIDs: List<String>) = viewModelScope.launch {
        val usersToCreateChat = mutableListOf<User>()
        for (userID in userIDs) {
            var user = getUserByID(userID)
            if (user == null) {
                user = firebaseRepository.getFirebaseUser(userID)
                usersToCreateChat.add(user)
            } else {
                usersToCreateChat.add(user)
            }
        }
        _users.value = usersToCreateChat
    }

    fun onBackClicked() = viewModelScope.launch {
        messagingEventsChannel.send(MessagingEvent.NavigateToChatsScreen)
    }

    fun loadImage() = viewModelScope.launch(Dispatchers.IO){
        //donwload and insert image from firebasestorage
        downloadImageFromFirebaseStorage()
    }

    private suspend fun downloadImageFromFirebaseStorage() {
        try {
            val maxDownloadSize = 10L * 1024 * 1024
            val bytes = firebaseRepository.firebaseStorageImagesRef.child(chat.chatID).getBytes(maxDownloadSize).await()

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            insertBitmapInMobileStorage(bitmap)

        } catch (e: Exception) {
            Log.d(TAG, "${e.message}")
        }
    }

    private fun insertBitmapInMobileStorage(bitmap: Bitmap) {
        val dir = File(Environment.getExternalStorageState() + "/images")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        viewModelScope.launch {
            val filePath = (dir.toString() + File.separator + "" + chat.chatID)
            messagingEventsChannel.send(MessagingEvent.FilePathReady(filePath, "chatImage.jpg", bitmap))
        }
    }

    fun insertBitmapInStorage(file: File, bitmap: Bitmap) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            Log.d(TAG, "${e.message}")
        }
    }

    fun onSendClicked(text: String) {
        if (text.isBlank()) {
            return
        }

        val currentUserID = userPreferences.value?.currentUserID ?: ""

        if (currentUserID == "") {
            return
        }

        val message = createMessage(text, currentUserID, chat.chatID)
        val usersToCreateChatWith = users.value ?: listOf()

        if (usersToCreateChatWith.isEmpty()) {
            return
        }

        viewModelScope.launch {
            val notificationData = createNotificationData(chat.name, message.text)
            sendNotification(usersToCreateChatWith, notificationData, currentUserID)
            pushMessageToFirestore(message)
            updateLastMessageInFirestore(message)

        }

    }




    suspend fun getUserByID(userID: String): User {
        return repository.getUserByID(userID)
    }

    suspend fun sendNotification(
        usersToCreateChatWith: List<User>,
        notificationData: NotificationData,
        currentUserID: String
    ) {
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

    suspend fun updateLastMessageInFirestore(message: Message) {
        firebaseRepository.chatsCollectionReference.document(message.chatID)
            .update("lastMessageID", message.messageid).await()
    }

    suspend fun sendNotification(notification: PushNotification){
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

    fun createNotificationData(chatName: String, messageText: String) =
        NotificationData(title = chatName, message = messageText)


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

    sealed class MessagingEvent {
        object NavigateToChatsScreen : MessagingEvent()
        data class FilePathReady(val filePath: String, val fileName: String, val bitmap: Bitmap) : MessagingEvent()
    }
}