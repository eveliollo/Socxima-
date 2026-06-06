package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "certificados")
data class Certificado(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prompt: String,
    val responseText: String,
    val modelName: String,
    val blockchain: String,
    val txHash: String,
    val ipfsCid: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sigHex: String,
    val publicKeyHex: String
)
