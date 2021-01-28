package com.example.messagingapp.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messagingapp.R
import com.example.messagingapp.databinding.FragmentChatsBinding
import com.example.messagingapp.db.room.entities.Chat
import com.example.messagingapp.db.room.entities.Message
import com.example.messagingapp.ui.messaging.MessagingActivity
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
    var lastMessages = mutableListOf<Message>()

    private val chatsFragmentViewModel: ChatsFragmentViewModel by viewModels()

    lateinit var chatItemAdapter: ChatItemAdapter

    private val TAG = "ChatsFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatsBinding.bind(view)


        chatItemAdapter = ChatItemAdapter(this, chatsFragmentViewModel)

        binding.apply {
            recViewChats.apply {
                adapter = chatItemAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }

        chatsFragmentViewModel.chats.observe(viewLifecycleOwner) {
            chats = it as MutableList<Chat>
            chatItemAdapter.submitList(it)
            chatItemAdapter.notifyDataSetChanged()
        }

        chatsFragmentViewModel.lastMessages.observe(viewLifecycleOwner) {
            chatItemAdapter.lastMessages = it as MutableList<Message>
            lastMessages = it
        }

        chatsFragmentViewModel.allChatIDs.observe(viewLifecycleOwner) {
            chatsFragmentViewModel.subsribeToMessageUpdates(it)
        }

        subscribeToChatUpdates()

        binding.searchChats.setOnQueryTextListener(this)
    }


    fun subscribeToChatUpdates() {
        val currentUserID =
            requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                .getString("currentUserID", "").toString()


        chatsFragmentViewModel.subscribeToChatUpdates(currentUserID)

    }

    override fun onItemClick(position: Int) {
        val chat = chats[position]
        val intent = Intent(requireContext(), MessagingActivity::class.java)
        intent.putExtra("chat", chat)
        startActivity(intent)
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

        chatsFragmentViewModel.searchDBForChats(searchQuery).observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.IO).launch {
                chats = it as MutableList<Chat>
                withContext(Dispatchers.Main) {
                    chatItemAdapter.submitList(it)
                    chatItemAdapter.lastMessages = lastMessages
                    chatItemAdapter.notifyDataSetChanged()
                    Log.d(TAG, "aaaaaaa")
                }
            }
        }
    }


    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let {
            searchDBForChats(it)
        }
        return true
    }


}