package com.example.data.repository

import com.example.data.database.Certificado
import com.example.data.database.CertificadoDao
import kotlinx.coroutines.flow.Flow

class CertificadoRepository(private val dao: CertificadoDao) {
    val allCertificados: Flow<List<Certificado>> = dao.getAllCertificados()

    suspend fun insert(certificado: Certificado): Long {
        return dao.insertCertificado(certificado)
    }

    suspend fun deleteById(id: Int) {
        dao.deleteCertificadoById(id)
    }

    suspend fun clearAll() {
        dao.deleteAllCertificados()
    }
}
