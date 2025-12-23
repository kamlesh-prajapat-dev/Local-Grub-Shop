package com.example.localgrubshop.data.remote.firebase.repository

import com.example.localgrubshop.domain.models.UserResult
import com.example.localgrubshop.domain.repository.UserRepository
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
                if (token != null) {
                    UserResult.Success(token)
                } else {
                    UserResult.Error(Exception("Token not found"))
                }
            } else {
                UserResult.Error(Exception("User not found"))
            }
        } catch (e: Exception) {
            UserResult.Error(e)
        }
    }
}