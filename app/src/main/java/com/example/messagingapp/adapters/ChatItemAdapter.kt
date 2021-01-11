package com.example.messagingapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messagingapp.ui.ChatViewModel
import com.example.messagingapp.databinding.ChatItemBinding
import com.example.messagingapp.db.entities.Chat
import com.example.messagingapp.db.entities.Message

class ChatItemAdapter(
    var chats: List<Chat>,
    private val chatViewModel: ChatViewModel,
    private val lastMessage: Message
) : RecyclerView.Adapter<ChatItemAdapter.ChatItemHolder>() {

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItemHolder {
        val binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return ChatItemHolder(binding)

    }

    override fun getItemCount() = chats.size

    override fun onBindViewHolder(holder: ChatItemHolder, position: Int) {
        val chat = chats[position]
        holder.binding.tvName.text = chat.name
        //TODO: DODAJ SLIKU NA CHAT LIST

        //TODO: SLJEDECE NAPRAVI DA SLUSAS NA PORUKE U FRAGMENTU I SAMO UPDATAJ REC VIEW PO CHATID KOJI DOBIJES SA MESSEGON

        holder.binding.tvLastMessage.text = lastMessage.text
        holder.binding.tvTime.text = lastMessage.time

    }

    class ChatItemHolder(val binding: ChatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}