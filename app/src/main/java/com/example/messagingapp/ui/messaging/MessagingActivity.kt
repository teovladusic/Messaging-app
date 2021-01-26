package com.example.messagingapp.ui.messaging

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messagingapp.databinding.ActivityMessagingBinding
import com.example.messagingapp.db.firebase.FirebaseChat
import com.example.messagingapp.db.room.entities.Chat
import com.example.messagingapp.db.room.entities.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MessagingActivity : AppCompatActivity() {

    lateinit var binding: ActivityMessagingBinding
    private val messagingModel: MessagingModel by viewModels()
    private val TAG = "looog"
    var usersInChat = mutableListOf<User>()
    lateinit var chat: Chat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        chat = intent.getParcelableExtra("chat")!!
        var firebaseChat = FirebaseChat()
        var users = listOf<User>()

        binding.tvChatName.text = chat.name

        binding.imgViewBack.setOnClickListener {
            finish()
        }

        val adapter = MessagingAdapter()
        binding.recViewMessages.adapter = adapter
        binding.recViewMessages.layoutManager = LinearLayoutManager(this)


        messagingModel.getAllMessagesOfChat(chat.chatID).observe(this, {
            adapter.messages = it
            adapter.notifyDataSetChanged()
        })

        CoroutineScope(Dispatchers.IO).launch {
            firebaseChat = messagingModel.getFirebaseChat(chat.chatID)
            usersInChat = messagingModel.getUsersByIDs(firebaseChat.userIDs) as MutableList<User>

            withContext(Dispatchers.Main) {
                adapter.users = usersInChat
                adapter.notifyDataSetChanged()
            }
        }

        binding.btnSendMessage.setOnClickListener {
            val text = binding.etMessage.text.toString()
            if (text.isEmpty()) {
                return@setOnClickListener
            }

            val currentUserID = getSharedPreferences(
                "MyPreferences",
                Context.MODE_PRIVATE
            ).getString("currentUserID", "").toString()

            var message = messagingModel.createMessage(text, currentUserID, chat.chatID)

            CoroutineScope(Dispatchers.IO).launch {
                message = messagingModel.pushMessageToFirestore(message)

                messagingModel.sendMessage(users, message, currentUserID)
            }

            binding.etMessage.setText("")

        }

    }
}