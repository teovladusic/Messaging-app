package com.example.messagingapp.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.messagingapp.R
import com.example.messagingapp.data.PreferencesManager
import com.example.messagingapp.data.UserPreferences
import com.example.messagingapp.databinding.ActivityChatBinding
import com.example.messagingapp.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {


    lateinit var binding: ActivityChatBinding
    private val TAG = "ChatActivity"
    private val viewModel: ChatViewModel by viewModels()
    lateinit var navDestination: NavDestination

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment)
        binding.bottomNavigationView.setupWithNavController(navController)

       viewModel.allChatIDs.observe(this) {
           viewModel.subsribeToMessageUpdates(it)
       }

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            navDestination = destination
            when (destination.id) {
                R.id.messagingFragment, R.id.verifyNumberFragment, R.id.enterOtpFragment,
                R.id.createAccFragment -> {
                    binding.bottomNavigationView.visibility = View.GONE
                }

                else -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onBackPressed() {
        when (navDestination.id) {
            R.id.verifyNumberFragment, R.id.enterOtpFragment, R.id.createAccFragment -> {
                finish()
            }
            else -> super.onBackPressed()
        }
    }
}