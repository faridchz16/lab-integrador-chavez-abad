package com.tecsup.productos.domain.inventario

import com.tecsup.productos.domain.model.EstadoStock
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.estadoStock
import com.tecsup.productos.domain.model.precioFinal
import java.util.Locale

fun generarCsvInventario(
    items: List<Item>,
    encabezados: List<String>,
    etiquetaEstado: (EstadoStock) -> String
): String = buildString {
    append(encabezados.joinToString(SEPARADOR) { it.escaparCsv() })
    append(SALTO)
    items.forEach { item ->
        val fila = listOf(
            item.id.toString(),
            item.titulo,
            item.marca,
            item.categoria.orEmpty(),
            item.precio.comoDecimal(),
            (item.descuento ?: 0.0).comoDecimal(),
            item.precioFinal().comoDecimal(),
            (item.stock ?: 0).toString(),
            item.stockMinimo.toString(),
            item.stockMaximo.toString(),
            item.unidadesPorPedido.toString(),
            etiquetaEstado(item.estadoStock())
        )
        append(fila.joinToString(SEPARADOR) { it.escaparCsv() })
        append(SALTO)
    }
}

private fun Double.comoDecimal(): String = String.format(Locale.US, "%.2f", this)

private fun String.escaparCsv(): String =
    if (any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
        "\"" + replace("\"", "\"\"") + "\""
    } else {
        this
    }

private const val SEPARADOR = ","
private const val SALTO = "\r\n"
