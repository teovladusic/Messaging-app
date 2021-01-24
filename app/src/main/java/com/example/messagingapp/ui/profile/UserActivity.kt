package com.example.messagingapp.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.messagingapp.R
import com.example.messagingapp.databinding.ActivityUserBinding
import com.example.messagingapp.db.room.entities.User
import com.example.messagingapp.ui.chat.ChatViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class UserActivity : AppCompatActivity() {

    val TAG = "UserActivity"
    lateinit var user: User
    val chatViewModel: ChatViewModel by viewModels()

    lateinit var binding: ActivityUserBinding
    lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = intent.getParcelableExtra("user")!!

        if (user == null) {
            finish()
        }

        title = "User: ${user.nickname}"

        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        currentUserId = sharedPreferences.getString("currentUserID", "")!!
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu_user, menu)
        return true
    }

    private fun addUser() {
        if (currentUserId == user.userID) {
            Snackbar.make(binding.constraintLayout, "You can't add yourself", Snackbar.LENGTH_SHORT)
                .show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.insertUser(user)
            withContext(Dispatchers.Main) {
                Snackbar.make(
                    binding.constraintLayout,
                    "User added to your contacts",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun removeUser() {
        if (currentUserId == user.userID) {
            Snackbar.make(
                binding.constraintLayout,
                "You can't delete yourself",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.removeUser(user)
            withContext(Dispatchers.Main) {
                Snackbar.make(
                    binding.constraintLayout,
                    "User deleted from your contacts",
                    Snackbar.LENGTH_SHORT
                ).show()
            }


        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.addUser -> {
                addUser()
                true
            }

            R.id.deleteUser -> {
                removeUser()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}