package com.example.localgrubshop.data.remote.firebase.repository

import com.example.localgrubshop.data.models.NewDish
import com.example.localgrubshop.data.models.FetchedDish
import com.example.localgrubshop.domain.models.DishResult
import com.example.localgrubshop.domain.repository.DishRepository
import com.example.localgrubshop.utils.DishFields
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DishRepositoryImpl @Inject constructor(
    private val realtimeDatabase: FirebaseDatabase
) : DishRepository {

    override fun getMenu(): Flow<DishResult> = callbackFlow {
        val ref = realtimeDatabase.getReference(DishFields.COLLECTION)

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val dishes = snapshot.children.mapNotNull { child ->
                    child.getValue(FetchedDish::class.java)
                        ?.copy(id = child.key ?: "")
                }
                trySend(DishResult.GetSuccess(dishes))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    override suspend fun addDish(newDish: NewDish): DishResult {
        return try {
            val ref = realtimeDatabase.getReference(DishFields.COLLECTION).push()
            val dishId = ref.key!!
            ref.setValue(newDish).await()
            DishResult.AddSuccess(dish = newDish, id = dishId)
        } catch (e: Exception) {
            DishResult.Failure(e)
        }
    }

    override suspend fun updateDish(newDish: FetchedDish): DishResult {
        return try {
            realtimeDatabase.getReference(DishFields.COLLECTION).child(newDish.id).setValue(newDish).await()
            DishResult.UpdateSuccess(newDish)
        } catch (e: Exception) {
            DishResult.Failure(e)
        }
    }

    override suspend fun deleteDish(dishId: String): DishResult {
        return try {
            realtimeDatabase.getReference(DishFields.COLLECTION).child(dishId).removeValue().await()
            DishResult.DeleteSuccess(true)
        } catch (e: Exception) {
            DishResult.Failure(e)
        }
    }

    override suspend fun updateStockStatus(dishId: String, inStock: Boolean): DishResult {
        return try {
            realtimeDatabase.getReference(DishFields.COLLECTION)
                .child(dishId)
                .child(DishFields.IN_STOCK)
                .setValue(inStock)
                .await()
            DishResult.StockUpdateSuccess(true)
        } catch (e: Exception) {
            DishResult.Failure(e)
        }
    }
}
