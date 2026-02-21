package com.example.localgrubshop.data.remote.repository

import com.example.localgrubshop.data.models.TokenData
import com.example.localgrubshop.data.models.User
import com.example.localgrubshop.domain.models.result.UserResult
import com.example.localgrubshop.domain.repository.UserRepository
import com.example.localgrubshop.utils.DataNotFoundException
import com.example.localgrubshop.utils.UserFields
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) : UserRepository {
    override suspend fun getToken(userId: String): UserResult {
        return try {
            val snapshot = firebaseFirestore.collection(UserFields.TOKEN_COLLECTION)
                .document(userId)
                .get()
                .await()

            val tokenData = snapshot.toObject(TokenData::class.java)

            if (tokenData == null) {
                UserResult.Failure(
                    DataNotFoundException("Token not found")
                )
            } else {
                UserResult.TokenGetSuccess(tokenData.token)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            UserResult.Failure(e)
        }
    }

    override suspend fun getUserByUid(uid: String): UserResult {
        if (uid.isBlank()) {
            return UserResult.Failure(
                IllegalArgumentException("Uid cannot be blank")
            )
        }

        return try {
            val document = FirebaseFirestore.getInstance()
                .collection(UserFields.COLLECTION)
                .document(uid)
                .get()
                .await()

            val user = document?.toObject(User::class.java)?.copy(uid = document.id)
                ?: return UserResult.Failure(
                    DataNotFoundException("User not found")
                )


            UserResult.UserGetSuccess(user)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            UserResult.Failure(e)
        }
    }
}