package com.example.messagingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.messagingapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Razgovori"


    }
}