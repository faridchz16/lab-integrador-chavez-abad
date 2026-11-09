package com.tecsup.productos.domain.model

enum class EstadoStock { DISPONIBLE, CERCA_MINIMO, BAJO_MINIMO, AGOTADO, SOBRE_MAXIMO, SIN_DATO }

fun Item.estadoStock(): EstadoStock {
    val unidades = stock ?: return EstadoStock.SIN_DATO
    return when {
        unidades <= 0 -> EstadoStock.AGOTADO
        unidades < stockMinimo -> EstadoStock.BAJO_MINIMO
        unidades <= stockMinimo + margenCercaMinimo() -> EstadoStock.CERCA_MINIMO
        unidades > stockMaximo -> EstadoStock.SOBRE_MAXIMO
        else -> EstadoStock.DISPONIBLE
    }
}

fun Item.tieneStockBajo(): Boolean =
    estadoStock() == EstadoStock.CERCA_MINIMO || estadoStock() == EstadoStock.BAJO_MINIMO

fun Item.tieneDescuento(): Boolean = (descuento ?: 0.0) > 0.0

fun Item.precioFinal(): Double =
    if (tieneDescuento()) precio * (1 - (descuento ?: 0.0) / 100) else precio

private fun Item.margenCercaMinimo(): Int = maxOf(MARGEN_MINIMO_ALERTA, stockMinimo / 2)

private const val MARGEN_MINIMO_ALERTA = 2
