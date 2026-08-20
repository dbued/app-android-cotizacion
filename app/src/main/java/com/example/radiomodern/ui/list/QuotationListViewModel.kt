package com.example.radiomodern.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.radiomodern.data.database.AppDatabase
import com.example.radiomodern.data.model.Quotation
import com.example.radiomodern.data.repository.QuotationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuotationListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuotationRepository

    val allQuotations: StateFlow<List<Quotation>>

    init {
        val dao = AppDatabase.getDatabase(application).quotationDao()
        repository = QuotationRepository(dao)
        allQuotations = repository.allQuotations
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun delete(quotation: Quotation) {
        viewModelScope.launch { repository.delete(quotation) }
    }

    fun insert(quotation: Quotation) {
        viewModelScope.launch { repository.insert(quotation) }
    }
}
