package com.example.radiomodern.ui.add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.radiomodern.data.database.AppDatabase
import com.example.radiomodern.data.model.Quotation
import com.example.radiomodern.data.repository.QuotationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddQuotationUiState(
    val price: String = "",
    val description: String = "",
    val photoUri: String? = null,
    val isSaved: Boolean = false,
    val isEditing: Boolean = false,
    val existingQuotation: Quotation? = null
)

class AddQuotationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuotationRepository

    private val _uiState = MutableStateFlow(AddQuotationUiState())
    val uiState: StateFlow<AddQuotationUiState> = _uiState.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).quotationDao()
        repository = QuotationRepository(dao)
    }

    fun loadQuotation(id: Long) {
        if (id <= 0) return
        viewModelScope.launch {
            repository.getById(id).collect { quotation ->
                quotation?.let {
                    _uiState.value = _uiState.value.copy(
                        price = it.price.toString(),
                        description = it.description,
                        photoUri = it.photoUri,
                        isEditing = true,
                        existingQuotation = it
                    )
                }
            }
        }
    }

    fun updatePrice(price: String) {
        _uiState.value = _uiState.value.copy(price = price)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updatePhotoUri(uri: String?) {
        _uiState.value = _uiState.value.copy(photoUri = uri)
    }

    fun save() {
        val state = _uiState.value
        val priceValue = state.price.toDoubleOrNull() ?: return
        if (state.description.isBlank()) return

        viewModelScope.launch {
            if (state.isEditing && state.existingQuotation != null) {
                repository.update(
                    state.existingQuotation.copy(
                        price = priceValue,
                        description = state.description,
                        photoUri = state.photoUri
                    )
                )
            } else {
                repository.insert(
                    Quotation(
                        price = priceValue,
                        description = state.description,
                        photoUri = state.photoUri
                    )
                )
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
