package com.example.localgrubshop.domain.usecase

import com.example.localgrubshop.data.models.NewDish
import com.example.localgrubshop.data.models.FetchedDish
import com.example.localgrubshop.domain.models.DishResult
import com.example.localgrubshop.domain.repository.DishRepository
import com.example.localgrubshop.ui.screens.dish.DishUIState
import com.example.localgrubshop.ui.screens.menu.MenuUIState
import javax.inject.Inject

class DishUseCase @Inject constructor(
    private val dishRepository: DishRepository
) {
    suspend fun getMenu(): MenuUIState {
        return when(val result = dishRepository.getMenu()) {
            is DishResult.GetSuccess -> {
                MenuUIState.Success(result.dishes)
            }

            is DishResult.Failure -> {
                MenuUIState.Failure(result.e)
            }

            else -> MenuUIState.Idle
        }
    }

    suspend fun deleteDish(dishId: String): MenuUIState {
        return when(val result = dishRepository.deleteDish(dishId = dishId)) {
            is DishResult.DeleteSuccess -> {
                MenuUIState.DeleteSuccess(result.isSuccess)
            }

            is DishResult.Failure -> {
                MenuUIState.Failure(result.e)
            }

            else -> MenuUIState.Idle
        }
    }

    suspend fun updateStockStatus(dishId: String, inStock: Boolean): MenuUIState {
        return when(val result = dishRepository.updateStockStatus(dishId = dishId, inStock = inStock)) {
            is DishResult.StockUpdateSuccess -> {
                MenuUIState.StockUpdateSuccess(result.isSuccess)
            }

            is DishResult.Failure -> {
                MenuUIState.Failure(result.e)
            }

            else -> MenuUIState.Idle
        }
    }

    suspend fun updateDish(newDish: FetchedDish): DishUIState {
        return when(val result = dishRepository.updateDish(newDish = newDish)) {
            is DishResult.UpdateSuccess -> {
                DishUIState.Success(result.dish)
            }

            is DishResult.Failure -> {
                DishUIState.Failure(result.e)
            }

            else -> DishUIState.Idle
        }
    }

    suspend fun addDish(newDish: NewDish): DishUIState {
        return when(val result = dishRepository.addDish(newDish = newDish)) {
            is DishResult.AddSuccess -> {
                val newDish = result.dish
                val id = result.id
                DishUIState.Success(
                    FetchedDish(
                        id = id,
                        name = newDish.name,
                        description = newDish.description,
                        price = newDish.price,
                        thumbnail = newDish.thumbnail,
                        available = newDish.available
                    )
                )
            }

            is DishResult.Failure -> {
                DishUIState.Failure(result.e)
            }

            else -> DishUIState.Idle
        }
    }
}