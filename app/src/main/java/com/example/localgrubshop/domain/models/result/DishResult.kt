package com.example.localgrubshop.domain.models.result

import com.example.localgrubshop.data.models.NewDish
import com.example.localgrubshop.data.models.FetchedDish

sealed interface DishResult {
    data class GetSuccess(val dishes: List<FetchedDish>): DishResult
    data class Failure(val e: Exception): DishResult
    data class StockUpdateSuccess(val isSuccess: Boolean): DishResult
    data class DeleteSuccess(val isSuccess: Boolean): DishResult
    data class UpdateSuccess(val dish: FetchedDish): DishResult
    data class AddSuccess(val dish: NewDish, val id: String): DishResult
}