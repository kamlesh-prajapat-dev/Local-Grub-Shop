package com.example.localgrubshop.domain.repository

import com.example.localgrubshop.data.models.NewDish
import com.example.localgrubshop.data.models.OldDish
import com.example.localgrubshop.ui.screens.dish.DishUIState
import com.example.localgrubshop.ui.screens.menu.MenuUIState

interface MenuRepository {
    suspend fun getMenu(): MenuUIState
    suspend fun addDish(newDish: NewDish): DishUIState
    suspend fun updateDish(newDish: OldDish): DishUIState
    suspend fun deleteDish(dishId: String): MenuUIState
    suspend fun updateStockStatus(dishId: String, inStock: Boolean): MenuUIState
}
