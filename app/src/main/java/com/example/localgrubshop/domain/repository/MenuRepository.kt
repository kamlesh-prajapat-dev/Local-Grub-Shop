package com.example.localgrubshop.domain.repository

import com.example.localgrubshop.data.models.Dish
import com.example.localgrubshop.ui.screens.dish.DishUIState
import com.example.localgrubshop.ui.screens.menu.MenuUIState

interface MenuRepository {
    suspend fun getMenu(): MenuUIState
    suspend fun getDish(dishId: String): Dish?
    suspend fun addDish(dish: Dish): DishUIState
    suspend fun updateDish(dish: Dish): DishUIState
    suspend fun deleteDish(dishId: String): MenuUIState
    suspend fun updateStockStatus(dishId: String, inStock: Boolean): MenuUIState
}
