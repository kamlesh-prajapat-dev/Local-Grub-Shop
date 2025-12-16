package com.example.localgrubshop.ui.sharedviewmodel

import androidx.lifecycle.ViewModel
import com.example.localgrubshop.data.models.OldDish
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SharedMDViewModel: ViewModel() {

    private val _dish = MutableStateFlow<OldDish?>(null)
    val dish: StateFlow<OldDish?> get() = _dish.asStateFlow()

    fun onSetDish(newDish: OldDish) {
        _dish.value = newDish
    }

    fun reset() {
        _dish.update { null }
    }
}