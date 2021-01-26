package com.example.messagingapp.ui.messaging

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.messagingapp.databinding.MessageItemBinding
import com.example.messagingapp.db.room.entities.Message
import com.example.messagingapp.db.room.entities.User
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MessagingAdapter() : RecyclerView.Adapter<MessagingAdapter.MessagingViewHolder>() {

    private val TAG = "MessagingAdapter"

    lateinit var context: Context
    var messages = listOf<Message>()

    var users = listOf<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagingViewHolder {
        val binding = MessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return MessagingViewHolder(binding)
    }


    override fun onBindViewHolder(holder: MessagingViewHolder, position: Int) {
        val currentUserID = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            .getString("currentUserID", "")!!
        val message = messages[position]
        var user = User()

        if (users.isNotEmpty()) {
            for (userToLoop in users) {
                if (userToLoop.userID == message.userID) {
                    user = userToLoop
                }
            }

            when (currentUserID) {
                user.userID -> {
                    holder.binding.cardViewCurrent.isVisible = true
                    holder.binding.cardView.isVisible = false

                    holder.binding.tvCurrentNick.text = user.nickname
                    holder.binding.tvCurrentText.text = message.text
                    holder.binding.tvCurrentTime.text = formatTime(message.dateTime)
                }

                else -> {
                    holder.binding.cardViewCurrent.isVisible = false
                    holder.binding.cardView.isVisible = true

                    holder.binding.tvNickname.text = user.nickname
                    holder.binding.tvText.text = message.text
                    holder.binding.tvTime.text = formatTime(message.dateTime)
                }
            }
        } else {
            Log.d(TAG, "emptyy je")
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

    override fun getItemCount() = messages.size

    inner class MessagingViewHolder(
        val binding: MessageItemBinding
    ) : RecyclerView.ViewHolder(binding.root)
}