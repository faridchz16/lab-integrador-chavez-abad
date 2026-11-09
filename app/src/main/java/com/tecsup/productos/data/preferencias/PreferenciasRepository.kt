package com.tecsup.productos.data.preferencias

import com.tecsup.productos.domain.model.ModoTema
import kotlinx.coroutines.flow.Flow

interface PreferenciasRepository {
    val modoTema: Flow<ModoTema>
    suspend fun guardarModoTema(modo: ModoTema)
}
