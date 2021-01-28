package com.example.messagingapp.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.messagingapp.R
import com.example.messagingapp.databinding.FragmentMyProfileBinding
import com.example.messagingapp.db.room.entities.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyProfileFragment : Fragment(R.layout.fragment_my_profile) {

    private var _binding: FragmentMyProfileBinding? = null
    private val binding: FragmentMyProfileBinding get() = _binding!!

    val myProfileViewModel: MyProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyProfileBinding.bind(view)

        val sharedPreferences =
            requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)!!
        val currentUserID = sharedPreferences.getString("currentUserID", "").toString()
        CoroutineScope(Dispatchers.IO).launch {
            val user = myProfileViewModel.getUserByID(currentUserID)
            updateUI(user)
        }

    }

    fun updateUI(user: User) {
        binding.tvNumber.text = user.number
        binding.tvName.text = user.name
        binding.tvLastName.text = user.lastName
        binding.tvNick.text = user.nickname
        binding.tvNickname.text = user.nickname
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}