package com.example.messagingapp.ui.chat

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messagingapp.R
import com.example.messagingapp.data.room.entities.Chat
import com.example.messagingapp.databinding.FragmentChatsBinding
import com.example.messagingapp.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ChatsFragment : Fragment(R.layout.fragment_chats), ChatsAdapter.OnItemClickListener,
    SearchView.OnQueryTextListener {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatsFragmentViewModel by viewModels()

    private val TAG = "ChatsFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatsBinding.bind(view)

        val permissions = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(
            requireActivity(),
            permissions.toTypedArray(),
            78432
        )

        viewModel.isLoggedIn.observe(viewLifecycleOwner) {
            when (it) {
                true -> viewModel.onLoggedIn()
                else -> viewModel.onNotLoggedIn()
            }
        }

        val chatsAdapter = ChatsAdapter(this)

        viewModel.chats.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                val chatsWithLastMessages = viewModel.getChatsWithLastMessages(it)
                withContext(Dispatchers.Main) {
                    chatsAdapter.submitList(chatsWithLastMessages)
                }
            }
        }

        binding.apply {
            recViewChats.apply {
                adapter = chatsAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            searchChats.setOnQueryTextListener(this@ChatsFragment)

            btnAddChat.setOnClickListener {
                viewModel.onAddChatClicked()
            }

            searchChats.setOnSearchClickListener {
                tvTitleChats.visibility = View.INVISIBLE
            }
            searchChats.setOnCloseListener {
                tvTitleChats.visibility = View.VISIBLE
                false
            }
        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.chatsEvent.collect { event ->
                when (event) {
                    is ChatsFragmentViewModel.ChatsEvent.NavigateToMessagingScreen -> {
                        val action =
                            ChatsFragmentDirections.actionChatsFragmentToMessagingFragment(event.chat)
                        findNavController().navigate(action)
                    }
                    ChatsFragmentViewModel.ChatsEvent.LoggedIn -> Log.d(TAG, "logged in")
                    ChatsFragmentViewModel.ChatsEvent.NotLoggedIn -> {
                        val action =
                            ChatsFragmentDirections.actionChatsFragmentToVerifyNumberFragment()
                        findNavController().navigate(action)
                    }
                    ChatsFragmentViewModel.ChatsEvent.NavigateToAddChatFragment -> {
                        val action =
                            ChatsFragmentDirections.actionChatsFragmentToAddChatFragment()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }

    }

    override fun onItemClick(chat: Chat) {
        viewModel.onChatClicked(chat)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onQueryTextSubmit(query: String?) = true

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let {
            viewModel.searchQuery.value = it
        }
        return true
    }


}