package com.example.localgrubshop.data.models

import com.google.firebase.firestore.PropertyName

data class NewDish(
    val name: String = "",
    val description: String = "",
    val price: Int = 0,
    val thumbnail: String = "",
    @PropertyName("isAvailable")
    val isAvailable: Boolean = false
)
