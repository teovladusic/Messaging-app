package com.example.messagingapp.ui.contacts

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messagingapp.R
import com.example.messagingapp.databinding.FragmentContactsBinding
import com.example.messagingapp.db.room.entities.User
import com.example.messagingapp.ui.profile.UserActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ContactsFragment : Fragment(R.layout.fragment_contacts), ContactsAdapter.OnItemClickListener,
    SearchView.OnQueryTextListener {

    private val TAG = "ContactsFragment"

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    private val contactsViewModel: ContactsViewModel by viewModels()

    var contacts = listOf<User>()
    var users = mutableListOf<User>()

    val adapterContacts = ContactsAdapter(this)
    val adapterUsers = ContactsAdapter(this)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentContactsBinding.bind(view)

        binding.recViewFragmentContacts.adapter = adapterContacts
        binding.recViewFragmentContacts.layoutManager = LinearLayoutManager(requireContext())

        binding.recViewUsers.adapter = adapterUsers
        binding.recViewUsers.layoutManager = LinearLayoutManager(requireContext())


        contactsViewModel.getAllUsers().observe(viewLifecycleOwner, {
            adapterContacts.setData(it)
            contacts = it
            adapterContacts.notifyDataSetChanged()
        })


        binding.searchContacts.isSubmitButtonEnabled = true
        binding.searchContacts.setOnQueryTextListener(this)

    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let {
            searchFirestoreForUsers(it)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        users.clear()
        adapterUsers.notifyDataSetChanged()

        query?.let {
            searchDatabaseForUsers(it)
        }
        return true
    }

    private fun searchDatabaseForUsers(query: String?) {
        val searchQuery = "%$query%"

        contactsViewModel.searchDatabase(searchQuery).observe(this, { list ->
            list.let {
                adapterContacts.setData(it)
                adapterContacts.notifyDataSetChanged()
            }
        })
    }

    private fun searchFirestoreForUsers(query: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            users = contactsViewModel.searchFirestoreUsers(query!!) as MutableList<User>
            withContext(Dispatchers.Main) {
                adapterUsers.setData(users)
                adapterUsers.notifyDataSetChanged()
            }

        }
    }

    override fun onItemClick(user: User) {
        val intent = Intent(requireActivity(), UserActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}