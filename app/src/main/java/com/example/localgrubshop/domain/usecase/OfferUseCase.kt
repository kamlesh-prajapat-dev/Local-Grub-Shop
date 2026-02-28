package com.example.localgrubshop.domain.usecase

import com.example.localgrubshop.data.models.NewOffer
import com.example.localgrubshop.domain.mapper.firebase.toGetReqDomainFailure
import com.example.localgrubshop.domain.mapper.firebase.toWriteReqDomainFailure
import com.example.localgrubshop.domain.models.result.OfferResult
import com.example.localgrubshop.domain.repository.OfferRepository
import com.example.localgrubshop.ui.screens.offerbuilder.OfferBuilderUIState
import com.example.localgrubshop.ui.screens.offerdashboard.OfferDashboardUIState
import com.example.localgrubshop.ui.screens.offerdetails.OfferDetailsUIState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfferUseCase @Inject constructor(
    private val offerRepository: OfferRepository
) {
    fun getOffers(): Flow<OfferDashboardUIState> {
        return offerRepository.getOffers()
            .map { result ->
                when (result) {
                    is OfferResult.GetSuccess ->
                        OfferDashboardUIState.Success(result.offers)

                    is OfferResult.Failure ->
                        OfferDashboardUIState.Failure(result.failure.toGetReqDomainFailure("Offers Data."))

                    else -> OfferDashboardUIState.Idle
                }
            }.catch {
                emit(OfferDashboardUIState.Failure(it.toGetReqDomainFailure("Offers Data.")))
            }
    }

    suspend fun deleteOffer(offerId: String): OfferDetailsUIState {
        return when (val result = offerRepository.deleteOffer(offerId)) {
            is OfferResult.DeleteSuccess -> {
                OfferDetailsUIState.DeleteSuccess(result.isSuccess)
            }

            is OfferResult.Failure -> {
                OfferDetailsUIState.Failure(result.failure.toWriteReqDomainFailure(offerId))
            }

            else -> OfferDetailsUIState.Idle
        }
    }

    suspend fun updateOfferStatus(offerId: String, offerStatus: String): OfferDetailsUIState {
        return when (val result = offerRepository.updateOfferStatus(offerId, offerStatus)) {
            is OfferResult.UpdateOfferStatusSuccess -> {
                OfferDetailsUIState.StatusUpdateSuccess(result.isSuccess)
            }

            is OfferResult.Failure -> {
                OfferDetailsUIState.Failure(result.failure.toWriteReqDomainFailure(offerId))
            }

            else -> OfferDetailsUIState.Idle
        }
    }

    suspend fun updateOffer(offerId: String, offer: NewOffer): OfferBuilderUIState {
        return when (val result = offerRepository.updateOffer(id = offerId, offer = offer)) {
            is OfferResult.AddSuccess -> {
                OfferBuilderUIState.Success(true)
            }

            is OfferResult.Failure -> {
                OfferBuilderUIState.Failure(result.failure.toWriteReqDomainFailure(offerId))
            }

            else -> OfferBuilderUIState.Idle
        }
    }

    suspend fun addNewOffer(offer: NewOffer): OfferBuilderUIState {
        return when (val result = offerRepository.addOffer(offer)) {
            is OfferResult.AddSuccess -> {
                OfferBuilderUIState.Success(true)
            }

            is OfferResult.Failure -> {
                OfferBuilderUIState.Failure(result.failure.toWriteReqDomainFailure(offer.promoCode))
            }

            else -> OfferBuilderUIState.Idle
        }
    }
}