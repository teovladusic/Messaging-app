package com.example.messagingapp.ui.register

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.example.messagingapp.databinding.ActivityLogInBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.*
import java.util.concurrent.TimeUnit


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var phoneNum: String

    private val TAG = "LoginActivity"


    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken


    override fun onCreate(savedInstanceState: Bundle?) {
        try {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var canSendCode = true
        var canVerify = false

        auth = FirebaseAuth.getInstance()

        countDownTimer = object : CountDownTimer(60000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvUntilNextVerification.text = "You can send code again in: ${millisUntilFinished / 1000L} sec"
            }

            override fun onFinish() {
                Snackbar.make(
                    binding.constraintLayout,
                    "You can send verification again",
                    Snackbar.LENGTH_SHORT
                ).show()
                canSendCode = true

                binding.tvUntilNextVerification.visibility = View.INVISIBLE
            }
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.d(TAG, "onVerificationFailed: $e")


                if (e is FirebaseAuthInvalidCredentialsException) {
                    Snackbar.make(
                        binding.constraintLayout,
                        "Invalid request (wrong phone number)",
                        Snackbar.LENGTH_SHORT
                    ).show()

                } else if (e is FirebaseTooManyRequestsException) {
                    Snackbar.make(
                        binding.constraintLayout,
                        "The SMS quota for the project has been exceeded",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    Snackbar.make(
                        binding.constraintLayout,
                        e.message.toString(),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent:$verificationId")
                canSendCode = false
                canVerify = true

                Snackbar.make(binding.constraintLayout, "Code sent", Snackbar.LENGTH_SHORT).show()

                storedVerificationId = verificationId
                resendToken = token

                binding.tvUntilNextVerification.visibility = View.VISIBLE

                countDownTimer.start()
            }
        }


        binding.btnSendVer.setOnClickListener {
            Snackbar.make(binding.constraintLayout, "Sending code...", Snackbar.LENGTH_LONG).show()
            val phoneNumber = binding.etPhoneNum.text.toString()
            if (phoneNumber.isNotEmpty()) {  // && phoneNumber.length == 13) {
                if (canSendCode) {
                    phoneNum = phoneNumber
                    sendVerCode(phoneNumber)
                }
            } else {
                Snackbar.make(
                    binding.constraintLayout,
                    "Enter phone number correctly",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            if (!canSendCode) {
                Snackbar.make(
                    binding.constraintLayout,
                    "Wait before sending another verification",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }



        binding.btnVerify.setOnClickListener {

            if(!canVerify){
                Snackbar.make(binding.constraintLayout, "You have to send verification!", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val code = binding.etCode.text.toString()
            if (code.isNotEmpty()) {
                val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
                signInWithPhoneAuthCredential(credential)

            } else {
                Snackbar.make(
                    binding.constraintLayout,
                    "Enter verification code",
                    Snackbar.LENGTH_SHORT
                ).show()
            }


        }

        }catch (e: Exception){
            Snackbar.make(binding.constraintLayout, e.message.toString(), Snackbar.LENGTH_INDEFINITE).show()
        }

    }

    private fun sendVerCode(phoneNumber: String) {
        try {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)

        }catch (e: Exception){
            Snackbar.make(binding.constraintLayout, "Can't send code", Snackbar.LENGTH_LONG).show()
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    countDownTimer.cancel()
                    binding.tvUntilNextVerification.visibility = INVISIBLE
                    val intent = Intent(this@LoginActivity, CreateAccActivity::class.java)
                    intent.putExtra("PHONENUMBER", phoneNum)
                    startActivityForResult(intent, 0)

                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Snackbar.make(
                            binding.constraintLayout,
                            "Entered Code is incorrect",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        binding.etCode.setText("")
                    } else {
                        Snackbar.make(
                            binding.constraintLayout,
                            task.result.toString(),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_CANCELED) {
            Snackbar.make(
                binding.constraintLayout,
                "You have to register with your information",
                Snackbar.LENGTH_SHORT
            ).show()
        }

    }

}