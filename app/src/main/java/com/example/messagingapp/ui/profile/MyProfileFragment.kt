package com.example.messagingapp.ui.profile

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.messagingapp.R
import com.example.messagingapp.data.room.entities.User
import com.example.messagingapp.databinding.FragmentMyProfileBinding
import com.example.messagingapp.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File


@AndroidEntryPoint
class MyProfileFragment : Fragment(R.layout.fragment_my_profile) {

    private var _binding: FragmentMyProfileBinding? = null
    private val binding: FragmentMyProfileBinding get() = _binding!!

    private val viewModel: MyProfileViewModel by viewModels()

    private val TAG = "MyProfileFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyProfileBinding.bind(view)

        viewModel.user.observe(viewLifecycleOwner) {
            updateUI(it)
        }

        viewModel.loadImage()

        binding.apply {
            tvSave.setOnClickListener {
                viewModel.onSaveClicked(
                    etNick.text.toString(),
                    etName.text.toString(),
                    etLastname.text.toString()
                )
            }
        }

        binding.etNick.doOnTextChanged { text, _, _, _ ->
            viewModel.onNickChanged(text.toString())
        }


        binding.imgProfilePicture.setOnClickListener {
            viewModel.onPicImageClicked()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.myProfileEvents.collect { event ->
                when (event) {
                    is MyProfileViewModel.MyProfileEvents.NicknameAvailable -> {
                        binding.imgCheckNick.setImageResource(R.drawable.ic_check)
                    }
                    is MyProfileViewModel.MyProfileEvents.NicknameUnavailable -> {
                        binding.imgCheckNick.setImageResource(R.drawable.ic_close)
                    }
                    is MyProfileViewModel.MyProfileEvents.ShowMessage -> {
                        Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                    }
                    is MyProfileViewModel.MyProfileEvents.StartGalleryIntent -> {
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.INTERNAL_CONTENT_URI
                        ).also {
                            startActivityForResult(it, viewModel.REQUEST_CODE_IMAGE_PICK)
                        }
                    }
                    is MyProfileViewModel.MyProfileEvents.FilePathReady -> {
                        val file = File(requireContext().getExternalFilesDir(event.filePath), event.fileName)
                        viewModel.insertPictureToInternalStorage(event.bitmap, file)
                    }
                    is MyProfileViewModel.MyProfileEvents.LoadImage -> {
                        val file = File(requireContext().getExternalFilesDir(event.filePath), event.fileName)
                        viewModel.setImageOnProfilePicFromFile(file)
                    }
                    is MyProfileViewModel.MyProfileEvents.BitmapLoaded -> {
                        binding.imgProfilePicture.setImageBitmap(event.bitmap)
                    }
                    is MyProfileViewModel.MyProfileEvents.SetImageResource -> {
                        binding.imgProfilePicture.setImageResource(R.drawable.default_user)
                    }
                }.exhaustive
            }
        }
    }


    private fun updateUI(user: User) {
        binding.apply {
            tvNickname.text = user.nickname
            etNick.setText(user.nickname)
            etName.setText(user.name)
            etLastname.setText(user.lastName)
            tvPhone.text = user.number
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == viewModel.REQUEST_CODE_IMAGE_PICK) {
            data?.data?.let { uri ->
                binding.imgProfilePicture.setImageURI(uri)
                viewModel.onPictureSelected(uri)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}