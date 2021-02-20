package com.example.messagingapp.ui.addChat

import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.CheckBox
import androidx.activity.addCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messagingapp.R
import com.example.messagingapp.data.room.entities.User
import com.example.messagingapp.databinding.FragmentAddChatBinding
import com.example.messagingapp.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddChatFragment : Fragment(R.layout.fragment_add_chat),
    SearchView.OnQueryTextListener, AddChatContactsAdapter.OnItemClickListener {

    private var _binding: FragmentAddChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddChatViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddChatBinding.bind(view)

        val contactsAdapter = AddChatContactsAdapter(mutableListOf(), this)
        val usersToCreateChatAdapter = UsersToCreateChatAdapter()

        viewModel.setBtnColor()

        viewModel.users.observe(viewLifecycleOwner) {
            contactsAdapter.contacts = it as MutableList<User>
            contactsAdapter.notifyDataSetChanged()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.usersToCreateChatWith.collect{
                usersToCreateChatAdapter.contacts = it
                usersToCreateChatAdapter.notifyDataSetChanged()

                contactsAdapter.usersToCreateChat = it
                contactsAdapter.notifyDataSetChanged()
            }
        }

        binding.apply {
            searchViewContacts.setOnQueryTextListener(this@AddChatFragment)
            recViewUsers.adapter = contactsAdapter
            recViewUsers.layoutManager = LinearLayoutManager(requireContext())

            recViewUsersToCreateChat.adapter = usersToCreateChatAdapter
            recViewUsersToCreateChat.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            
            btnCreateChat.setOnClickListener {
                viewModel.onCreateChatClick()
            }
            imgViewBack.setOnClickListener {
                viewModel.onBackClicked()
            }



            searchViewContacts.setOnSearchClickListener {
                tvTitleAddChat.visibility = View.GONE
                imgViewBack.visibility = View.GONE
            }

            searchViewContacts.setOnCloseListener {
                tvTitleAddChat.visibility = View.VISIBLE
                imgViewBack.visibility = View.VISIBLE
                false
            }
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
        ItemTouchHelper.UP or ItemTouchHelper.DOWN){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val user = usersToCreateChatAdapter.contacts[viewHolder.bindingAdapterPosition]

                val position = viewModel.users.value?.indexOf(user) ?: -1
                binding.recViewUsers.findViewHolderForAdapterPosition(position)?.itemView?.performClick()
            }

        }).attachToRecyclerView(binding.recViewUsersToCreateChat)


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addChatEvents.collect{ event ->
                when (event) {
                    is AddChatViewModel.AddChatEvents.ShowMessage -> {
                        Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                    }
                    is AddChatViewModel.AddChatEvents.NotifyUsersToCreateChatAdapter -> {
                        usersToCreateChatAdapter.notifyDataSetChanged()
                    }
                    is AddChatViewModel.AddChatEvents.NoUsersSelected -> {
                        binding.btnCreateChat.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(), R.color.grey))
                    }
                    is AddChatViewModel.AddChatEvents.UserSelected -> {
                        binding.btnCreateChat.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(), R.color.azure))
                    }
                    is AddChatViewModel.AddChatEvents.CreateChat -> {
                        val action =
                            AddChatFragmentDirections.actionAddChatFragmentToCreateChatFragment(event.users.toTypedArray())
                        findNavController().navigate(action)
                    }
                    is AddChatViewModel.AddChatEvents.NavigateToMessagingFragment -> {
                        requireActivity().onBackPressed()
                    }
                }.exhaustive
            }
        }
    }

    override fun onQueryTextSubmit(query: String?) = true

    override fun onQueryTextChange(newText: String?): Boolean {
        viewModel.searchQuery.value = newText ?: ""
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(user: User) {
        viewModel.onUserClick(user)
    }
}