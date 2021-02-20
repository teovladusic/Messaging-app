package com.example.messagingapp.ui.register.createacc

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.messagingapp.R
import com.example.messagingapp.databinding.FragmentCreateAccBinding
import com.example.messagingapp.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.File

@AndroidEntryPoint
class CreateAccFragment : Fragment(R.layout.fragment_create_acc) {

    private var _binding: FragmentCreateAccBinding? = null
    private val binding get() = _binding!!

    val viewModel: CreateAccountViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreateAccBinding.bind(view)

        binding.apply {
            btnRegister.setOnClickListener {
                val name = etFirstName.text.toString()
                val lastName = etLastName.text.toString()
                val nickname = etNickname.text.toString()

                viewModel.registerUser(name, lastName, nickname)
            }

            imgProfilePic.setOnClickListener {
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI
                ).also {
                    startActivityForResult(it, viewModel.REQUEST_CODE_IMAGE_PICK)
                }
            }
        }

        binding.etNickname.doOnTextChanged { text, _, _, _ ->
            viewModel.onNicknameChanged(text.toString())
        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.createAccEvents.collect { event ->
                when (event) {
                    is CreateAccountViewModel.CreateAccEvents.ShowMessage -> {
                        Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                    }
                    CreateAccountViewModel.CreateAccEvents.RegisteredSuccessfully -> {
                        val action = CreateAccFragmentDirections.actionCreateAccFragmentToChatsFragment()
                        findNavController().navigate(action)
                    }
                    is CreateAccountViewModel.CreateAccEvents.IsNicknameAvailable -> {
                        if (event.isAvailable) {
                            binding.imgViewCheckNickname.visibility = View.VISIBLE
                        }else {
                            binding.imgViewCheckNickname.visibility = View.INVISIBLE
                        }
                    }
                    is CreateAccountViewModel.CreateAccEvents.FilePathReady -> {
                        val file = File(
                            requireContext().getExternalFilesDir(event.filePath),
                            event.fileName
                        )

                        viewModel.imageUri?.let { uri ->
                            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, uri))
                            } else {
                                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                            }
                            viewModel.insertPictureToInternalStorage(bitmap, file)
                        }
                    }
                    is CreateAccountViewModel.CreateAccEvents.Registering -> {
                        binding.btnRegister.isClickable = false
                        binding.progressBarCreateAcc.visibility = View.VISIBLE
                    }
                    CreateAccountViewModel.CreateAccEvents.RegistrationFailed -> {
                        binding.progressBarCreateAcc.visibility = View.GONE
                        binding.btnRegister.isClickable = true
                        Snackbar.make(binding.root, "Something went wrong", Snackbar.LENGTH_SHORT).show()
                    }
                }.exhaustive
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == viewModel.REQUEST_CODE_IMAGE_PICK) {
            data?.data?.let { uri ->
                binding.imgProfilePic.setImageURI(uri)
                viewModel.imageUri = uri
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}