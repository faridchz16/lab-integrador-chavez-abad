package com.tecsup.productos.domain.inventario

import com.tecsup.productos.domain.model.EstadoStock
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.estadoStock

enum class NivelAlerta { AGOTADO, BAJO_MINIMO, CERCA_MINIMO, SOBRE_MAXIMO }

data class AlertaStock(
    val producto: Item,
    val nivel: NivelAlerta
) {
    val stock: Int get() = producto.stock ?: 0

    val unidadesSugeridas: Int
        get() = if (nivel == NivelAlerta.SOBRE_MAXIMO) {
            0
        } else {
            (producto.stockMaximo - stock).coerceAtLeast(1)
        }

    val excedente: Int
        get() = if (nivel == NivelAlerta.SOBRE_MAXIMO) stock - producto.stockMaximo else 0
}

fun Item.nivelAlerta(): NivelAlerta? = when (estadoStock()) {
    EstadoStock.AGOTADO -> NivelAlerta.AGOTADO
    EstadoStock.BAJO_MINIMO -> NivelAlerta.BAJO_MINIMO
    EstadoStock.CERCA_MINIMO -> NivelAlerta.CERCA_MINIMO
    EstadoStock.SOBRE_MAXIMO -> NivelAlerta.SOBRE_MAXIMO
    EstadoStock.DISPONIBLE, EstadoStock.SIN_DATO -> null
}

fun NivelAlerta.requiereReposicion(): Boolean = this != NivelAlerta.SOBRE_MAXIMO

fun NivelAlerta.esCritica(): Boolean =
    this == NivelAlerta.AGOTADO || this == NivelAlerta.BAJO_MINIMO

fun List<Item>.alertas(): List<AlertaStock> =
    mapNotNull { item -> item.nivelAlerta()?.let { nivel -> AlertaStock(item, nivel) } }
        .sortedWith(compareBy<AlertaStock>({ it.nivel.ordinal }, { it.stock }, { it.producto.titulo }))

fun List<Item>.cantidadPorReponer(): Int = count { it.nivelAlerta()?.requiereReposicion() == true }

fun nuevasAlertasCriticas(
    anteriores: Map<Int, NivelAlerta>,
    actuales: List<Item>
): List<AlertaStock> = actuales.mapNotNull { item ->
    val nivel = item.nivelAlerta()?.takeIf { it.esCritica() } ?: return@mapNotNull null
    val previo = anteriores[item.id]
    if (previo == nivel || (previo == NivelAlerta.AGOTADO && nivel == NivelAlerta.BAJO_MINIMO)) {
        null
    } else {
        AlertaStock(item, nivel)
    }
}

fun List<Item>.nivelesCriticos(): Map<Int, NivelAlerta> =
    mapNotNull { item -> item.nivelAlerta()?.takeIf { it.esCritica() }?.let { item.id to it } }
        .toMap()
