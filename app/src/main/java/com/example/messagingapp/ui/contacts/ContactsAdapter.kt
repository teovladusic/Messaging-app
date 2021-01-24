package com.example.messagingapp.ui.contacts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messagingapp.databinding.ContactsItemBinding
import com.example.messagingapp.db.room.entities.User

class ContactsAdapter(
    private var listener: OnItemClickListener
) :
    RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {

    private var contacts = listOf<User>()

    fun setData(users: List<User>) {
        contacts = users
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val binding =
            ContactsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        val contact = contacts[position]

        holder.binding.tvNameLastName.text = "${contact.name} ${contact.lastName}"
        holder.binding.tvNickname.text = contact.nickname
    }

    override fun getItemCount() = contacts.size

    inner class ContactsViewHolder(val binding: ContactsItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onItemClick(contacts[adapterPosition])
        }
    }

    interface OnItemClickListener {
        fun onItemClick(user: User)
    }
}