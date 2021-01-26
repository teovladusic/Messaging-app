package com.example.messagingapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.messagingapp.R
import com.example.messagingapp.databinding.ActivityChatBinding
import com.example.messagingapp.ui.addChat.AddChatActivity
import com.example.messagingapp.ui.chat.ChatViewModel
import com.example.messagingapp.ui.register.LoginActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {


    val CREATE_CHAT_CODE = 2
    lateinit var binding: ActivityChatBinding
    private val TAG = "ChatActivity"
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment)
        binding.bottomNavigationView.setupWithNavController(navController)

        binding.btnAddChat.setOnClickListener {
            val intent = Intent(this, AddChatActivity::class.java)
            startActivityForResult(intent, CREATE_CHAT_CODE)
        }

    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val phoneNum = sharedPreferences.getString("phoneNum", "").toString()
        val token = sharedPreferences.getString("token", "").toString()
        val currentUserID = sharedPreferences.getString("currentUserID", "").toString()

        if (phoneNum == "" || currentUserID == "" || token == "") {
            sharedPreferences.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val tokenInDB = chatViewModel.getCurrentUserToken(currentUserID)
                if (token != tokenInDB) {
                    chatViewModel.updateUserToken(token, currentUserID)
                    chatViewModel.updateFirestoreUserField(currentUserID, "token", token)
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CREATE_CHAT_CODE && resultCode == RESULT_CANCELED) {
            Snackbar.make(binding.root, "Chat wasn't created.", Snackbar.LENGTH_SHORT).show()
        }
    }
}