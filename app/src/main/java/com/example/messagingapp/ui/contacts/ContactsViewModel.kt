package com.example.messagingapp.ui.contacts

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.messagingapp.data.PreferencesManager
import com.example.messagingapp.data.UserPreferences
import com.example.messagingapp.data.room.Repository
import com.example.messagingapp.data.room.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ContactsViewModel @ViewModelInject constructor(
    val repository: Repository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val usersCollectionRef = FirebaseFirestore.getInstance().collection("users")

    private val contactEventsChannel = Channel<ContactEvents>()
    val contactsEvents = contactEventsChannel.receiveAsFlow()

    private val preferencesFlow = preferencesManager.preferencesFlow

    val searchQuery = MutableStateFlow("")

    private val usersFlow = combine(
        searchQuery,
        preferencesFlow
    ) { query, preferences ->
        Pair(query, preferences.currentUserID)
    }.flatMapLatest { (query, currentUserID) ->
        repository.searchDBForUsersWithoutYourself(query, currentUserID)
    }

    var users = usersFlow.asLiveData() as MutableLiveData

    fun areUsersEmpty(users: List<User>) {
        if (users.isEmpty()) {
            viewModelScope.launch {
                contactEventsChannel.send(ContactEvents.ShowNoContactsMessage)
            }
        }else {
            viewModelScope.launch {
                contactEventsChannel.send(ContactEvents.HideNoContactsMessage)
            }
        }
    }


    fun onSearchFirestoreUsers(query: String) = viewModelScope.launch {
        val usersQuery =
            usersCollectionRef.whereEqualTo("nickname", query.toLowerCase().trim()).get()
                .await()

        val firestoreUsers = usersQuery.toObjects(User::class.java)
        users.value = firestoreUsers
    }

    fun onUserClick(user: User) = viewModelScope.launch {
        contactEventsChannel.send(ContactEvents.UserClicked(user))
    }

    sealed class ContactEvents {
        data class UserClicked(val user: User) : ContactEvents()
        object ShowNoContactsMessage : ContactEvents()
        object HideNoContactsMessage : ContactEvents()
    }
}