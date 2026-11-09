package com.tecsup.productos.domain.carrito

data class LineaCarrito(
    val productoId: Int,
    val titulo: String,
    val imagenUrl: String?,
    val precioOriginal: Double,
    val precioUnitario: Double,
    val cantidad: Int,
    val stockDisponible: Int,
    val unidadesPorPedido: Int
) {
    val subtotal: Double get() = precioUnitario * cantidad
    val ahorro: Double get() = (precioOriginal - precioUnitario) * cantidad
    val maximoPermitido: Int get() = maximoUnidades(stockDisponible, unidadesPorPedido)
}

data class Carrito(val lineas: List<LineaCarrito>) {
    val cantidadArticulos: Int get() = lineas.sumOf { it.cantidad }
    val subtotal: Double get() = lineas.sumOf { it.precioOriginal * it.cantidad }
    val descuentos: Double get() = lineas.sumOf { it.ahorro }
    val total: Double get() = lineas.sumOf { it.subtotal }
    val estaVacio: Boolean get() = lineas.isEmpty()
}

sealed interface ResultadoCarrito {
    data class Agregado(val titulo: String, val cantidad: Int) : ResultadoCarrito
    data class LimiteStock(val titulo: String, val maximo: Int) : ResultadoCarrito
    data class Agotado(val titulo: String) : ResultadoCarrito
    data object Actualizado : ResultadoCarrito
    data object NoDisponible : ResultadoCarrito
}

fun maximoUnidades(stock: Int, unidadesPorPedido: Int): Int =
    if (unidadesPorPedido <= 0) 0 else maxOf(stock, 0) / unidadesPorPedido
