package com.example.messagingapp.db.firebase.sendmessages

data class PushNotification(
    val data: NotificationData,
    val to: String
)