package com.example.messagingapp.ui.chat

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.messagingapp.R
import com.example.messagingapp.databinding.ChatItemBinding
import com.example.messagingapp.data.room.entities.Chat
import com.example.messagingapp.data.room.entities.Message
import kotlinx.coroutines.flow.first
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatsAdapter(
    private val listener: OnItemClickListener
) : ListAdapter<ChatWithLastMessage, ChatsAdapter.ChatItemHolder>(DiffCallback()) {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItemHolder {
        context = parent.context
        val binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatItemHolder, position: Int) {
        val currentChat = getItem(position)
        holder.bind(currentChat)
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
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatWithLastMessage: ChatWithLastMessage) {
            binding.apply {
                tvName.text = chatWithLastMessage.chat.name
                tvLastMessage.text = chatWithLastMessage.lastMessage.text
                tvTime.text = formatTime(chatWithLastMessage.lastMessage.dateTime)

                val fileName = "chatImage.jpg"
                val dir = File(Environment.getExternalStorageState() + "/images")
                val filePath = (dir.toString() + File.separator + "" + chatWithLastMessage.chat.chatID)
                val file = File(context.getExternalFilesDir(filePath), fileName)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    imgViewProfilePic.setImageBitmap(bitmap)
                }else {
                    imgViewProfilePic.setImageResource(R.drawable.default_group)
                    Log.d("TAG", "ne postoji")
                }
            }
        }

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val chatWithLastMessage = getItem(position)
                    listener.onItemClick(chatWithLastMessage.chat)
                }
            }
        }

    }

    interface OnItemClickListener {
        fun onItemClick(chat: Chat)
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatWithLastMessage>() {
        override fun areItemsTheSame(oldItem: ChatWithLastMessage, newItem: ChatWithLastMessage) =
            oldItem.chat.chatID == newItem.chat.chatID


        override fun areContentsTheSame(
            oldItem: ChatWithLastMessage,
            newItem: ChatWithLastMessage
        ) =
            oldItem == newItem
    }
}