package com.example.messagingapp.ui.register.verifynumber

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.messagingapp.R
import com.example.messagingapp.databinding.FragmentVerifyNumberBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.util.concurrent.TimeUnit

class VerifyNumberFragment : Fragment(R.layout.fragment_verify_number) {

    private val TAG = "VerifyNumberFragment"

    private var _binding: FragmentVerifyNumberBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VerifyNumberViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentVerifyNumberBinding.bind(view)

        if (!viewModel.canSendCode){
            binding.tvTimer.visibility = View.VISIBLE
        }

        binding.etPhoneNumber.setText(viewModel.number)

        binding.btnCreateChat.setOnClickListener {
            val phoneNum = binding.etPhoneNumber.text.toString().trim()
            viewModel.onNextClicked(phoneNum)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.verifyPhoneNumEvents.collect { event ->
                when (event) {
                    is VerifyNumberViewModel.VerifyPhoneNumEvents.ShowMessage -> {
                        Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                    }

                    is VerifyNumberViewModel.VerifyPhoneNumEvents.ProgressBarVisibility -> {
                        binding.progressBarSendVer.visibility = event.visibility
                    }
                    VerifyNumberViewModel.VerifyPhoneNumEvents.CodeSent -> {
                        val action = VerifyNumberFragmentDirections.actionVerifyNumberFragmentToEnterOtpFragment()
                        findNavController().navigate(action)
                    }
                    is VerifyNumberViewModel.VerifyPhoneNumEvents.SendCode -> {
                        sendVerCode(event.phoneNum)
                    }
                    is VerifyNumberViewModel.VerifyPhoneNumEvents.SecondsUntilNextVerification -> {
                        binding.tvTimer.text = "${event.seconds}s until next verification"
                    }
                    is VerifyNumberViewModel.VerifyPhoneNumEvents.SetTimerVisibility -> {
                        binding.tvTimer.visibility = event.visibility
                    }
                }
            }
        }
    }

    private fun sendVerCode(phoneNumber: String) {
        try {
            val options = PhoneAuthOptions.newBuilder(viewModel.auth)
                .setPhoneNumber(phoneNumber)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(requireActivity())                 // Activity (for callback binding)
                .setCallbacks(viewModel.callbacks)          // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)

        } catch (e: Exception) {
            Snackbar.make(binding.root, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            binding.progressBarSendVer.visibility = View.INVISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}