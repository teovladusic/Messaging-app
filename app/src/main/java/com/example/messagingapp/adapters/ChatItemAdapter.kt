package com.example.messagingapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messagingapp.databinding.ChatItemBinding
import com.example.messagingapp.entities.Chat

class ChatItemAdapter(private var chats: List<Chat>) : RecyclerView.Adapter<ChatItemAdapter.ChatItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItemHolder {
        val binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatItemHolder(binding)
    }

    override fun getItemCount() = chats.size

    override fun onBindViewHolder(holder: ChatItemHolder, position: Int) {

    }

    class ChatItemHolder(private val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root){

    }
}