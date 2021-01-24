package com.example.messagingapp.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messagingapp.databinding.ChatItemBinding
import com.example.messagingapp.db.room.entities.Chat
import com.example.messagingapp.db.room.entities.Message
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatItemAdapter(
    private val listener: OnItemClickListener,
) : RecyclerView.Adapter<ChatItemAdapter.ChatItemHolder>() {


    private val TAG = "ChatItemAdapter"

    var chats = mutableListOf<Chat>()
    var lastMessages = mutableListOf<Message>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItemHolder {
        val binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatItemHolder(binding)
    }

    override fun getItemCount() = chats.size

    override fun onBindViewHolder(holder: ChatItemHolder, position: Int) {
        val chat = chats[position]
        for (message in lastMessages) {
            if (message.messageid == chat.lastMessageID) {
                holder.binding.tvName.text = chat.name
                holder.binding.tvLastMessage.text = message.text
                holder.binding.tvTime.text = formatTime(message.dateTime)
            }
        }


    }

    fun formatTime(dateTime: String): String {
        //dateTime format -> 2021-01-22 14:04:45:923
        val currentDateTime = LocalDateTime.now()
        val date = currentDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)

        return if (dateTime.substring(0, 10) == date) {
            dateTime.substring(11, 16)
        } else {
            "${dateTime.substring(8, 10)}.${dateTime.substring(5, 7)}.${dateTime.substring(0, 4)}"
        }
    }

    inner class ChatItemHolder(val binding: ChatItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onItemClick(adapterPosition)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

}