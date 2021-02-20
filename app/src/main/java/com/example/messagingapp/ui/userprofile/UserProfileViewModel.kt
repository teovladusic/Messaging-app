package com.example.messagingapp.ui.userprofile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messagingapp.data.firebase.FirebaseRepository
import com.example.messagingapp.data.room.Repository
import com.example.messagingapp.data.room.entities.User
import com.example.messagingapp.ui.profile.MyProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

class UserProfileViewModel @ViewModelInject constructor(
    private val repository: Repository,
    @Assisted private val state: SavedStateHandle,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val TAG = "UserProfileViewModel"

    val user = state.get<User>("user")!!

    private val userProfileEventsChannel = Channel<UserProfileEvents>()
    val userProfileEvents = userProfileEventsChannel.receiveAsFlow()

    fun onAddUserClicked() = viewModelScope.launch {
        repository.insertUser(user)
        userProfileEventsChannel.send(UserProfileEvents.ShowMessage("User added"))
        downloadImageAndSaveToMobileStorage()
    }

    fun loadImage() {
        setImageFromMobileStorage()
        downloadAndSetImage()
    }

    private fun downloadImageAndSaveToMobileStorage() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val maxDownloadSize = 10L * 1024 * 1024
            val bytes = firebaseRepository.firebaseStorageImagesRef.child(user.userID)
                .getBytes(maxDownloadSize).await()

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            insertBitmapInMobileStorage(bitmap)

        } catch (e: Exception) {
            userProfileEventsChannel.send(UserProfileEvents.ShowMessage("Error downloading profile picture"))
        }
    }

    private fun insertBitmapInMobileStorage(bitmap: Bitmap) {
        val dir = File(Environment.getExternalStorageState() + "/images")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        viewModelScope.launch {
            val filePath = (dir.toString() + File.separator + "" + user.userID)
            userProfileEventsChannel.send(UserProfileEvents.FilePathReady(filePath, "profilePicture.jpg", bitmap))
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

    private fun setImageFromMobileStorage() = viewModelScope.launch(Dispatchers.IO) {
        val fileName = "profilePicture.jpg"
        val dir = File(Environment.getExternalStorageState() + "/images")

        val filePath = (dir.toString() + File.separator + "" + user.userID)
        userProfileEventsChannel.send(UserProfileEvents.LoadImage(filePath, fileName))
    }

    fun onDeleteUserClicked() = viewModelScope.launch(Dispatchers.IO) {
        repository.removeUser(user)
        userProfileEventsChannel.send(UserProfileEvents.ShowMessage("User deleted"))
    }

    private fun downloadAndSetImage() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val maxDownloadSize = 10L * 1024 * 1024
            val bytes = firebaseRepository.firebaseStorageImagesRef.child(user.userID)
                .getBytes(maxDownloadSize).await()

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            userProfileEventsChannel.send(UserProfileEvents.BitmapLoaded(bitmap))

        } catch (e: Exception) {
            userProfileEventsChannel.send(UserProfileEvents.ShowMessage("Error downloading profile picture"))
            userProfileEventsChannel.send(UserProfileEvents.SetDefaultImage)
        }
    }

    fun setImageOnProfilePicFromFile(file: File) = viewModelScope.launch(Dispatchers.IO) {
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            userProfileEventsChannel.send(UserProfileEvents.BitmapLoaded(bitmap))
        } else {
            userProfileEventsChannel.send(UserProfileEvents.SetDefaultImage)
        }
    }

    sealed class UserProfileEvents {
        data class ShowMessage(val message: String) : UserProfileEvents()
        data class BitmapLoaded(val bitmap: Bitmap) : UserProfileEvents()
        object SetDefaultImage : UserProfileEvents()
        data class LoadImage(val filePath: String, val fileName: String) : UserProfileEvents()
        data class FilePathReady(val filePath: String, val fileName: String, val bitmap: Bitmap) : UserProfileEvents()
    }
}