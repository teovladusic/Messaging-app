package com.example.messagingapp.ui.messaging

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.messagingapp.databinding.MessageItemBinding
import com.example.messagingapp.data.room.entities.Message
import com.example.messagingapp.data.room.entities.User
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MessagingAdapter :
    ListAdapter<Message, MessagingAdapter.MessagingViewHolder>(DiffCallback()) {

    private val TAG = "MessagingAdapter"

    lateinit var context: Context

    var currentUserID = ""

    var users = listOf<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagingViewHolder {
        val binding = MessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return MessagingViewHolder(binding)
    }


    override fun onBindViewHolder(holder: MessagingViewHolder, position: Int) {
        val message = getItem(position)

        for (userToLoop in users) {
            if (userToLoop.userID == message.userID) {
                holder.bind(userToLoop, message)
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

    inner class MessagingViewHolder(
        val binding: MessageItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, message: Message) {
            binding.apply {
                when (currentUserID) {
                    user.userID -> {
                        cardViewCurrent.isVisible = true
                        cardView.isVisible = false

                        tvCurrentNick.text = user.nickname
                        tvCurrentText.text = message.text
                        tvCurrentTime.text = formatTime(message.dateTime)
                    }

                    else -> {
                        cardViewCurrent.isVisible = false
                        cardView.isVisible = true

                        tvNickname.text = user.nickname
                        tvText.text = message.text
                        tvTime.text = formatTime(message.dateTime)
                    }
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message) =
            oldItem.messageid == newItem.messageid

        override fun areContentsTheSame(oldItem: Message, newItem: Message) =
            oldItem == newItem
    }

}
