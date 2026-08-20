package com.example.radiomodern.data.database

import androidx.room.*
import com.example.radiomodern.data.model.Quotation
import kotlinx.coroutines.flow.Flow

@Dao
interface QuotationDao {

    @Query("SELECT * FROM quotations ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Quotation>>

    @Query("SELECT * FROM quotations WHERE id = :id")
    fun getById(id: Long): Flow<Quotation?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quotation: Quotation): Long

    @Update
    suspend fun update(quotation: Quotation)

    @Delete
    suspend fun delete(quotation: Quotation)

    @Query("DELETE FROM quotations WHERE id = :id")
    suspend fun deleteById(id: Long)
}
