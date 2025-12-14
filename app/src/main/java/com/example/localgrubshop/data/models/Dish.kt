package com.example.localgrubshop.data.models

data class Dish(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Int = 0,
    val thumbnail: String = "",
    val isAvailable: Boolean = false,
    val quantity: Int = 0
)
