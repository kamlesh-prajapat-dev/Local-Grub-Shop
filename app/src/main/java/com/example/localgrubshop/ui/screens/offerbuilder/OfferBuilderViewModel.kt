package com.example.localgrubshop.ui.screens.offerbuilder

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.localgrubshop.data.models.NewOffer
import com.example.localgrubshop.domain.usecase.OfferUseCase
import com.example.localgrubshop.ui.screens.dish.DishUIState
import com.example.localgrubshop.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfferBuilderViewModel @Inject constructor(
    private val offerUseCase: OfferUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow<OfferBuilderUIState>(OfferBuilderUIState.Idle)
    val uiState: StateFlow<OfferBuilderUIState> = _uiState.asStateFlow()

    fun saveOffer(offer: NewOffer, id: String, imageUri: Uri?) {
        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = OfferBuilderUIState.NoInternet
            return
        }

        if (offer.promoCode.isEmpty()) {
            _uiState.value = OfferBuilderUIState.ValidationError("Promo code required")
            return
        }
        if (offer.description.isEmpty()) {
            _uiState.value = OfferBuilderUIState.ValidationError("Description required")
            return
        }
        if (offer.discountValue <= 0) {
            _uiState.value = OfferBuilderUIState.ValidationError("Invalid discount value")
            return
        }
        if (offer.expiryDate <= System.currentTimeMillis()) {
            _uiState.value = OfferBuilderUIState.ValidationError("Expiry date must be in future")
            return
        }

        _uiState.value = OfferBuilderUIState.Loading

        if (imageUri != null) {
            MediaManager.get()
                .upload(imageUri)
                .unsigned("dish_images_unsigned")
                .callback(object : UploadCallback {

                    override fun onSuccess(
                        requestId: String?,
                        resultData: MutableMap<*, *>
                    ) {
                        val imageUrl = resultData["secure_url"] as? String

                        if (imageUrl.isNullOrEmpty()) {
                            _uiState.value = OfferBuilderUIState.ValidationError("Failed to save Image.")
                        }

                        saveOfferToDatabase(id = id, offer = offer)
                    }

                    override fun onError(
                        requestId: String?,
                        error: ErrorInfo?
                    ) {
                        _uiState.value = OfferBuilderUIState.ValidationError("Failed to save image.")
                    }

                    override fun onProgress(
                        requestId: String?,
                        bytes: Long,
                        totalBytes: Long
                    ) { /* Optional: progress UI */
                    }

                    override fun onStart(requestId: String?) {}

                    override fun onReschedule(
                        requestId: String?,
                        error: ErrorInfo?
                    ) {
                    }
                })
                .dispatch()
        } else {
            saveOfferToDatabase(id = id, offer = offer)
        }
    }

    private fun saveOfferToDatabase(id: String, offer: NewOffer) {
        viewModelScope.launch {
            if (id.isBlank()) {
                _uiState.value = offerUseCase.addNewOffer(offer)
                return@launch
            }

            _uiState.value = offerUseCase.updateOffer(offer = offer, offerId = id)
        }
    }

    fun resetState() {
        _uiState.value = OfferBuilderUIState.Idle
    }
}
