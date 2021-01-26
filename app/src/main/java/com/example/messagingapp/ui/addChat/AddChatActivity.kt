package com.example.messagingapp.ui.addChat

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messagingapp.databinding.ActivityAddChatBinding
import com.example.messagingapp.db.room.entities.User
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddChatActivity : AppCompatActivity(), AddChatContactsAdapter.OnItemClickListener {

    private val addChatViewModel: AddChatViewModel by viewModels()
    lateinit var binding: ActivityAddChatBinding
    var contacts = mutableListOf<User>()
    var usersToCreateChat = mutableListOf<String>()
    lateinit var currentUserID: String

    private val TAG = "AddChatActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Add New Chat"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        usersToCreateChat.clear()


        val adapter = AddChatContactsAdapter(mutableListOf(), this)
        binding.recViewContacts.adapter = adapter
        binding.recViewContacts.layoutManager = LinearLayoutManager(this)

        addChatViewModel.getAllUsers().observe(this, {
            adapter.contacts = it as MutableList<User>
            contacts = it
            adapter.notifyDataSetChanged()
        })

        currentUserID = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE).getString(
            "currentUserID",
            ""
        )!!

        binding.ImgViewBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        binding.ImgViewCheck.setOnClickListener {
            createChat()
        }
    }

    override fun onItemClick(position: Int, isChecked: Boolean) {
        if (isChecked) {
            usersToCreateChat.add(contacts[position].userID)
        } else {
            try {
                usersToCreateChat.remove(contacts[position].userID)
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Something went wrong", Snackbar.LENGTH_LONG)
            }
        }
    }

    fun createChat() {
        val chatName = binding.etChatName.text.toString()

        if (chatName.isEmpty()) {
            Snackbar.make(binding.parentAddChatActivity, "Enter chat name", Snackbar.LENGTH_SHORT)
                .show()
            return
        }

        if (!usersToCreateChat.contains(currentUserID)) {
            usersToCreateChat.add(currentUserID)
        }

        if (usersToCreateChat.size < 2) {
            Snackbar.make(
                binding.parentAddChatActivity,
                "Select at least 2 contacts",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            addChatViewModel.createChat(usersToCreateChat, chatName, currentUserID)
            //TODO: NAVIGATE TO CHAT
        }


    }

}