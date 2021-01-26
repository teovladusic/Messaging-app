package com.example.messagingapp.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.messagingapp.databinding.ChatItemBinding
import com.example.messagingapp.db.room.entities.Chat
import com.example.messagingapp.db.room.entities.Message
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatItemAdapter(
    private val listener: OnItemClickListener,
    private val chatsFragmentViewModel: ChatsFragmentViewModel
) : ListAdapter<Chat, ChatItemAdapter.ChatItemHolder>(DiffCallback()) {


    private val TAG = "ChatItemAdapter"

    var lastMessages = mutableListOf<Message>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItemHolder {
        val binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatItemHolder, position: Int) {
        val currentChat = getItem(position)
        for (message in lastMessages) {
            if (currentChat.lastMessageID == message.messageid) {
                holder.bind(currentChat, message)
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

    inner class ChatItemHolder(private val binding: ChatItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(chat: Chat, message: Message) {
            binding.apply {
                tvName.text = chat.name
                tvLastMessage.text = message.text
                tvTime.text = formatTime(message.dateTime)

            }
        }

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

    class DiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat) =
            oldItem.chatID == newItem.chatID

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat) =
            oldItem == newItem
    }

}