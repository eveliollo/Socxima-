package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificadoDao {
    @Query("SELECT * FROM certificados ORDER BY timestamp DESC")
    fun getAllCertificados(): Flow<List<Certificado>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificado(certificado: Certificado): Long

    @Query("DELETE FROM certificados WHERE id = :id")
    suspend fun deleteCertificadoById(id: Int)

    @Query("DELETE FROM certificados")
    suspend fun deleteAllCertificados()
}
