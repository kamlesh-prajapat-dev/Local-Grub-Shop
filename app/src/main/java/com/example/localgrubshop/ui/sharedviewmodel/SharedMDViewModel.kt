package com.example.localgrubshop.ui.sharedviewmodel

import androidx.lifecycle.ViewModel
import com.example.localgrubshop.data.models.Dish
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SharedMDViewModel: ViewModel() {

    private val _dish = MutableStateFlow<Dish?>(null)
    val dish: StateFlow<Dish?> get() = _dish.asStateFlow()

    fun onSetDish(dish: Dish) {
        _dish.value = dish
    }

    fun reset() {
        _dish.update { null }
    }
}