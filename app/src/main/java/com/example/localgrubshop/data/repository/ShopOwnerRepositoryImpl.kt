package com.example.localgrubshop.data.repository

import com.example.localgrubshop.data.local.LocalHelper
import com.example.localgrubshop.domain.repository.ShopOwnerRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopOwnerRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val localHelper: LocalHelper
) : ShopOwnerRepository {
    override suspend fun saveFCMToken(
        token: String,
        onResult: (Boolean) -> Unit
    ) {
        try {
            val docId = "ykqsYYKVJ8wNJ4UrvKKm"

            firebaseFirestore.collection("owners").document(docId).update(mapOf("token" to token))
                .await()

            localHelper.setToken(token)
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }
}