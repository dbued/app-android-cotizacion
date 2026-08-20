package com.example.radiomodern.ui.detail

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

class DetailQuotationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuotationRepository

    private val _quotation = MutableStateFlow<Quotation?>(null)
    val quotation: StateFlow<Quotation?> = _quotation.asStateFlow()

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).quotationDao()
        repository = QuotationRepository(dao)
    }

    fun loadQuotation(id: Long) {
        viewModelScope.launch {
            repository.getById(id).collect {
                _quotation.value = it
            }
        }
    }

    fun delete(quotation: Quotation) {
        viewModelScope.launch {
            repository.delete(quotation)
            _isDeleted.value = true
        }
    }
}
