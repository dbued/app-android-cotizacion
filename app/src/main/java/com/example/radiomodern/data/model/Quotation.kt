package com.example.radiomodern.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotations")
data class Quotation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val price: Double,
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
