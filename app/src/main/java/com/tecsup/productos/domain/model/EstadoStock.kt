package com.tecsup.productos.domain.model

enum class EstadoStock { DISPONIBLE, STOCK_BAJO, AGOTADO, SIN_DATO }

const val LIMITE_STOCK_BAJO = 10

fun Item.estadoStock(): EstadoStock {
    val unidades = stock ?: return EstadoStock.SIN_DATO
    return when {
        unidades <= 0 -> EstadoStock.AGOTADO
        unidades < LIMITE_STOCK_BAJO -> EstadoStock.STOCK_BAJO
        else -> EstadoStock.DISPONIBLE
    }
}

fun Item.tieneDescuento(): Boolean = (descuento ?: 0.0) > 0.0

fun Item.precioFinal(): Double =
    if (tieneDescuento()) precio * (1 - (descuento ?: 0.0) / 100) else precio
