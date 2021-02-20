package com.example.messagingapp.ui.contacts

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messagingapp.R
import com.example.messagingapp.databinding.FragmentContactsBinding
import com.example.messagingapp.data.room.entities.User
import com.example.messagingapp.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ContactsFragment : Fragment(R.layout.fragment_contacts), ContactsAdapter.OnItemClickListener,
    SearchView.OnQueryTextListener {

    private val TAG = "ContactsFragment"

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentContactsBinding.bind(view)

        val adapterContacts = ContactsAdapter(this)


        binding.apply {
            recViewContacts.apply {
                adapter = adapterContacts
                layoutManager = LinearLayoutManager(requireContext())
            }

            searchContacts.apply {
                isSubmitButtonEnabled = true
                setOnQueryTextListener(this@ContactsFragment)

                setOnSearchClickListener {
                    tvTitleContacts.visibility = View.GONE
                }

                setOnCloseListener {
                    tvTitleContacts.visibility = View.VISIBLE
                    false
                }
            }
        }


        viewModel.users.observe(viewLifecycleOwner, {
            viewModel.areUsersEmpty(it)
            adapterContacts.submitList(it)
        })


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.contactsEvents.collect { event ->
                when (event) {
                    is ContactsViewModel.ContactEvents.UserClicked -> {
                        val action =
                            ContactsFragmentDirections.actionContactFragmentToUserProfileFragment(event.user)
                        findNavController().navigate(action)
                    }
                    ContactsViewModel.ContactEvents.HideNoContactsMessage -> {
                        binding.tvNoContacts.visibility = View.INVISIBLE
                    }
                    ContactsViewModel.ContactEvents.ShowNoContactsMessage -> {
                        binding.tvNoContacts.visibility = View.VISIBLE
                    }
                }.exhaustive
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let {
            viewModel.onSearchFirestoreUsers(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        viewModel.searchQuery.value = query ?: ""
        return true
    }

    override fun onItemClick(user: User) {
        viewModel.onUserClick(user)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}