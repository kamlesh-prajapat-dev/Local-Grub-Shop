package com.example.localgrubshop.domain.repository

import com.example.localgrubshop.data.models.NewDish
import com.example.localgrubshop.data.models.FetchedDish
import com.example.localgrubshop.domain.models.DishResult

interface DishRepository {
    suspend fun getMenu(): DishResult
    suspend fun addDish(newDish: NewDish): DishResult
    suspend fun updateDish(newDish: FetchedDish): DishResult
    suspend fun deleteDish(dishId: String): DishResult
    suspend fun updateStockStatus(dishId: String, inStock: Boolean): DishResult
}