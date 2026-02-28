package com.example.localgrubshop.data.remote.repository

import com.example.localgrubshop.data.models.NewDish
import com.example.localgrubshop.data.models.FetchedDish
import com.example.localgrubshop.data.remote.mapper.ErrorMapper
import com.example.localgrubshop.domain.models.result.DishResult
import com.example.localgrubshop.domain.repository.DishRepository
import com.example.localgrubshop.utils.DishFields
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class DishRepositoryImpl @Inject constructor(
    private val realtimeDatabase: FirebaseDatabase
) : DishRepository {

    override fun getMenu(): Flow<DishResult> = callbackFlow {
        val ref = realtimeDatabase.getReference(DishFields.COLLECTION)

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                runCatching {
                    snapshot.children.mapNotNull { child ->
                        child.getValue(FetchedDish::class.java)
                            ?.copy(id = child.key.orEmpty())
                    }
                }.onSuccess { dishes ->
                    trySend(DishResult.GetSuccess(dishes))
                        .onFailure {
                            // Channel cancelled — collector gone (normal lifecycle case)
                        }
                }.onFailure { throwable ->
                    trySend(DishResult.Failure(throwable as Exception))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(ErrorMapper.map(error))
            }
        }

        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
        }
    }


    override suspend fun addDish(newDish: NewDish): DishResult {
        return try {
            val ref = realtimeDatabase
                .getReference(DishFields.COLLECTION)
                .push()

            val dishId = ref.key
                ?: throw IllegalStateException("Failed to generate dish id")

            ref.child(dishId).setValue(newDish).await()

            DishResult.AddSuccess(
                dish = newDish,
                id = dishId
            )
        } catch (e: CancellationException) {
            throw e // never swallow coroutine cancellation
        } catch (e: Exception) {
            DishResult.Failure(e)
        }
    }


    override suspend fun updateDish(newDish: FetchedDish): DishResult {
        if (newDish.id.isBlank()) {
            return DishResult.Failure(
                IllegalArgumentException("Dish id cannot be blank")
            )
        }

        return try {
            realtimeDatabase
                .getReference(DishFields.COLLECTION)
                .child(newDish.id)
                .setValue(newDish)
                .await()

            DishResult.UpdateSuccess(newDish)
        } catch (e: CancellationException) {
            throw e // respect structured concurrency
        } catch (e: Exception) {
            DishResult.Failure(e)
        }
    }

    override suspend fun deleteDish(dishId: String): DishResult {
        if (dishId.isBlank()) {
            return DishResult.Failure(
                IllegalArgumentException("Dish id cannot be blank")
            )
        }

        return try {
            realtimeDatabase
                .getReference(DishFields.COLLECTION)
                .child(dishId)
                .removeValue()
                .await()

            DishResult.DeleteSuccess(true)
        } catch (e: CancellationException) {
            throw e // never swallow coroutine cancellation
        } catch (e: Exception) {
            DishResult.Failure(e)
        }
    }

    override suspend fun updateStockStatus(
        dishId: String,
        inStock: Boolean
    ): DishResult {
        if (dishId.isBlank()) {
            return DishResult.Failure(
                IllegalArgumentException("Dish id cannot be blank")
            )
        }

        return try {
            realtimeDatabase
                .getReference(DishFields.COLLECTION)
                .child(dishId)
                .child(DishFields.IN_STOCK)
                .setValue(inStock)
                .await()

            DishResult.StockUpdateSuccess(true)
        } catch (e: CancellationException) {
            throw e // respect structured concurrency
        } catch (e: Exception) {
            DishResult.Failure(e)
        }
    }
}