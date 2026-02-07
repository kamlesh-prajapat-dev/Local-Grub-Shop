package com.example.localgrubshop.data.remote.repository

import com.example.localgrubshop.data.models.AdminUser
import com.example.localgrubshop.domain.models.result.ShopOwnerResult
import com.example.localgrubshop.domain.repository.ShopOwnerRepository
import com.example.localgrubshop.utils.DataNotFoundException
import com.example.localgrubshop.utils.ShopOwnerFields
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.java

@Singleton
class ShopOwnerRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) : ShopOwnerRepository {

    override suspend fun saveFCMToken(
        token: String,
        docId: String
    ): ShopOwnerResult {
        if (token.isBlank()) {
            return ShopOwnerResult.Error(
                IllegalArgumentException("FCM token cannot be empty")
            )
        }

        if (docId.isBlank()) {
            return ShopOwnerResult.Error(
                IllegalArgumentException("FCM token cannot be empty")
            )
        }

        return try {
            val tokenData = mapOf(
                "token" to token,
                "updatedAt" to System.currentTimeMillis(),
                "platform" to "Android"
            )

            firebaseFirestore
                .collection(ShopOwnerFields.TOKEN_COLLECTION_NAME)
                .document(docId)
                .set(
                    mapOf(
                        ShopOwnerFields.TOKEN to tokenData
                    ),
                    SetOptions.merge()
                )
                .await()

            ShopOwnerResult.UpdateSuccess(true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ShopOwnerResult.Error(e)
        }
    }


    override suspend fun login(
        username: String,
        password: String
    ): ShopOwnerResult {
        return try {
            val snapshot = firebaseFirestore
                .collection(ShopOwnerFields.COLLECTION)
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return ShopOwnerResult.Error(
                    DataNotFoundException("Invalid username or password")
                )
            }

            val document = snapshot.documents.first()
            val adminUser = document.toObject(AdminUser::class.java)
                ?.copy(id = document.id)

            if (adminUser == null) {
                ShopOwnerResult.Error(
                    DataNotFoundException("Admin user mapping failed")
                )
            } else if (adminUser.password != password) {
                ShopOwnerResult.Error(
                    DataNotFoundException("Invalid username or password")
                )
            } else {
                ShopOwnerResult.Success(adminUser)
            }

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ShopOwnerResult.Error(e)
        }
    }
}