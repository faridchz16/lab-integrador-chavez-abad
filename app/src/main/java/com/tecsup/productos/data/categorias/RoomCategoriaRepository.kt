package com.tecsup.productos.data.categorias

import com.tecsup.productos.data.local.CategoriaDao
import com.tecsup.productos.data.mapper.aModelo
import com.tecsup.productos.domain.model.Categoria
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCategoriaRepository(
    private val dao: CategoriaDao
) : CategoriaRepository {

    override val categorias: Flow<List<Categoria>> =
        dao.conConteo().map { lista -> lista.map { it.aModelo() } }
}
