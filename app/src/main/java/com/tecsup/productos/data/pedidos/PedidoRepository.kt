package com.tecsup.productos.data.pedidos

import com.tecsup.productos.domain.carrito.Pedido
import kotlinx.coroutines.flow.Flow

interface PedidoRepository {
    val pedidos: Flow<List<Pedido>>
    fun pedidosDesde(desde: Long): Flow<Int>
    suspend fun confirmarPedido(correoUsuario: String): Result<Pedido>
}
