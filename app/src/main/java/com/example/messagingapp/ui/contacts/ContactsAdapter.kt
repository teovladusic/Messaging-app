package com.example.messagingapp.ui.contacts

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.messagingapp.R
import com.example.messagingapp.databinding.ContactsItemBinding
import com.example.messagingapp.data.room.entities.User
import com.example.messagingapp.databinding.ChatItemBinding
import com.example.messagingapp.ui.chat.ChatWithLastMessage
import com.example.messagingapp.ui.chat.ChatsAdapter
import java.io.File

class ContactsAdapter(
    private var listener: OnItemClickListener
) : ListAdapter<User, ContactsAdapter.ContactsViewHolder>(DiffCallback()) {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        context = parent.context
        val binding =
            ContactsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }

    inner class ContactsViewHolder(val binding: ContactsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                tvNameLastName.text = "${user.name} ${user.lastName}"
                tvNickname.text = user.nickname

                val fileName = "profilePicture.jpg"
                val dir = File(Environment.getExternalStorageState() + "/images")
                val filePath = (dir.toString() + File.separator + "" + user.userID)
                val file = File(context.getExternalFilesDir(filePath), fileName)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    imgView.setImageBitmap(bitmap)
                }else {
                    imgView.setImageResource(R.drawable.default_user)
                    Log.d("TAG", "ne postoji")
                }
            }
        }

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val user = getItem(position)
                    listener.onItemClick(user)
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(user: User)
    }

    class DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) =
            oldItem.userID == newItem.userID

        override fun areContentsTheSame(oldItem: User, newItem: User) =
            oldItem == newItem
    }

}