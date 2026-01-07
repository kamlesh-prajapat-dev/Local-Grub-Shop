package com.example.localgrubshop.data.remote.firebase.repository

import com.example.localgrubshop.data.models.Order
import com.example.localgrubshop.domain.models.OrderResult
import com.example.localgrubshop.domain.repository.OrderRepository
import com.example.localgrubshop.utils.OrderFields
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val realtimeDatabase: FirebaseDatabase
) : OrderRepository {

    override fun getOrders(): Flow<OrderResult> = callbackFlow {
        val ref = realtimeDatabase.getReference(OrderFields.COLLECTION)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = snapshot.children.mapNotNull { dataSnapshot ->
                    dataSnapshot.getValue(Order::class.java)?.copy(id = dataSnapshot.key ?: "")
                }
                trySend(OrderResult.Success(orders))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)

        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun updateOrderStatus(
        orderId: String,
        newStatus: String
    ): OrderResult {
        return try {
            val update = mapOf<String, Any>(
                OrderFields.STATUS to newStatus
            )

            realtimeDatabase.getReference(OrderFields.COLLECTION).child(orderId)
                .updateChildren(update)
                .await()

            OrderResult.UpdateSuccess(true)
        } catch (e: Exception) {
            OrderResult.Error(e)
        }
    }

    override fun observeOrderById(orderId: String): Flow<OrderResult>  = callbackFlow {
        val ref = realtimeDatabase.getReference(OrderFields.COLLECTION)
        val query = ref.child(orderId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Order::class.java)?.copy(id = orderId)
                if (order != null) {
                    trySend(OrderResult.OrderGetSuccessByOrderId(order))
                } else {
                    trySend(OrderResult.Error(Exception("Order not found")))
                }
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        query.addValueEventListener(listener)
        awaitClose {
            query.removeEventListener(listener)
        }
    }
}
