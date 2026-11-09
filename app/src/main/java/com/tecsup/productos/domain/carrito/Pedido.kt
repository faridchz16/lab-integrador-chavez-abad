package com.tecsup.productos.domain.carrito

data class LineaPedido(
    val productoId: Int,
    val titulo: String,
    val imagenUrl: String?,
    val precioUnitario: Double,
    val cantidad: Int
) {
    val subtotal: Double get() = precioUnitario * cantidad
}

data class Pedido(
    val id: Long,
    val fecha: Long,
    val total: Double,
    val cantidadArticulos: Int,
    val lineas: List<LineaPedido>
)

sealed class ErrorPedido : Exception() {
    class CarritoVacio : ErrorPedido()
    class StockInsuficiente(val titulo: String, val disponible: Int) : ErrorPedido()
}
