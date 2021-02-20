package com.example.messagingapp.ui.register.createacc

import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messagingapp.data.PreferencesManager
import com.example.messagingapp.data.firebase.FirebaseRepository
import com.example.messagingapp.data.room.Repository
import com.example.messagingapp.data.room.entities.User
import com.example.messagingapp.ui.createchat.CreateChatViewModel
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

class CreateAccountViewModel @ViewModelInject constructor(
    val repository: Repository,
    val preferencesManager: PreferencesManager,
    val firebaseRepository: FirebaseRepository,
    @Assisted private val state: SavedStateHandle,
) : ViewModel() {

    private val TAG = "CreateAccountViewModel"

    val REQUEST_CODE_IMAGE_PICK = 23

    private val phoneNum = state.get<String>("phone")!!

    private var isNicknameAvailable = false

    private val createAccEventsChannel = Channel<CreateAccEvents>()
    val createAccEvents = createAccEventsChannel.receiveAsFlow()

    var imageUri: Uri? = null

    fun registerUser(name: String, lastName: String, nickname: String) {
        if (name.isBlank() || lastName.isBlank() || nickname.isBlank()) {
            viewModelScope.launch {
                createAccEventsChannel.send(CreateAccEvents.ShowMessage("Please enter all fields"))
            }
            return
        }

        if (!isNicknameAvailable) {
            viewModelScope.launch {
                createAccEventsChannel.send(CreateAccEvents.ShowMessage("Nickname is not available"))
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                createAccEventsChannel.send(CreateAccEvents.Registering)
                val token = getToken()
                var user = User("no_id", name, lastName, phoneNum, nickname, token)
                user = registerUserInFirestore(user)

                preferencesManager.updateCurrentUserID(user.userID)
                preferencesManager.updateToken(user.token)
                preferencesManager.updatePhoneNum(user.number)

                prepareImageForInsertAndInsert(user.userID)
                insertImageInStorage(user.userID)

                createAccEventsChannel.send(CreateAccEvents.RegisteredSuccessfully)

            } catch (e: Exception) {
                createAccEventsChannel.send(CreateAccEvents.RegistrationFailed)
            }
        }
    }

    private suspend fun getToken(): String {
        var token = "no_token"

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            token = it
        }.await()

        return token
    }

    private suspend fun insertImageInStorage(userID: String) {
        imageUri?.let {
            try {
                firebaseRepository.firebaseStorageImagesRef.child(userID).putFile(it).await()
            } catch (e: java.lang.Exception) {
                createAccEventsChannel.send(CreateAccEvents.ShowMessage("Error uploading image"))
            }
        }
    }

    private suspend fun prepareImageForInsertAndInsert(userID: String) {
        val fileName = "profilePicture.jpg"
        val dir = File(Environment.getExternalStorageState() + "/images")

        val filePath = (dir.toString() + File.separator + "" + userID)
        createAccEventsChannel.send(CreateAccEvents.FilePathReady(fileName, filePath))
    }

    fun insertPictureToInternalStorage(bitmap: Bitmap, file: File) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
            } catch (e: Exception) {
                Log.d(TAG, "${e.message}")
            }
        }


    fun registerUserInFirestore(user: User): User {
        runBlocking {
            firebaseRepository.usersCollectionReference.add(user).await()
            val userQuery = firebaseRepository.getUserByNumber(user.number)


            if (userQuery.documents.size == 1) {
                try {
                    val document = userQuery.documents[0]
                    user.userID = document.id
                    firebaseRepository.updateUserField(user.userID, "userID", document.id)
                    insertUser(user)

                } catch (e: Exception) {
                    Log.d(TAG, e.message.toString())
                }
            }
        }

        return user
    }

    private fun insertUser(user: User) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertUser(user)
    }

    fun onNicknameChanged(nickname: String) {
        viewModelScope.launch {
            val nicknameQuery =
                firebaseRepository.usersCollectionReference.whereEqualTo("nickname", nickname).get()
                    .await()
            isNicknameAvailable = if (nicknameQuery.documents.size == 0) {
                createAccEventsChannel.send(CreateAccEvents.IsNicknameAvailable(true))
                true
            } else {
                createAccEventsChannel.send(CreateAccEvents.IsNicknameAvailable(false))
                false
            }
        }
    }

    sealed class CreateAccEvents {
        object RegistrationFailed : CreateAccEvents()
        data class ShowMessage(val message: String) : CreateAccEvents()
        object RegisteredSuccessfully : CreateAccEvents()
        data class IsNicknameAvailable(val isAvailable: Boolean) : CreateAccEvents()
        data class FilePathReady(val fileName: String, val filePath: String) : CreateAccEvents()
        object Registering : CreateAccEvents()
    }
}