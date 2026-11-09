package com.tecsup.productos.domain.model

const val TIPO_CAMBIO_DOLAR_A_SOL = 3.46

fun Double.dolaresASoles(): Double = Math.round(this * TIPO_CAMBIO_DOLAR_A_SOL * CENTESIMOS) / CENTESIMOS

private const val CENTESIMOS = 100.0
