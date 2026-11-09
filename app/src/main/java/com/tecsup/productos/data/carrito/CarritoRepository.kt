package com.tecsup.productos.data.carrito

import com.tecsup.productos.domain.carrito.LineaCarrito
import com.tecsup.productos.domain.carrito.ResultadoCarrito
import kotlinx.coroutines.flow.Flow

interface CarritoRepository {
    val lineas: Flow<List<LineaCarrito>>
    val cantidadArticulos: Flow<Int>
    suspend fun agregar(productoId: Int, cantidad: Int): ResultadoCarrito
    suspend fun cambiarCantidad(productoId: Int, cantidad: Int): ResultadoCarrito
    suspend fun quitar(productoId: Int)
    suspend fun vaciar()
}
