package com.example.messagingapp.ui.register

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.messagingapp.db.Repository
import com.example.messagingapp.db.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class CreateAccViewModel(
    val repository: Repository,
) : ViewModel() {

    private val usersCollectionRef = FirebaseFirestore.getInstance().collection("users")
    private val TAG = "CreateAcc"

    private fun insertUser(user: User) = GlobalScope.launch {
        repository.insertUser(user)
    }


    fun registerUserInFirestore(user: User) = CoroutineScope(Dispatchers.IO).launch {
        try {
            usersCollectionRef.add(user).await()
            val userQuery = usersCollectionRef
                .whereEqualTo("number", user.number)
                .get()
                .await()

            if (userQuery.documents.size == 1) {
                try {
                    val document = userQuery.documents[0]
                    user.userID = document.id
                    usersCollectionRef.document(document.id).set(user).await()
                    insertUser(user)

                } catch (e: Exception) {
                    Log.d(TAG, e.message.toString())
                }
            }

        } catch (e: Exception) {

        }

    }

}