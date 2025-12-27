package com.example.localgrubshop.ui.screens.dish

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.localgrubshop.data.models.NewDish
import com.example.localgrubshop.data.models.FetchedDish
import com.example.localgrubshop.domain.usecase.DishUseCase
import com.example.localgrubshop.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DishViewModel @Inject constructor(
    private val dishUseCase: DishUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {
    private val _uiState = MutableStateFlow<DishUIState>(DishUIState.Idle)
    val uiState: StateFlow<DishUIState> get() = _uiState.asStateFlow()
    private val _dish = MutableStateFlow<FetchedDish?>(null)
    val dish: StateFlow<FetchedDish?> get() = _dish.asStateFlow()
    fun onSetDish(newDish: FetchedDish) {
        _dish.value = newDish
    }
    fun saveDish(
        name: String,
        description: String,
        price: Double,
        imageUri: Uri?,
        isAvailable: Boolean
    ) {
        _uiState.value = DishUIState.Loading

        if (name == dish.value?.name && description == dish.value?.description && price == dish.value?.price?.toDouble() && imageUri == dish.value?.thumbnail?.toUri()) {
            _uiState.value = DishUIState.Error("No changes to save")
            return
        } else if (imageUri == dish.value?.thumbnail?.toUri()) {
            updateDish(name = name, description = description, price = price, imageUrl = dish.value?.thumbnail ?: "", isAvailable = isAvailable)
            return
        }

        if (!networkUtils.isInternetAvailable()) {
            _uiState.update { DishUIState.NoInternet }
            return
        }

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
                        _uiState.update {
                            DishUIState.Error("Image upload failed")
                        }
                        return
                    }

                    updateDish(name = name, description = description, price = price, imageUrl = imageUrl, isAvailable = isAvailable)
                }

                override fun onError(
                    requestId: String?,
                    error: ErrorInfo?
                ) {
                    _uiState.update {
                        DishUIState.Error(
                            error?.description ?: "Image upload error"
                        )
                    }
                }

                override fun onProgress(
                    requestId: String?,
                    bytes: Long,
                    totalBytes: Long
                ) { /* Optional: progress UI */ }

                override fun onStart(requestId: String?) {}

                override fun onReschedule(
                    requestId: String?,
                    error: ErrorInfo?
                ) { }
            })
            .dispatch()
    }
    private fun updateDish(
        name: String,
        description: String,
        price: Double,
        imageUrl: String,
        isAvailable: Boolean
    ) {

        val newDish = _dish.value
        val newDishToSave = newDish?.copy(
            name = name,
            description = description,
            price = price.toInt(),
            thumbnail = imageUrl,
            available = isAvailable
        ) ?: NewDish(
            name = name,
            description = description,
            price = price.toInt(),
            thumbnail = imageUrl,
            available = isAvailable
        )

        viewModelScope.launch(Dispatchers.IO) {
            val result = when (newDishToSave) {
                is NewDish -> {
                    dishUseCase.addDish(newDishToSave)
                }

                is FetchedDish -> {
                    dishUseCase.updateDish(newDishToSave)
                }

                else -> Unit
            }
            _uiState.value = result as DishUIState
        }
    }
}