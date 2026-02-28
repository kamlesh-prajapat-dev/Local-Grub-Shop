package com.example.localgrubshop.data.remote.repository

import com.example.localgrubshop.data.models.GetOffer
import com.example.localgrubshop.data.models.NewOffer
import com.example.localgrubshop.data.remote.mapper.ErrorMapper
import com.example.localgrubshop.domain.models.result.OfferResult
import com.example.localgrubshop.domain.repository.OfferRepository
import com.example.localgrubshop.utils.OfferConstant
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.lang.IllegalStateException
import javax.inject.Inject

class OfferRepositoryImpl @Inject constructor(
    private val realtimeDatabase: FirebaseDatabase
) : OfferRepository {

    override fun getOffers(): Flow<OfferResult> = callbackFlow {
        val ref = realtimeDatabase.getReference(OfferConstant.COLLECTION_NAME)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                runCatching {
                    snapshot.children.mapNotNull { child ->
                        child.getValue(GetOffer::class.java)
                            ?.copy(id = child.key.orEmpty())
                    }
                }.onSuccess { offers ->
                    trySend(OfferResult.GetSuccess(offers))
                        .onFailure {
                            // Channel cancelled — collector gone (normal lifecycle case)
                        }
                }.onFailure { throwable ->
                    trySend(OfferResult.Failure(throwable as Exception))
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

    override suspend fun addOffer(offer: NewOffer): OfferResult {
        return try {
            val ref = realtimeDatabase.getReference(OfferConstant.COLLECTION_NAME)
            val key =
                ref.push().key ?: throw IllegalStateException(
                    "Failed to generate offer id."
                )
            ref.child(key).setValue(offer).await()

            OfferResult.AddSuccess(
                newOffer = offer,
                id = key
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            OfferResult.Failure(e)
        }
    }

    override suspend fun updateOffer(offer: NewOffer, id: String): OfferResult {
        if (id.isBlank()) {
            return OfferResult.Failure(
                IllegalArgumentException(
                    "Offer id cannot be blank."
                )
            )
        }

        return try {
            realtimeDatabase
                .getReference(OfferConstant.COLLECTION_NAME)
                .child(id)
                .setValue(offer)
                .await()

            OfferResult.AddSuccess(
                newOffer = offer,
                id = id
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            OfferResult.Failure(e)
        }
    }

    override suspend fun deleteOffer(offerId: String): OfferResult {
        if (offerId.isBlank()) {
            return OfferResult.Failure(
                IllegalArgumentException(
                    "Offer id cannot be blank."
                )
            )
        }

        return try {
            realtimeDatabase
                .getReference(OfferConstant.COLLECTION_NAME)
                .child(offerId)
                .removeValue()
                .await()

            OfferResult.DeleteSuccess(true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            OfferResult.Failure(e)
        }
    }

    override suspend fun updateOfferStatus(
        offerId: String,
        offerStatus: String
    ): OfferResult {
        if (offerId.isBlank()) {
            return OfferResult.Failure(
                IllegalArgumentException(
                    "Offer id cannot be blank."
                )
            )
        }

        return try {
            realtimeDatabase
                .getReference(OfferConstant.COLLECTION_NAME)
                .child(offerId)
                .child(OfferConstant.OFFER_STATUS)
                .setValue(offerStatus)
                .await()

            OfferResult.UpdateOfferStatusSuccess(true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            OfferResult.Failure(e)
        }
    }
}