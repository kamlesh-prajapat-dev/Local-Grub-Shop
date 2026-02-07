package com.example.localgrubshop.data.remote.repository

import com.example.localgrubshop.data.models.Order
import com.example.localgrubshop.data.remote.mapper.ErrorMapper
import com.example.localgrubshop.domain.models.result.OrderResult
import com.example.localgrubshop.domain.repository.OrderRepository
import com.example.localgrubshop.utils.DataNotFoundException
import com.example.localgrubshop.utils.OrderFields
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
import kotlin.coroutines.cancellation.CancellationException

class OrderRepositoryImpl @Inject constructor(
    private val realtimeDatabase: FirebaseDatabase
) : OrderRepository {

    override fun getOrders(): Flow<OrderResult> = callbackFlow {
        val ref = realtimeDatabase.getReference(OrderFields.COLLECTION)

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                runCatching {
                    snapshot.children.mapNotNull { child ->
                        child.getValue(Order::class.java)
                            ?.copy(id = child.key.orEmpty())
                    }
                }.onSuccess { orders ->
                    trySend(OrderResult.Success(orders))
                        .onFailure { throwable ->
                            // Channel closed or cancelled — safe to ignore or log
                        }
                }.onFailure { throwable ->
                    trySend(OrderResult.Failure(throwable as Exception))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(OrderResult.Failure(ErrorMapper.map(error)))
                close(ErrorMapper.map(error))
            }
        }

        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    override suspend fun updateOrderStatus(
        orderId: String,
        newStatus: String
    ): OrderResult {
        if (orderId.isBlank()) {
            return OrderResult.Failure(IllegalArgumentException("OrderId cannot be blank"))
        }

        return try {
            val update = mapOf(
                OrderFields.STATUS to newStatus
            )

            realtimeDatabase
                .getReference(OrderFields.COLLECTION)
                .child(orderId)
                .updateChildren(update)
                .await()

            OrderResult.UpdateSuccess(true)
        } catch (e: CancellationException) {
            throw e // coroutine contract: never swallow cancellation
        } catch (e: Exception) {
            OrderResult.Failure(e)
        }
    }

    override fun observeOrderById(orderId: String): Flow<OrderResult> = callbackFlow {
        val ref = realtimeDatabase
            .getReference(OrderFields.COLLECTION)
            .child(orderId)

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                runCatching {
                    snapshot.getValue(Order::class.java)
                        ?.copy(id = orderId)
                        ?: throw DataNotFoundException("Order not found for id=$orderId")
                }.onSuccess { order ->
                    trySend(OrderResult.OrderGetSuccessByOrderId(order))
                        .onFailure {
                            // Channel cancelled — collector no longer active (normal case)
                        }
                }.onFailure { throwable ->
                    trySend(OrderResult.Failure(throwable as Exception))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(OrderResult.Failure(ErrorMapper.map(error)))
                close(ErrorMapper.map(error))
            }
        }

        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
        }
    }
}
