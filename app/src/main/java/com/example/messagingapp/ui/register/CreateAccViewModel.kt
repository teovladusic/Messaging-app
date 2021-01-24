package com.example.messagingapp.ui.register

import android.content.SharedPreferences
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messagingapp.db.firebase.FirebaseRepository
import com.example.messagingapp.db.room.Repository
import com.example.messagingapp.db.room.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CreateAccViewModel @ViewModelInject constructor(
    val repository: Repository,
) : ViewModel() {

    private val firebaseRepository = FirebaseRepository()
    private val usersCollectionRef = FirebaseFirestore.getInstance().collection("users")
    private val TAG = "CreateAcc"

    private fun insertUser(user: User) = viewModelScope.launch {
        repository.insertUser(user)
    }


    suspend fun registerUserInFirestore(user: User): User {
        CoroutineScope(Dispatchers.IO).launch {
            usersCollectionRef.add(user).await()
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
        }.join()
        return user
    }

    suspend fun getToken(sharedPreferences: SharedPreferences): String {
        var token = sharedPreferences.getString("token", "noToken")

        if (token == "noToken") {
            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                token = it
                Log.d(TAG, "$token")
            }.await()
        }
        return token!!
    }

}