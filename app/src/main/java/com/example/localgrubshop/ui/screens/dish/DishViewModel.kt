package com.example.localgrubshop.ui.screens.dish

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.localgrubshop.data.models.Dish
import com.example.localgrubshop.domain.repository.MenuRepository
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
    private val menuRepository: MenuRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow<DishUIState>(DishUIState.Idle)
    val uiState: StateFlow<DishUIState> get() = _uiState.asStateFlow()

    private val _dish = MutableStateFlow<Dish?>(null)
    val dish: StateFlow<Dish?> get() = _dish.asStateFlow()

    fun onSetDish(dish: Dish) {
        _dish.update { dish }
    }


    fun saveDish(
        name: String,
        description: String,
        price: Double,
        imageUri: Uri?
    ) {
        _uiState.update { DishUIState.Loading }

        if (name == dish.value?.name && description == dish.value?.description && price == dish.value?.price?.toDouble() && imageUri == dish.value?.thumbnail?.toUri()) {
            _uiState.update { DishUIState.Failure("No changes to save") }
            return
        } else if (imageUri == dish.value?.thumbnail?.toUri()) {
            updateDish(name = name, description = description, price = price, imageUrl = dish.value?.thumbnail ?: "")
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
                            DishUIState.Failure("Image upload failed")
                        }
                        return
                    }

                    updateDish(name = name, description = description, price = price, imageUrl = imageUrl)
                }

                override fun onError(
                    requestId: String?,
                    error: ErrorInfo?
                ) {
                    _uiState.update {
                        DishUIState.Failure(
                            error?.description ?: "Image upload error"
                        )
                    }
                }

                override fun onProgress(
                    requestId: String?,
                    bytes: Long,
                    totalBytes: Long
                ) {
                    // Optional: progress UI
                }

                override fun onStart(requestId: String?) {}

                override fun onReschedule(
                    requestId: String?,
                    error: ErrorInfo?
                ) {
                }
            })
            .dispatch()
    }

    private fun updateDish(
        name: String,
        description: String,
        price: Double,
        imageUrl: String
    ) {
        val dishToSave = _dish.value?.copy(
            name = name,
            description = description,
            price = price.toInt(),
            thumbnail = imageUrl
        ) ?: Dish(
            name = name,
            description = description,
            price = price.toInt(),
            thumbnail = imageUrl
        )
        viewModelScope.launch(Dispatchers.IO) {
            val result = if (dishToSave.id.isEmpty()) {
                menuRepository.addDish(dishToSave)
            } else {
                menuRepository.updateDish(dishToSave)
            }
            _uiState.update { result }
        }
    }
}
