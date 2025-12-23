package com.example.localgrubshop.ui.sharedviewmodel

import androidx.lifecycle.ViewModel
import com.example.localgrubshop.data.models.FetchedDish
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SharedMDViewModel: ViewModel() {

    private val _dish = MutableStateFlow<FetchedDish?>(null)
    val dish: StateFlow<FetchedDish?> get() = _dish.asStateFlow()

    fun onSetDish(newDish: FetchedDish) {
        _dish.value = newDish
    }

    fun reset() {
        _dish.update { null }
    }
}