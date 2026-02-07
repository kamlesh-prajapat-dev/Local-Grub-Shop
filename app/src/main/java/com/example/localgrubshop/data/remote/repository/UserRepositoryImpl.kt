package com.example.localgrubshop.data.remote.repository

import com.example.localgrubshop.domain.models.result.UserResult
import com.example.localgrubshop.domain.repository.UserRepository
import com.example.localgrubshop.utils.DataNotFoundException
import com.example.localgrubshop.utils.UserFields
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) : UserRepository {
    override suspend fun getToken(userId: String): UserResult {
        return try {
            val document = firebaseFirestore.collection(UserFields.COLLECTION).document(userId).get().await()

            if (document.exists()) {
                val token = document.getString(UserFields.TOKEN)
                if (token != null && token.isNotBlank()) {
                    UserResult.Success(token)
                } else {
                    UserResult.Error(DataNotFoundException("Token not found"))
                }
            } else {
                UserResult.Error(DataNotFoundException("User not found"))
            }
        } catch (e: Exception) {
            UserResult.Error(e)
        }
    }
}