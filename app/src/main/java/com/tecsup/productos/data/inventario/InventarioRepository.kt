package com.tecsup.productos.data.inventario

import com.tecsup.productos.domain.inventario.CategoriaEliminada
import com.tecsup.productos.domain.model.Item

interface InventarioRepository {
    suspend fun obtenerProducto(id: Int): Item?
    suspend fun guardarProducto(producto: Item, nuevo: Boolean): Int
    suspend fun eliminarProducto(id: Int)
    suspend fun restaurarProducto(id: Int)
    suspend fun actualizarStock(id: Int, stock: Int)
    suspend fun crearCategoria(nombre: String): Long
    suspend fun renombrarCategoria(id: Long, nombre: String)
    suspend fun eliminarCategoria(id: Long): CategoriaEliminada
    suspend fun restaurarCategoria(categoria: CategoriaEliminada)
}
