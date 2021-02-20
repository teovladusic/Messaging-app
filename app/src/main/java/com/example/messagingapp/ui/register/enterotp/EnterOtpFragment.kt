package com.example.messagingapp.ui.register.enterotp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.messagingapp.R
import com.example.messagingapp.databinding.FragmentEnterOtpBinding
import com.example.messagingapp.ui.register.verifynumber.VerifyNumberFragmentDirections
import com.example.messagingapp.ui.register.verifynumber.VerifyNumberViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import kotlinx.coroutines.flow.collect

class EnterOtpFragment : Fragment(R.layout.fragment_enter_otp) {

    private val TAG = "EnterOtpFragment"

    private var _binding: FragmentEnterOtpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VerifyNumberViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEnterOtpBinding.bind(view)

        //GenericTextWatcher here works only for moving to next EditText when a number is entered
        //first parameter is the current EditText and second parameter is next EditText
        binding.apply {
            et1.addTextChangedListener(GenericTextWatcher(et1, et2))
            et2.addTextChangedListener(GenericTextWatcher(et2, et3))
            et3.addTextChangedListener(GenericTextWatcher(et3, et4))
            et4.addTextChangedListener(GenericTextWatcher(et4, et5))
            et5.addTextChangedListener(GenericTextWatcher(et5, et6))
            et6.addTextChangedListener(GenericTextWatcher(et6, null))
        }

        //GenericKeyEvent here works for deleting the element and to switch back to previous EditText
        //first parameter is the current EditText and second parameter is previous EditText
        binding.apply {
            et1.setOnKeyListener(GenericKeyEvent(et1, null))
            et2.setOnKeyListener(GenericKeyEvent(et2, et1))
            et3.setOnKeyListener(GenericKeyEvent(et3, et2))
            et4.setOnKeyListener(GenericKeyEvent(et4, et3))
            et5.setOnKeyListener(GenericKeyEvent(et5, et4))
            et6.setOnKeyListener(GenericKeyEvent(et6, et5))
        }

        binding.apply {
            textViewText.append(viewModel.number)

            btnVerify.setOnClickListener {
                val codeEntered =
                    et1.text.toString() + et2.text.toString() + et3.text.toString() + et4.text.toString() + et5.text.toString() + et6.text.toString()

                viewModel.onVerifyClicked(codeEntered)
            }

            tvDidntReceiveCode.setOnClickListener {
                viewModel.onDidntReceiveCodeClick()
            }
        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.verifyPhoneNumEvents.collect { event ->
                when (event) {
                    is VerifyNumberViewModel.VerifyPhoneNumEvents.SignUserIn -> {
                        signInWithPhoneAuthCredential(event.credential)
                    }

                    is VerifyNumberViewModel.VerifyPhoneNumEvents.ShowMessage -> {
                        Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
                    }

                    is VerifyNumberViewModel.VerifyPhoneNumEvents.SuccessfullySignedIn -> {
                        val action =
                            EnterOtpFragmentDirections.actionEnterOtpFragmentToCreateAccFragment(
                                event.number
                            )
                        findNavController().navigate(action)
                    }
                    is VerifyNumberViewModel.VerifyPhoneNumEvents.GoToVerifyNumber -> {
                        val action =
                            EnterOtpFragmentDirections.actionEnterOtpFragmentToVerifyNumberFragment()
                        findNavController().navigate(action)
                    }
                }
            }
        }

    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        viewModel.auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    viewModel.onSuccessfullySignedIn()
                } else {
                    Snackbar.make(binding.root, "Sign in failed", Snackbar.LENGTH_LONG).show()
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Snackbar.make(binding.root, "Code entered is invalid", Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
            }
    }

    class GenericKeyEvent internal constructor(
        private val currentView: EditText,
        private val previousView: EditText?
    ) : View.OnKeyListener {
        override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {
            if (event!!.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && currentView.id != R.id.et1 && currentView.text.isEmpty()) {
                //If current is empty then previous EditText's number will also be deleted
                previousView!!.text = null
                previousView.requestFocus()
                return true
            }
            return false
        }


    }

    class GenericTextWatcher internal constructor(
        private val currentView: View,
        private val nextView: View?
    ) :
        TextWatcher {
        override fun afterTextChanged(editable: Editable) { // TODO Auto-generated method stub
            val text = editable.toString()
            when (currentView.id) {
                R.id.et1 -> if (text.length == 1) nextView!!.requestFocus()
                R.id.et2 -> if (text.length == 1) nextView!!.requestFocus()
                R.id.et3 -> if (text.length == 1) nextView!!.requestFocus()
                R.id.et4 -> if (text.length == 1) nextView!!.requestFocus()
                R.id.et5 -> if (text.length == 1) nextView!!.requestFocus()
                //You can use EditText4 same as above to hide the keyboard
            }
        }

        override fun beforeTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) {
        }

        override fun onTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) {
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}