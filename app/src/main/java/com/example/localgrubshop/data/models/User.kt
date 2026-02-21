package com.example.localgrubshop.data.models

import java.io.Serializable

data class User(
    val uid: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val address: String = ""
): Serializable