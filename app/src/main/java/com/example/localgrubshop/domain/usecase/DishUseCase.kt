package com.example.localgrubshop.domain.usecase

import com.example.localgrubshop.data.models.NewDish
import com.example.localgrubshop.data.models.FetchedDish
import com.example.localgrubshop.domain.mapper.firebase.toGetReqDomainFailure
import com.example.localgrubshop.domain.mapper.firebase.toWriteReqDomainFailure
import com.example.localgrubshop.domain.models.result.DishResult
import com.example.localgrubshop.domain.repository.DishRepository
import com.example.localgrubshop.ui.screens.dish.DishUIState
import com.example.localgrubshop.ui.screens.menu.MenuUIState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DishUseCase @Inject constructor(
    private val dishRepository: DishRepository
) {
    fun getMenu(): Flow<MenuUIState> {
        return dishRepository.getMenu()
            .map { result ->
                when (result) {
                    is DishResult.GetSuccess ->
                        MenuUIState.Success(result.dishes)

                    is DishResult.Failure ->
                        MenuUIState.GetFailure(result.e.toGetReqDomainFailure("Dishes Data"))

                    else -> MenuUIState.Idle
                }
            }
            .catch { throwable ->
                emit(MenuUIState.GetFailure(throwable.toGetReqDomainFailure("Dishes Data")))
            }
    }


    suspend fun deleteDish(dishId: String): MenuUIState {
        return when (val result = dishRepository.deleteDish(dishId = dishId)) {
            is DishResult.DeleteSuccess -> {
                MenuUIState.DeleteSuccess(result.isSuccess)
            }

            is DishResult.Failure -> {
                MenuUIState.WriteFailure(result.e.toWriteReqDomainFailure(dishId))
            }

            else -> MenuUIState.Idle
        }
    }

    suspend fun updateStockStatus(dishId: String, inStock: Boolean): MenuUIState {
        return when (val result =
            dishRepository.updateStockStatus(dishId = dishId, inStock = inStock)) {
            is DishResult.StockUpdateSuccess -> {
                MenuUIState.StockUpdateSuccess(result.isSuccess)
            }

            is DishResult.Failure -> {
                MenuUIState.WriteFailure(result.e.toWriteReqDomainFailure(dishId))
            }

            else -> MenuUIState.Idle
        }
    }

    suspend fun updateDish(newDish: FetchedDish): DishUIState {
        return when (val result = dishRepository.updateDish(newDish = newDish)) {
            is DishResult.UpdateSuccess -> {
                DishUIState.Success(result.dish)
            }

            is DishResult.Failure -> {
                DishUIState.Failure(result.e.toWriteReqDomainFailure(newDish))
            }

            else -> DishUIState.Idle
        }
    }

    suspend fun addDish(newDish: NewDish): DishUIState {
        return when (val result = dishRepository.addDish(newDish = newDish)) {
            is DishResult.AddSuccess -> {
                val newDish = result.dish
                val id = result.id
                DishUIState.Success(
                    convertNewDishToFetchedDish(newDish = newDish, id = id)
                )
            }

            is DishResult.Failure -> {
                DishUIState.Failure(result.e.toWriteReqDomainFailure(newDish))
            }

            else -> DishUIState.Idle
        }
    }

    private fun convertNewDishToFetchedDish(newDish: NewDish, id: String): FetchedDish {
        return FetchedDish(
            id = id,
            name = newDish.name,
            description = newDish.description,
            price = newDish.price,
            thumbnail = newDish.thumbnail,
            available = newDish.available
        )
    }
}