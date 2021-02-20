package com.example.messagingapp.ui.addChat

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.messagingapp.data.PreferencesManager
import com.example.messagingapp.data.firebase.FirebaseChat
import com.example.messagingapp.data.firebase.sendmessages.NotificationData
import com.example.messagingapp.data.firebase.sendmessages.PushNotification
import com.example.messagingapp.data.firebase.sendmessages.RetrofitInstance
import com.example.messagingapp.data.room.Repository
import com.example.messagingapp.data.room.entities.Message
import com.example.messagingapp.data.room.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AddChatViewModel @ViewModelInject constructor(
    private val repository: Repository,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    private val TAG = "AddChatViewModel"

    private val addChatEventsChannel = Channel<AddChatEvents>()
    val addChatEvents = addChatEventsChannel.receiveAsFlow()

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

    val users = usersFlow.asLiveData()

    private val stateUsers = state.get<Array<User>>("usersToCreateChat")?.toMutableList() ?: mutableListOf()
    val usersToCreateChatWith = MutableStateFlow(stateUsers)


    fun onUserClick(user: User) {
        if (usersToCreateChatWith.value.contains(user)) {
            removeUser(user)
        }else {
            addUser(user)
        }
    }

    private fun addUser(user: User) {
        stateUsers.add(user)
        viewModelScope.launch {
            addChatEventsChannel.send(AddChatEvents.NotifyUsersToCreateChatAdapter)
            addChatEventsChannel.send(AddChatEvents.UserSelected)
        }
    }

    private fun removeUser(user: User) {
        stateUsers.remove(user)
        viewModelScope.launch {
            addChatEventsChannel.send(AddChatEvents.NotifyUsersToCreateChatAdapter)
            if(usersToCreateChatWith.value.size == 0) {
                addChatEventsChannel.send(AddChatEvents.NoUsersSelected)
            }
        }
    }

    fun setBtnColor() = viewModelScope.launch {
        if (usersToCreateChatWith.value.isEmpty()) {
            addChatEventsChannel.send(AddChatEvents.NoUsersSelected)
        }else {
            addChatEventsChannel.send(AddChatEvents.UserSelected)
        }
    }

    fun onBackClicked() = viewModelScope.launch {
        addChatEventsChannel.send(AddChatEvents.NavigateToMessagingFragment)
    }

    fun onCreateChatClick() = viewModelScope.launch {
        if (usersToCreateChatWith.value.size < 1) {
            addChatEventsChannel.send(AddChatEvents.ShowMessage("You have to specify at least 1 user"))
            return@launch
        }

        addChatEventsChannel.send(AddChatEvents.CreateChat(usersToCreateChatWith.value))
    }


    sealed class AddChatEvents {
        object NotifyUsersToCreateChatAdapter : AddChatEvents()
        object NoUsersSelected : AddChatEvents()
        object UserSelected : AddChatEvents()
        data class ShowMessage(val message: String) : AddChatEvents()
        data class CreateChat(val users: List<User>) : AddChatEvents()
        object NavigateToMessagingFragment : AddChatEvents()
    }
}