package com.example.messagingapp.ui.contacts

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.messagingapp.db.room.Repository
import com.example.messagingapp.db.room.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ContactsViewModel @ViewModelInject constructor(
    val repository: Repository
) : ViewModel() {

    private val usersCollectionRef = FirebaseFirestore.getInstance().collection("users")

    fun getAllUsers(): LiveData<List<User>> = repository.getAllUsers()

    fun searchDatabase(searchQuery: String): LiveData<List<User>> =
        repository.searchDatabase(searchQuery).asLiveData()

    suspend fun searchFirestoreUsers(query: String): List<User> {
        val usersQuery =
            usersCollectionRef.whereEqualTo("nickname", query.toLowerCase().trim()).get()
                .await()

        return usersQuery.toObjects(User::class.java)
    }


}