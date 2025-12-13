package com.example.localgrubshop.domain.models

data class FoodItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    var isSelected: Boolean = false
)
