package com.example.localgrubshop.data.repository

import com.example.localgrubshop.data.local.LocalDatabase
import com.example.localgrubshop.domain.repository.ShopOwnerRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopOwnerRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val localDatabase: LocalDatabase
) : ShopOwnerRepository {

    private var docId: String? = null


    override suspend fun saveFCMToken(token: String, onResult: (Boolean) -> Unit) {
        try {
            firebaseFirestore
                .collection("owners")
                .document("SHOP_OWNER")
                .set(
                    mapOf(
                        "token" to token,
                        "shop_owner" to true
                    ),
                    SetOptions.merge()
                )
                .await()

            localDatabase.setToken(token)
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }

    override suspend fun getFCMToken(
        onResult: (String) -> Unit
    ) {
        try {
            val docId = this.docId
            if (docId != null) {
                firebaseFirestore.collection("owners").document(docId).get().await()
                    .getString("token")?.let {
                        onResult(it)
                    }
            }
        } catch (e: Exception) {
            onResult("")
        }
    }
}