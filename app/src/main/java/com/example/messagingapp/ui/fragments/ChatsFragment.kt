package com.example.messagingapp.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messagingapp.R
import com.example.messagingapp.adapters.ChatItemAdapter
import com.example.messagingapp.databinding.FragmentChatsBinding
import com.example.messagingapp.db.ChatDatabase
import com.example.messagingapp.db.Repository
import com.example.messagingapp.db.entities.Message
import com.example.messagingapp.ui.ChatViewModel
import com.example.messagingapp.ui.ChatViewModelFactory
import com.google.android.material.snackbar.Snackbar


class ChatsFragment : Fragment(R.layout.fragment_chats) {

    private lateinit var binding: FragmentChatsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentChatsBinding.bind(view)

        //TODO: PROMINI MISTO DI INCIJALIZIRAS STVARI ZA MVVM (dependency injection - dagger hilt)
        val database = ChatDatabase(requireContext())
        val repository = Repository(database)
        val factory = ChatViewModelFactory(repository)
        val chatViewModel = ViewModelProvider(this, factory).get(ChatViewModel::class.java)

        val sharedPreferences =
            requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val currentUserNumber = chatViewModel.getCurrentUserNumber(sharedPreferences)

        var currentUserID = "x"
        if (currentUserNumber != "x") {
            currentUserID = chatViewModel.getUserIdByNumber(currentUserNumber)
        } else {
            Snackbar.make(
                binding.parentFragmentChats,
                "Error: You are not logged in",
                Snackbar.LENGTH_LONG
            ).show()
        }

        /*
        val chat = Chat(0)
        chatViewModel.insertChat(chat)


        val chatUsersCrossRef = mutableListOf<ChatUserCrossRef>()
        chatUsersCrossRef.add(ChatUserCrossRef(chat.chatID, currentUserID))
        chatUsersCrossRef.add(ChatUserCrossRef(chat.chatID, "WiSb2SjRC3BhCIDsdAXg"))

        for(chatUserCrossRef in chatUsersCrossRef){
            chatViewModel.insertChatUserCrossRef(chatUserCrossRef)
        }
         */


        val adapter = ChatItemAdapter(listOf(), chatViewModel, Message(-1, "-1", -1, "-1", "-1", "null"))

        chatViewModel.getAllChats().observe(viewLifecycleOwner, {
            adapter.chats = it
            adapter.notifyDataSetChanged()
        })

        binding.recViewChats.adapter = adapter
        binding.recViewChats.layoutManager = LinearLayoutManager(requireContext())

    }

}