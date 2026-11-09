package com.tecsup.productos.data.categorias

import com.tecsup.productos.domain.model.Categoria
import kotlinx.coroutines.flow.Flow

interface CategoriaRepository {
    val categorias: Flow<List<Categoria>>
}
