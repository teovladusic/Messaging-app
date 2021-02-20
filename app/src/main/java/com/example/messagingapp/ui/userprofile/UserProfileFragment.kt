package com.example.messagingapp.ui.userprofile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.messagingapp.R
import com.example.messagingapp.databinding.FragmentUserProfileBinding
import com.example.messagingapp.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.File

@AndroidEntryPoint
class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserProfileBinding.bind(view)

        viewModel.loadImage()

        binding.apply {
            tvUser.text = "${tvUser.text} ${viewModel.user.nickname}"

            imgViewAddPerson.setOnClickListener {
                viewModel.onAddUserClicked()
            }

            imgViewDeletePerson.setOnClickListener {
                viewModel.onDeleteUserClicked()
            }

            viewModel.user.apply {
                tvNick.text = nickname
                tvName.text = name
                tvLastname.text = lastName
                tvPhoneNumber.text = number
            }


        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.userProfileEvents.collect { event ->
                when (event) {
                    is UserProfileViewModel.UserProfileEvents.ShowMessage -> {
                        Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                    }
                    is UserProfileViewModel.UserProfileEvents.BitmapLoaded -> {
                        binding.imgViewUserPicture.setImageBitmap(event.bitmap)
                    }
                    UserProfileViewModel.UserProfileEvents.SetDefaultImage -> {
                        binding.imgViewUserPicture.setImageResource(R.drawable.default_user)
                    }
                    is UserProfileViewModel.UserProfileEvents.LoadImage -> {
                        val file = File(requireContext().getExternalFilesDir(event.filePath), event.fileName)
                        viewModel.setImageOnProfilePicFromFile(file)
                    }
                    is UserProfileViewModel.UserProfileEvents.FilePathReady -> {
                        val file = File(requireContext().getExternalFilesDir(event.filePath), event.fileName)
                        viewModel.insertBitmapInStorage(file, event.bitmap)
                    }
                }.exhaustive
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}