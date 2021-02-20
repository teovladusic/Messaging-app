package com.example.messagingapp.ui.messaging

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messagingapp.R
import com.example.messagingapp.databinding.FragmentMessagingBinding
import com.example.messagingapp.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.File

@AndroidEntryPoint
class MessagingFragment : Fragment(R.layout.fragment_messaging) {

    private val TAG = "MessagingFragment"

    var _binding: FragmentMessagingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MessagingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMessagingBinding.bind(view)

        viewModel.loadImage()

        val messagingAdapter = MessagingAdapter()

        binding.apply {
            tvChatName.text = viewModel.chat.name

            imgViewBack.setOnClickListener {
                viewModel.onBackClicked()
            }
        }

        viewModel.userPreferences.observe(viewLifecycleOwner) {
            messagingAdapter.currentUserID = it.currentUserID
            messagingAdapter.notifyDataSetChanged()
        }

        viewModel.users.observe(viewLifecycleOwner) {
            messagingAdapter.users = it
            messagingAdapter.notifyDataSetChanged()
        }

        viewModel.messages.observe(viewLifecycleOwner) {
            messagingAdapter.submitList(it)
        }

        binding.recViewMessages.apply {
            adapter = messagingAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.messagingEvent.collect { event ->
                when (event) {
                    MessagingViewModel.MessagingEvent.NavigateToChatsScreen -> {
                        val action =
                            MessagingFragmentDirections.actionMessagingFragmentToChatsFragment()
                        findNavController().navigate(action)
                    }
                    is MessagingViewModel.MessagingEvent.FilePathReady -> {
                        val file = File(requireContext().getExternalFilesDir(event.filePath), event.fileName)
                        viewModel.insertBitmapInStorage(file, event.bitmap)
                    }
                }.exhaustive
            }
        }

        binding.sendMessage.setOnClickListener {
            viewModel.onSendClicked(binding.etMessage.text.toString())
            binding.etMessage.setText("")
        }

    }
}