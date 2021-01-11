package com.example.messagingapp.ui.register

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.messagingapp.databinding.ActivityCreateAccBinding
import com.example.messagingapp.db.ChatDatabase
import com.example.messagingapp.db.Repository
import com.example.messagingapp.db.entities.User
import com.example.messagingapp.ui.ChatActivity
import com.google.android.material.snackbar.Snackbar


class CreateAccActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAccBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Create account"



        //TODO: PROMINI MISTO DI INCIJALIZIRAS STVARI ZA MVVM
        val database = ChatDatabase(this)
        val repository = Repository(database)
        val factory = CreateAccViewModelFactory(repository)
        val createAccViewModel = ViewModelProvider(this, factory).get(CreateAccViewModel::class.java)

        val TAG = "tag"

        binding.btnRegister.setOnClickListener {

            val name = binding.etName.text.toString()
            val lastName = binding.etLastName.text.toString()
            val nickname = binding.etNickname.text.toString()
            val phoneNumber = intent.getStringExtra("PHONENUMBER").toString()

            if(name.isNotEmpty() && lastName.isNotEmpty() && nickname.isNotEmpty()){
                val user = User("x", name, lastName, phoneNumber, nickname)
                createAccViewModel.registerUserInFirestore(user)
                val editor = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE).edit()
                editor.putString("phoneNum", phoneNumber)
                editor.commit()

                Snackbar.make(binding.constraintLayout, "You are successfully registered!", Snackbar.LENGTH_SHORT).show()
                val intent = Intent(this, ChatActivity::class.java)
                startActivity(intent)

            }else{
                Snackbar.make(binding.constraintLayout, "Enter all required information", Snackbar.LENGTH_SHORT).show()
            }
        }
    }


}