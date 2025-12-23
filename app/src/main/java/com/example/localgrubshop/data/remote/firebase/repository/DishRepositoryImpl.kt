package com.example.localgrubshop.data.remote.firebase.repository

import com.example.localgrubshop.data.models.NewDish
import com.example.localgrubshop.data.models.FetchedDish
import com.example.localgrubshop.domain.models.DishResult
import com.example.localgrubshop.domain.repository.DishRepository
import com.example.localgrubshop.utils.DishFields
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DishRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DishRepository {

    override suspend fun getMenu(): DishResult {
        return try {
            val snapshot = firestore.collection(DishFields.COLLECTION).get().await()

            val document = snapshot.documents
            if (document.isEmpty()) {
                DishResult.GetSuccess(emptyList())
            } else {
                val dishes = document.map {
                    it.toObject(FetchedDish::class.java)?.copy(id = it.id) ?: FetchedDish()
                }
                DishResult.GetSuccess(dishes)
            }
        } catch (e: Exception) {
            DishResult.Failure(e)
        }
    }

    override suspend fun addDish(newDish: NewDish): DishResult {
        return try {
            val document = firestore.collection(DishFields.COLLECTION).add(newDish).await()
            DishResult.AddSuccess(dish = newDish, id = document.id)
        } catch (e: Exception) {
            DishResult.Failure(e)
        }
    }

    override suspend fun updateDish(newDish: FetchedDish): DishResult {
        return try {
            firestore.collection(DishFields.COLLECTION).document(newDish.id).set(newDish).await()
            DishResult.UpdateSuccess(newDish)
        } catch (e: Exception) {
            DishResult.Failure(e)
        }
    }

    override suspend fun deleteDish(dishId: String): DishResult {
        return try {
            firestore.collection(DishFields.COLLECTION).document(dishId).delete().await()
            DishResult.DeleteSuccess(true)
        } catch (e: Exception) {
            DishResult.Failure(e)
        }
    }

    override suspend fun updateStockStatus(dishId: String, inStock: Boolean): DishResult {
        return try {
            firestore.collection(DishFields.COLLECTION)
                .document(dishId)
                .update(DishFields.IN_STOCK, inStock)
                .await()
            DishResult.StockUpdateSuccess(true)
        } catch (e: Exception) {
            DishResult.Failure(e)
        }
    }
}
