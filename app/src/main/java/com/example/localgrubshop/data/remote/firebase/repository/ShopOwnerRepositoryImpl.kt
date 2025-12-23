package com.example.localgrubshop.data.remote.firebase.repository

import com.example.localgrubshop.domain.models.ShopOwnerResult
import com.example.localgrubshop.domain.repository.ShopOwnerRepository
import com.example.localgrubshop.utils.ShopOwnerFields
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopOwnerRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) : ShopOwnerRepository {
    private var docId: String = "SHOP_OWNER"

    override suspend fun saveFCMToken(token: String): ShopOwnerResult {
        return try {
            firebaseFirestore
                .collection(ShopOwnerFields.COLLECTION)
                .document(docId)
                .set(
                    mapOf(
                        ShopOwnerFields.TOKEN to token
                    ),
                    SetOptions.merge()
                )
                .await()
            ShopOwnerResult.UpdateSuccess(true)
        } catch (e: Exception) {
            ShopOwnerResult.Error(e)
        }
    }

    override suspend fun getFCMToken(): ShopOwnerResult {
        return try {
            val docId = this.docId

            firebaseFirestore.collection(ShopOwnerFields.COLLECTION).document(docId).get().await()
                .getString(ShopOwnerFields.TOKEN)?.let {
                    ShopOwnerResult.Success(it)
                }
                ?: ShopOwnerResult.Error(Exception("Token not found"))
        } catch (e: Exception) {
            ShopOwnerResult.Error(e)
        }
    }
}