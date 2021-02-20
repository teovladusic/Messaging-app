package com.example.messagingapp.ui.chat

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.messagingapp.data.PreferencesManager
import com.example.messagingapp.data.firebase.FirebaseChat
import com.example.messagingapp.data.firebase.FirebaseRepository
import com.example.messagingapp.data.room.Repository
import com.example.messagingapp.data.room.entities.Chat
import com.example.messagingapp.data.room.entities.Message
import com.example.messagingapp.ui.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatsFragmentViewModel @ViewModelInject constructor(
    private val repository: Repository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val firebaseRepository = FirebaseRepository()
    private val TAG = "ChatsFragmentViewModel"

    //chats
    val searchQuery = MutableStateFlow("")
    private val chatsFlow = searchQuery.flatMapLatest {
        repository.searchDBForChats(it)
    }
    val chats = chatsFlow.asLiveData()

    //events
    private val chatsEventChannel = Channel<ChatsEvent>()
    val chatsEvent = chatsEventChannel.receiveAsFlow()

    //preferences
    private val preferencesFlow = preferencesManager.preferencesFlow

    //is user logged in
    private val isLoggedInFlow = preferencesFlow.flatMapLatest {
        subscribeToChatUpdates(it.currentUserID)
        if (it.phoneNum == "no_phoneNum" || it.currentUserID == "no_user" || it.token == "no_token") {
            flowOf(false)
        }else{
            flowOf(true)
        }
    }

    val isLoggedIn = isLoggedInFlow.asLiveData()

    fun onLoggedIn() = viewModelScope.launch {
        chatsEventChannel.send(ChatsEvent.LoggedIn)
    }

    fun onNotLoggedIn() = viewModelScope.launch{
        chatsEventChannel.send(ChatsEvent.NotLoggedIn)
    }


    //when chat changes get last messages and return list of chatWithLastMessages objects
    suspend fun getChatsWithLastMessages(chats: List<Chat>) : List<ChatWithLastMessage> {
        val chatsWithLastMessages = mutableListOf<ChatWithLastMessage>()
        viewModelScope.launch(Dispatchers.IO) {
            for (chat in chats) {
                val message = getMessageByID(chat.lastMessageID)
                chatsWithLastMessages.add(ChatWithLastMessage(message, chat))
            }
        }.join()
        return chatsWithLastMessages
    }

    fun onAddChatClicked() = viewModelScope.launch {
        chatsEventChannel.send(ChatsEvent.NavigateToAddChatFragment)
    }

    suspend fun getMessageByID(lastMessageID: String) =
        repository.getMessageByID(lastMessageID)



    fun onChatClicked(chat: Chat) = viewModelScope.launch {
        chatsEventChannel.send(ChatsEvent.NavigateToMessagingScreen(chat))
    }

    fun subscribeToChatUpdates(currentUserID: String) {
        viewModelScope.launch {
            firebaseRepository.chatsCollectionReference.whereArrayContains("userIDs", currentUserID)
                .addSnapshotListener { value, error ->
                    error?.let {
                        Log.d(TAG, error.message.toString())
                        return@addSnapshotListener
                    }

                    value?.let {
                        for (document in it) {
                            val firebaseChat = document.toObject(FirebaseChat::class.java)
                            if (firebaseChat.chatID != "chatID") {
                                val chat =
                                    Chat(
                                        firebaseChat.chatID,
                                        firebaseChat.name,
                                        firebaseChat.lastMessageID
                                    )
                                insertChat(chat)
                            }
                        }
                    }
                }
        }
    }

    fun insertChat(chat: Chat) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertChat(chat)
    }

    sealed class ChatsEvent {
        data class NavigateToMessagingScreen(val chat: Chat) : ChatsEvent()
        object NotLoggedIn : ChatsEvent()
        object LoggedIn : ChatsEvent()
        object NavigateToAddChatFragment : ChatsEvent()
    }
}

data class ChatWithLastMessage(
    val lastMessage: Message,
    val chat: Chat
)

