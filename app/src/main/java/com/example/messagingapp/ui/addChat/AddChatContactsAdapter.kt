package com.example.messagingapp.ui.addChat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messagingapp.databinding.AddChatContactsItemBinding
import com.example.messagingapp.db.room.entities.User

class AddChatContactsAdapter(
    var contacts: MutableList<User>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<AddChatContactsAdapter.CreateChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateChatViewHolder {
        val binding =
            AddChatContactsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CreateChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CreateChatViewHolder, position: Int) {
        val contact = contacts[position]
        holder.binding.tvNameLastName.text = "${contact.name} ${contact.lastName}"
        holder.binding.tvNickname.text = "${contact.nickname}"
        holder.binding.checkBox.isClickable = false
    }

    override fun getItemCount() = contacts.size

    inner class CreateChatViewHolder(val binding: AddChatContactsItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            var isChecked = binding.checkBox.isChecked
            isChecked = !isChecked
            binding.checkBox.isChecked = isChecked
            if (adapterPosition != RecyclerView.NO_POSITION) {
                listener.onItemClick(adapterPosition, isChecked)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, isChecked: Boolean)
    }
}