package com.example.messagingapp.ui.register.verifynumber

import android.os.CountDownTimer
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class VerifyNumberViewModel: ViewModel() {

    private val TAG = "VerifyNumberViewModel"

    val auth = FirebaseAuth.getInstance()

    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private val verifyPhoneNumEventsChannel = Channel<VerifyPhoneNumEvents>()
    var verifyPhoneNumEvents = verifyPhoneNumEventsChannel.receiveAsFlow()

    var canSendCode = true

    var number = ""

    fun onNextClicked(phoneNum: String) {

        if (phoneNum.length != 13) {
            viewModelScope.launch {
                verifyPhoneNumEventsChannel.send(VerifyPhoneNumEvents.ShowMessage("Phone number must have 13 digits"))
            }
            //return
        }

        if(!canSendCode){
            viewModelScope.launch {
                verifyPhoneNumEventsChannel.send(VerifyPhoneNumEvents.ShowMessage("Please wait until another verification"))
            }
            return
        }

        number = phoneNum
        GlobalScope.launch {
            verifyPhoneNumEventsChannel.send(VerifyPhoneNumEvents.ProgressBarVisibility(View.VISIBLE))
            verifyPhoneNumEventsChannel.send(VerifyPhoneNumEvents.SendCode(phoneNum))
        }
    }


    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            viewModelScope.launch {
                verifyPhoneNumEventsChannel.send(VerifyPhoneNumEvents.SignUserIn(credential))
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    viewModelScope.launch {
                        verifyPhoneNumEventsChannel.send(VerifyPhoneNumEvents.ShowMessage("Wrong phone number"))
                        verifyPhoneNumEventsChannel.send(VerifyPhoneNumEvents.ProgressBarVisibility(View.INVISIBLE))

                    }

                }
                is FirebaseTooManyRequestsException -> {
                    viewModelScope.launch {
                        verifyPhoneNumEventsChannel.send(VerifyPhoneNumEvents.ShowMessage("The SMS quota for the project has been exceeded"))
                        verifyPhoneNumEventsChannel.send(VerifyPhoneNumEvents.ProgressBarVisibility(View.INVISIBLE))
                    }
                }
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            viewModelScope.launch {
                verifyPhoneNumEventsChannel.send(VerifyPhoneNumEvents.ProgressBarVisibility(View.INVISIBLE))
                verifyPhoneNumEventsChannel.send(VerifyPhoneNumEvents.CodeSent)
            }
            storedVerificationId = verificationId
            resendToken = token

            countDownTimer.start()
            canSendCode = false
        }
    }

    fun onVerifyClicked(codeEntered: String) {
        if(codeEntered.length != 6) {
            viewModelScope.launch {
                verifyPhoneNumEventsChannel.send(VerifyPhoneNumEvents.ShowMessage("Please enter a code with 6 digits"))
            }
            return
        }

        val credential = PhoneAuthProvider.getCredential(storedVerificationId, codeEntered)

        viewModelScope.launch {
            verifyPhoneNumEventsChannel.send(VerifyPhoneNumEvents.SignUserIn(credential))
        }
    }

    fun onSuccessfullySignedIn() = viewModelScope.launch {
        verifyPhoneNumEventsChannel.send(VerifyPhoneNumEvents.SuccessfullySignedIn(number))
    }

    fun onDidntReceiveCodeClick() {
        viewModelScope.launch {
            verifyPhoneNumEventsChannel.send(VerifyPhoneNumEvents.GoToVerifyNumber)
        }
    }

    val countDownTimer = object : CountDownTimer(60000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            viewModelScope.launch {
                verifyPhoneNumEventsChannel.send(VerifyPhoneNumEvents.SecondsUntilNextVerification((millisUntilFinished / 1000).toString()))
            }
        }

        override fun onFinish() {
            canSendCode = true
            viewModelScope.launch {
                verifyPhoneNumEventsChannel.send(VerifyPhoneNumEvents.SetTimerVisibility(View.INVISIBLE))
            }
        }
    }

    sealed class VerifyPhoneNumEvents {
        data class ProgressBarVisibility(val visibility: Int) : VerifyPhoneNumEvents()
        data class ShowMessage(val message: String) : VerifyPhoneNumEvents()
        data class SendCode(val phoneNum: String) : VerifyPhoneNumEvents()
        data class SignUserIn(val credential: PhoneAuthCredential) : VerifyPhoneNumEvents()
        object CodeSent : VerifyPhoneNumEvents()
        data class SuccessfullySignedIn(val number: String) : VerifyPhoneNumEvents()
        object GoToVerifyNumber : VerifyPhoneNumEvents()
        data class SecondsUntilNextVerification(val seconds: String) : VerifyPhoneNumEvents()
        data class SetTimerVisibility(val visibility: Int) : VerifyPhoneNumEvents()
    }
}