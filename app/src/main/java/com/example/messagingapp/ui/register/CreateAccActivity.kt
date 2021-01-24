package com.example.messagingapp.ui.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.messagingapp.databinding.ActivityCreateAccBinding
import com.example.messagingapp.db.room.entities.User
import com.example.messagingapp.ui.ChatActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class CreateAccActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAccBinding
    val TAG = "tag"
    private val createAccViewModel: CreateAccViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Create account"


        binding.btnRegister.setOnClickListener {

            val name = binding.etName.text.toString()
            val lastName = binding.etLastName.text.toString()
            val nickname = binding.etNickname.text.toString()
            val phoneNumber = intent.getStringExtra("PHONENUMBER").toString()

            if (name.isNotEmpty() && lastName.isNotEmpty() && nickname.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val token = createAccViewModel.getToken(
                        getSharedPreferences(
                            "MyPreferences",
                            Context.MODE_PRIVATE
                        )
                    )
                    var user = User("x", name, lastName, phoneNumber, nickname, token)
                    user = createAccViewModel.registerUserInFirestore(user)

                    val editor = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE).edit()
                    Log.d(
                        TAG,
                        "phoneNum = $phoneNumber, token= ${user.token}, currentUserID= ${user.userID} "
                    )

                    editor.putString("phoneNum", phoneNumber)
                    editor.putString("token", user.token)
                    editor.putString("currentUserID", user.userID)
                    editor.apply()

                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@CreateAccActivity, ChatActivity::class.java)
                        startActivity(intent)
                    }
                }

            } else {
                Snackbar.make(
                    binding.constraintLayout,
                    "Enter all required information",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }


}