package com.example.localgrubshop.domain.repository

import com.example.localgrubshop.data.models.NewDish
import com.example.localgrubshop.data.models.FetchedDish
import com.example.localgrubshop.domain.models.result.DishResult
import kotlinx.coroutines.flow.Flow

interface DishRepository {
    fun getMenu(): Flow<DishResult>
    suspend fun addDish(newDish: NewDish): DishResult
    suspend fun updateDish(newDish: FetchedDish): DishResult
    suspend fun deleteDish(dishId: String): DishResult
    suspend fun updateStockStatus(dishId: String, inStock: Boolean): DishResult
}