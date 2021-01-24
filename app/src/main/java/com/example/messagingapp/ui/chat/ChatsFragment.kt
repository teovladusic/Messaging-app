package com.example.messagingapp.ui.chat

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messagingapp.R
import com.example.messagingapp.databinding.FragmentChatsBinding
import com.example.messagingapp.db.room.entities.Chat
import com.example.messagingapp.db.room.entities.Message
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class ChatsFragment : Fragment(R.layout.fragment_chats), ChatItemAdapter.OnItemClickListener,
    SearchView.OnQueryTextListener {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    var chats = mutableListOf<Chat>()

    private val chatsFragmentViewModel: ChatsFragmentViewModel by viewModels()


    private val adapter = ChatItemAdapter(this)

    private val TAG = "ChatsFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatsBinding.bind(view)



        binding.recViewChats.adapter = adapter
        binding.recViewChats.layoutManager = LinearLayoutManager(requireContext())

        chatsFragmentViewModel.getAllChats().observe(viewLifecycleOwner, {
            CoroutineScope(Dispatchers.IO).launch {
                val lastMessages = setLastMessages(it)
                chats = it as MutableList<Chat>

                withContext(Dispatchers.Main) {
                    adapter.chats = it
                    adapter.lastMessages = lastMessages
                    adapter.notifyDataSetChanged()
                }
            }


        })

        chatsFragmentViewModel.getAllChatsIDs().observe(viewLifecycleOwner, {
            CoroutineScope(Dispatchers.IO).launch {
                chatsFragmentViewModel.subsribeToMessageUpdates(it)
            }
        })

        subscribeToChatUpdates()

        binding.searchChats.setOnQueryTextListener(this)
    }


    fun subscribeToChatUpdates() {
        val currentUserID =
            requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                .getString("currentUserID", "").toString()

        CoroutineScope(Dispatchers.IO).launch {
            chatsFragmentViewModel.subscribeToChatUpdates(currentUserID)
        }
    }

    suspend fun setLastMessages(chats: List<Chat>): MutableList<Message> {
        val lastMessages = mutableListOf<Message>()
        CoroutineScope(Dispatchers.IO).launch {
            for (chat in chats) {
                lastMessages.add(chatsFragmentViewModel.getMessageByID(chat.lastMessageID))
            }
        }.join()
        return lastMessages
    }

    override fun onItemClick(position: Int) {
        val chat = chats[position]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    private fun searchDBForChats(query: String) {
        val searchQuery = "%$query%"

        chatsFragmentViewModel.searchDBForChats(searchQuery).observe(viewLifecycleOwner, {
            CoroutineScope(Dispatchers.IO).launch {
                val lastMessages = setLastMessages(it)
                chats = it as MutableList<Chat>
                withContext(Dispatchers.Main) {
                    adapter.chats = it
                    adapter.lastMessages = lastMessages
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let {
            searchDBForChats(it)
        }
        return true
    }
}