package com.example.radiomodern.data.repository

import com.example.radiomodern.data.database.QuotationDao
import com.example.radiomodern.data.model.Quotation
import kotlinx.coroutines.flow.Flow

class QuotationRepository(private val dao: QuotationDao) {

    val allQuotations: Flow<List<Quotation>> = dao.getAll()

    fun getById(id: Long): Flow<Quotation?> = dao.getById(id)

    suspend fun insert(quotation: Quotation): Long = dao.insert(quotation)

    suspend fun update(quotation: Quotation) = dao.update(quotation)

    suspend fun delete(quotation: Quotation) = dao.delete(quotation)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
