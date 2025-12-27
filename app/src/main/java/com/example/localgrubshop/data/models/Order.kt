package com.example.localgrubshop.data.models

data class Order(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAddress: String = "",
    val userPhoneNumber: String = "",
    val items: List<SelectedDishItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val placeAt: Long = 0L,
    val status: String = "Placed",
    val token: String = ""
)
