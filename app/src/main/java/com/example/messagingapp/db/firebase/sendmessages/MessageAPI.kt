package com.example.messagingapp.db.firebase.sendmessages

import com.example.messagingapp.db.firebase.sendmessages.Constants.Companion.CONTENT_TYPE
import com.example.messagingapp.db.firebase.sendmessages.Constants.Companion.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface MessageAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postMessage(
        @Body message: PushNotification
    ): Response<ResponseBody>
}