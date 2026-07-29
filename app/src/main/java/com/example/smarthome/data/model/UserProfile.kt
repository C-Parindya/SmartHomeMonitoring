package com.example.smarthome.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserProfile(
    val email: String = "",
    val displayName: String = ""
)
