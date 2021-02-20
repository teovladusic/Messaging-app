package com.example.messagingapp.ui.createchat

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getBitmap
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.messagingapp.data.PreferencesManager
import com.example.messagingapp.data.firebase.FirebaseChat
import com.example.messagingapp.data.firebase.FirebaseRepository
import com.example.messagingapp.data.firebase.sendmessages.NotificationData
import com.example.messagingapp.data.firebase.sendmessages.PushNotification
import com.example.messagingapp.data.firebase.sendmessages.RetrofitInstance
import com.example.messagingapp.data.room.Repository
import com.example.messagingapp.data.room.entities.Chat
import com.example.messagingapp.data.room.entities.Message
import com.example.messagingapp.data.room.entities.User
import com.example.messagingapp.ui.profile.MyProfileViewModel
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

class CreateChatViewModel @ViewModelInject constructor(
    private val repository: Repository,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val TAG = "CreateChatViewModel"

    val REQUEST_CODE_IMAGE_PICK = 23

    private val createChatEventsChannel = Channel<CreateChatEvents>()
    val createChatEvents = createChatEventsChannel.receiveAsFlow()

    private val usersToCreateChat = state.get<Array<User>>("usersToCreateChat")?.toMutableList()

    val usersFlow = MutableStateFlow(usersToCreateChat)

    private val preferencesFlow = preferencesManager.preferencesFlow

    var imageUri: Uri? = null

    fun onSwiped(position: Int) = viewModelScope.launch {
        usersFlow.value?.let { users ->
            users.removeAt(position)
            if (users.size == 0) {
                createChatEventsChannel.send(CreateChatEvents.NavigateBack(null))
            }
        }
    }

    fun onImageClick() = viewModelScope.launch{
       createChatEventsChannel.send(CreateChatEvents.PickChatImage)
    }

    fun onBackClicked() = viewModelScope.launch {
        createChatEventsChannel.send(CreateChatEvents.NavigateBack(usersFlow.value))
    }

    fun onCreateChatClick(name: String) = viewModelScope.launch {

        if (name.isBlank()) {
            createChatEventsChannel.send(CreateChatEvents.ShowMessage("Name of the chat cannot be empty"))
            return@launch
        }

        createChatEventsChannel.send(CreateChatEvents.Loading)

        val preferences = preferencesFlow.first()

        var message = Message(
            "messageID",
            preferences.currentUserID,
            "chatID",
            getDateTime(),
            "The chat was created by ${getUserNicknameByID(preferences.currentUserID)}",
            false
        )

        val userIDs = mutableListOf<String>()

        for (user in usersToCreateChat ?: mutableListOf()) {
            userIDs.add(user.userID)
        }

        userIDs.add(preferences.currentUserID)

        val firebaseChat = FirebaseChat(name, "chatID", userIDs, message.messageid)
        firebaseRepository.chatsCollectionReference.add(firebaseChat).await()

        val chatsQuery = firebaseRepository.chatsCollectionReference
            .whereEqualTo("chatID", "chatID")
            .whereArrayContainsAny("userIDs", userIDs)
            .whereEqualTo("name", name)
            .get()
            .await()

        if (chatsQuery.size() == 1) {
            val document = chatsQuery.documents[0]

            firebaseChat.chatID = document.id
            message.chatID = firebaseChat.chatID

            message = pushMessageToFirestore(message)

            firebaseChat.lastMessageID = message.messageid

            firebaseRepository.chatsCollectionReference
                .document(firebaseChat.chatID).set(firebaseChat)
                .await()

            val notificationData = NotificationData(firebaseChat.name, message.text)

            //notifications
            for (user in usersToCreateChat ?: mutableListOf()) {
                if (user.userID != preferences.currentUserID) {
                    PushNotification(
                        notificationData, user.token
                    ).also {
                        sendNotification(it)
                    }
                }
            }

            val chat = Chat(firebaseChat.chatID, firebaseChat.name, firebaseChat.lastMessageID)
            repository.insertChat(chat)

            insertImageInStorage(chat.chatID)
            prepareImageForInsertAndInsert(chat.chatID)

            createChatEventsChannel.send(CreateChatEvents.ChatCreated(chat))
        }
    }

    private suspend fun insertImageInStorage(chatID: String) {
        imageUri?.let {
            try {
                Log.d(TAG, "uri nije null")
                firebaseRepository.firebaseStorageImagesRef.child(chatID).putFile(it).await()
            }catch (e: java.lang.Exception) {
                createChatEventsChannel.send(CreateChatEvents.ShowMessage("Error uploading image"))
            }
        }
    }

    private suspend fun prepareImageForInsertAndInsert(chatID: String) {
        val fileName = "chatImage.jpg"
        val dir = File(Environment.getExternalStorageState() + "/images")

        val filePath = (dir.toString() + File.separator + "" + chatID)
        createChatEventsChannel.send(CreateChatEvents.FilePathReady(filePath, fileName))
    }

    fun insertPictureToInternalStorage(bitmap: Bitmap, file: File) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            Log.d(TAG, "${e.message}")
        }
    }


    private suspend fun sendNotification(notification: PushNotification) {
        try {
            val response = RetrofitInstance.api.postMessage(notification)
            if (response.isSuccessful) {
                Log.d(TAG, "response successful")
            } else {
                Log.d(TAG, response.errorBody().toString())
            }

        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }

    private suspend fun pushMessageToFirestore(message: Message): Message {
        firebaseRepository.chatsCollectionReference
            .document(message.chatID).collection("messages").add(message).await()

        val messageQuery = firebaseRepository.chatsCollectionReference.document(message.chatID)
            .collection("messages")
            .whereEqualTo("chatID", message.chatID)
            .whereEqualTo("messageid", message.messageid)
            .whereEqualTo("userID", message.userID)
            .get()
            .await()

        if (messageQuery.documents.size == 1) {
            val document = messageQuery.documents[0]

            message.messageid = document.id
            firebaseRepository.chatsCollectionReference
                .document(message.chatID)
                .collection("messages")
                .document(message.messageid)
                .update("messageid", message.messageid)
                .await()
        }

        return message
    }


    private suspend fun getUserNicknameByID(userID: String) =
        repository.getUserByID(userID).nickname


    private fun getDateTime(): String {
        val currentDateTime = LocalDateTime.now()
        val date = currentDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val time = currentDateTime.format(DateTimeFormatter.ISO_LOCAL_TIME)
        return ("$date $time")
    }

    sealed class CreateChatEvents {
        data class NavigateBack(val users: MutableList<User>?) : CreateChatEvents()
        data class ShowMessage(val message: String) : CreateChatEvents()
        data class ChatCreated(val chat: Chat) : CreateChatEvents()
        object Loading : CreateChatEvents()
        object PickChatImage : CreateChatEvents()
        data class FilePathReady(val filePath: String, val fileName: String) : CreateChatEvents()
    }
}