package com.example.localgrubshop.data.remote.firebase.repository

import com.example.localgrubshop.data.models.NewDish
import com.example.localgrubshop.data.models.OldDish
import com.example.localgrubshop.domain.repository.MenuRepository
import com.example.localgrubshop.ui.screens.dish.DishUIState
import com.example.localgrubshop.ui.screens.menu.MenuUIState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MenuRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MenuRepository {

    object DishFields {
        const val NAME = "name"
        const val DESCRIPTION = "description"
        const val PRICE = "price"
        const val THUMBNAIL = "thumbnail"
        const val IN_STOCK = "isAvailable"
        const val COLLECTION = "dishes"
    }

    override suspend fun getMenu(): MenuUIState {
        return try {
            val snapshot = firestore.collection(DishFields.COLLECTION).get().await()

            val document = snapshot.documents
            if (document.isEmpty()) {
                return MenuUIState.Success(emptyList())
            }

            val dishes = document.map {
                OldDish(
                    id = it.id,
                    name = it.getString(DishFields.NAME) ?: "",
                    description = it.getString(DishFields.DESCRIPTION) ?: "",
                    price = it.getLong(DishFields.PRICE)?.toInt() ?: 0,
                    thumbnail = it.getString(DishFields.THUMBNAIL) ?: "",
                    isAvailable = it.getBoolean(DishFields.IN_STOCK) ?: false,
                )
            }
            MenuUIState.Success(dishes)
        } catch (e: Exception) {
            MenuUIState.Failure(e.message ?: "Unknown error")
        }
    }

    override suspend fun addDish(newDish: NewDish): DishUIState {
        return try {
            val dish = firestore.collection(DishFields.COLLECTION).add(newDish).await()
            DishUIState.Success(OldDish(
                id = dish.id,
                name = newDish.name,
                description = newDish.description,
                price = newDish.price,
                thumbnail = newDish.thumbnail,
                isAvailable = newDish.isAvailable
            ))
        } catch (e: Exception) {
            DishUIState.Failure(e.message ?: "Unknown error")
        }
    }

    override suspend fun updateDish(newDish: OldDish): DishUIState {
        return try {
            firestore.collection(DishFields.COLLECTION).document(newDish.id).set(newDish).await()
            DishUIState.Success(newDish)
        } catch (e: Exception) {
            DishUIState.Failure(e.message ?: "Unknown error")
        }
    }

    override suspend fun deleteDish(dishId: String): MenuUIState {
        return try {
            firestore.collection(DishFields.COLLECTION).document(dishId).delete().await()
            MenuUIState.DeleteSuccess(true)
        } catch (e: Exception) {
            MenuUIState.Failure(e.message ?: "Unknown error")
        }
    }

    override suspend fun updateStockStatus(dishId: String, inStock: Boolean): MenuUIState {
        return try {
            firestore.collection(DishFields.COLLECTION).document(dishId).update(DishFields.IN_STOCK, inStock).await()
            MenuUIState.StockUpdateSuccess(true)
        } catch (e: Exception) {
            MenuUIState.Failure(e.message ?: "Unknown error")
        }
    }
}
