package com.example.messagingapp.ui.addChat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messagingapp.data.room.entities.User
import com.example.messagingapp.databinding.UserToCreateChatItemBinding

class UsersToCreateChatAdapter :
    RecyclerView.Adapter<UsersToCreateChatAdapter.UsersToCreateChatViewHolder>() {

    var contacts = listOf<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersToCreateChatViewHolder {
        val binding =
            UserToCreateChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UsersToCreateChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsersToCreateChatViewHolder, position: Int) {
        val user = contacts[position]
        holder.bind(user)
    }

    override fun getItemCount() = contacts.size

    inner class UsersToCreateChatViewHolder(
        val binding: UserToCreateChatItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvNickname.text = user.nickname
        }
    }
}