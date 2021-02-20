package com.example.messagingapp.ui.createchat

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getBitmap
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messagingapp.R
import com.example.messagingapp.data.room.entities.User
import com.example.messagingapp.databinding.FragmentCreateChatBinding
import com.example.messagingapp.ui.addChat.UsersToCreateChatAdapter
import com.example.messagingapp.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class CreateChatFragment : Fragment(R.layout.fragment_create_chat) {

    private var _binding: FragmentCreateChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateChatViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreateChatBinding.bind(view)


        val usersToCreateChatAdapter = UsersToCreateChatAdapter()

        binding.apply {
            recViewUsers.adapter = usersToCreateChatAdapter
            recViewUsers.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            imgViewBack.setOnClickListener {
                viewModel.onBackClicked()
            }

            imgProfilePicture.setOnClickListener {
                viewModel.onImageClick()
            }

            btnCreateChat.setOnClickListener {
                viewModel.onCreateChatClick(etChatName.text.toString())
            }

            if (viewModel.imageUri != null) {
                imgProfilePicture.setImageURI(viewModel.imageUri)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.usersFlow.collect { users ->
                usersToCreateChatAdapter.contacts = users as List<User>
            }
        }


        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.UP or ItemTouchHelper.DOWN
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.onSwiped(viewHolder.bindingAdapterPosition)
            }
        }).attachToRecyclerView(binding.recViewUsers)


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.createChatEvents.collect { event ->
                when (event) {
                    is CreateChatViewModel.CreateChatEvents.NavigateBack -> {
                        val action =
                            CreateChatFragmentDirections.actionCreateChatFragmentToAddChatFragment(
                                event.users?.toTypedArray()
                            )
                        findNavController().navigate(action)
                    }
                    is CreateChatViewModel.CreateChatEvents.ShowMessage -> {
                        Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                    }
                    is CreateChatViewModel.CreateChatEvents.ChatCreated -> {
                        val action =
                            CreateChatFragmentDirections.actionCreateChatFragmentToMessagingFragment(
                                event.chat
                            )
                        findNavController().navigate(action)
                    }
                    is CreateChatViewModel.CreateChatEvents.Loading -> {
                        binding.progressBarCreatingChat.visibility = View.VISIBLE
                    }
                    is CreateChatViewModel.CreateChatEvents.PickChatImage -> {
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.INTERNAL_CONTENT_URI
                        ).also {
                            startActivityForResult(it, viewModel.REQUEST_CODE_IMAGE_PICK)
                        }
                    }
                    is CreateChatViewModel.CreateChatEvents.FilePathReady -> {
                        val file = File(
                            requireContext().getExternalFilesDir(event.filePath),
                            event.fileName
                        )

                        viewModel.imageUri?.let { uri ->
                            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, uri))
                            } else {
                                getBitmap(requireContext().contentResolver, uri)
                            }
                            viewModel.insertPictureToInternalStorage(bitmap, file)
                        }
                    }
                }.exhaustive
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback {
            viewModel.onBackClicked()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == viewModel.REQUEST_CODE_IMAGE_PICK) {
            data?.data?.let { uri ->
                binding.imgProfilePicture.setImageURI(uri)
                viewModel.imageUri = uri
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}