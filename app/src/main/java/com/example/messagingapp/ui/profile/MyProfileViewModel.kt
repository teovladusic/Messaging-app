package com.example.messagingapp.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.messagingapp.R
import com.example.messagingapp.data.PreferencesManager
import com.example.messagingapp.data.firebase.FirebaseRepository
import com.example.messagingapp.data.room.Repository
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

class MyProfileViewModel @ViewModelInject constructor(
    val repository: Repository,
    private val preferencesManager: PreferencesManager,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val TAG = "MyProfileViewModel"
    val REQUEST_CODE_IMAGE_PICK = 3

    private val myProfileEventsChannel = Channel<MyProfileEvents>()
    val myProfileEvents = myProfileEventsChannel.receiveAsFlow()

    private var isNickAvailable = true

    private val preferencesFlow = preferencesManager.preferencesFlow

    private val userFlow = preferencesFlow.flatMapLatest {
        getUpdatedFirestoreUserAndPicture(it.currentUserID)
        repository.getUserFlowByID(it.currentUserID)
    }

    val user = userFlow.asLiveData()

    private var firebaseStorageImagesRef = firebaseRepository.firebaseStorageImagesRef


    private fun getUpdatedFirestoreUserAndPicture(userID: String) {
        viewModelScope.launch {
            val user = firebaseRepository.getFirebaseUser(userID)
            repository.insertUser(user)

            downloadAndInsertProfilePictureOfUser(userID)
        }
    }


    fun onSaveClicked(nickname: String, name: String, lastName: String) {

        if (nickname.length < 3 || name.length < 3 || lastName.length < 3) {
            viewModelScope.launch {
                myProfileEventsChannel.send(MyProfileEvents.ShowMessage("All fields must have at least 3 letters"))
            }
            return
        }

        if (nickname == user.value!!.nickname) {
            viewModelScope.launch {
                myProfileEventsChannel.send(MyProfileEvents.ShowMessage("Saved successfully"))
            }
            return
        }

        if (!isNickAvailable) {
            viewModelScope.launch {
                myProfileEventsChannel.send(MyProfileEvents.ShowMessage("Nickname is already taken"))
            }
            return
        }

        viewModelScope.launch {
            val oldUser = user.value!!
            val newUser = oldUser.copy(name = name, lastName = lastName, nickname = nickname)
            repository.insertUser(newUser)

            firebaseRepository.usersCollectionReference.document(oldUser.userID).set(newUser)
                .await()

            viewModelScope.launch {
                myProfileEventsChannel.send(MyProfileEvents.ShowMessage("Saved successfully"))
            }
        }
    }

    fun getCurrentUserProfilePicturePath(bitmap: Bitmap) {
        val dir = File(Environment.getExternalStorageState() + "/images")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        viewModelScope.launch {
            preferencesFlow.collectLatest {
                val filePath = (dir.toString() + File.separator + "" + it.currentUserID)
                myProfileEventsChannel.send(
                    MyProfileEvents.FilePathReady(
                        filePath,
                        bitmap,
                        "profilePicture.jpg"
                    )
                )
            }
        }
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
        myProfileEventsChannel.send(MyProfileEvents.BitmapLoaded(bitmap))
    }

    fun loadImage() = viewModelScope.launch {
        val fileName = "profilePicture.jpg"
        val dir = File(Environment.getExternalStorageState() + "/images")

        val preferences = preferencesFlow.first()

        val filePath = (dir.toString() + File.separator + "" + preferences.currentUserID)
        myProfileEventsChannel.send(MyProfileEvents.LoadImage(filePath, fileName))
    }

    fun setImageOnProfilePicFromFile(file: File) = viewModelScope.launch {
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            myProfileEventsChannel.send(MyProfileEvents.BitmapLoaded(bitmap))
        } else {
            myProfileEventsChannel.send(MyProfileEvents.SetImageResource(R.drawable.default_user))
        }
    }

    private suspend fun downloadAndInsertProfilePictureOfUser(userID: String) {
        try {
            val maxDownloadSize = 10L * 1024 * 1024
            val bytes = firebaseStorageImagesRef.child(userID).getBytes(maxDownloadSize).await()

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            getCurrentUserProfilePicturePath(bitmap)

        } catch (e: Exception) {
            Log.d(TAG, "${e.message}")
        }

    }

    fun onPicImageClicked() = viewModelScope.launch {
        myProfileEventsChannel.send(MyProfileEvents.StartGalleryIntent)
    }


    fun onPictureSelected(uri: Uri) = viewModelScope.launch {
        try {
            firebaseStorageImagesRef.child(user.value!!.userID).putFile(uri).await()
            //TODO: show uploading image bar

        } catch (e: Exception) {
            myProfileEventsChannel.send(MyProfileEvents.ShowMessage("Error uploading picture"))
        }
    }

    fun onNickChanged(nickname: String) {
        viewModelScope.launch {
            val nicknameQuery =
                firebaseRepository.usersCollectionReference.whereEqualTo("nickname", nickname).get()
                    .await()
            isNickAvailable = if (nicknameQuery.documents.size == 0) {
                myProfileEventsChannel.send(MyProfileEvents.NicknameAvailable)
                true
            } else {
                if (nickname == user.value!!.nickname) {
                    myProfileEventsChannel.send(MyProfileEvents.NicknameAvailable)
                    true
                } else {
                    myProfileEventsChannel.send(MyProfileEvents.NicknameUnavailable)
                    false
                }

            }
        }
    }

    sealed class MyProfileEvents {
        object NicknameAvailable : MyProfileEvents()
        object NicknameUnavailable : MyProfileEvents()
        data class ShowMessage(val message: String) : MyProfileEvents()
        object StartGalleryIntent : MyProfileEvents()
        data class FilePathReady(val filePath: String, val bitmap: Bitmap, val fileName: String) :
            MyProfileEvents()

        data class LoadImage(val filePath: String, val fileName: String) : MyProfileEvents()
        data class BitmapLoaded(val bitmap: Bitmap) : MyProfileEvents()
        data class SetImageResource(val resource: Int) : MyProfileEvents()
    }
}