package com.example.messagingapp.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.messagingapp.R
import com.example.messagingapp.databinding.ActivityChatBinding
import com.example.messagingapp.db.ChatDatabase
import com.example.messagingapp.db.Repository
import com.example.messagingapp.ui.register.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {


    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Razgovori"

        val navController = findNavController(R.id.nav_host_fragment)
        binding.bottomNavigationView.setupWithNavController(navController)

        auth = FirebaseAuth.getInstance()

        //TODO: PROMINI MISTO DI INCIJALIZIRAS STVARI ZA MVVM (dependency injection - dagger hilt)





    }

    override fun onResume() {
        super.onResume()

        val database = ChatDatabase(this)
        val repository = Repository(database)
        val factory = ChatViewModelFactory(repository)
        val chatViewModel = ViewModelProvider(this, factory).get(ChatViewModel::class.java)


        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        if (sharedPreferences.getString("phoneNum", "x").equals("x")) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }
}