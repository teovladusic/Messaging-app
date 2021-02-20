package com.example.messagingapp.ui.addChat

import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messagingapp.databinding.AddChatContactsItemBinding
import com.example.messagingapp.data.room.entities.User
import java.io.File

class AddChatContactsAdapter(
    var contacts: MutableList<User>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AddChatContactsAdapter.CreateChatViewHolder>() {

    var usersToCreateChat = mutableListOf<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateChatViewHolder {
        val binding =
            AddChatContactsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CreateChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CreateChatViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact)
    }

    override fun getItemCount() = contacts.size

    inner class CreateChatViewHolder(val binding: AddChatContactsItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            binding.root.setOnClickListener(this)
        }

        fun bind(user: User) {
            binding.apply {
                tvNameLastName.text = "${user.name} ${user.lastName}"
                tvNickname.text = "${user.nickname}"
                checkBox.isClickable = false
                checkBox.isChecked = usersToCreateChat.contains(user)

            }
        }

        override fun onClick(v: View?) {
            binding.checkBox.isChecked = !binding.checkBox.isChecked
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                listener.onItemClick(contacts[bindingAdapterPosition])
            }
        }

    }

    interface OnItemClickListener {
        fun onItemClick(user: User)
    }
}