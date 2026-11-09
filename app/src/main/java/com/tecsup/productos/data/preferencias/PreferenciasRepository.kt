package com.tecsup.productos.data.preferencias

import com.tecsup.productos.domain.model.ModoTema
import kotlinx.coroutines.flow.Flow

interface PreferenciasRepository {
    val modoTema: Flow<ModoTema>
    val historialBusqueda: Flow<List<String>>
    suspend fun guardarModoTema(modo: ModoTema)
    suspend fun agregarBusqueda(consulta: String)
    suspend fun eliminarBusqueda(consulta: String)
    suspend fun limpiarHistorial()
}
