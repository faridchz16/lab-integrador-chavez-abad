package com.tecsup.productos.domain.catalogo

import com.tecsup.productos.domain.model.EstadoStock
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.TipoProducto
import com.tecsup.productos.domain.model.estadoStock
import com.tecsup.productos.domain.model.precioFinal
import java.text.Normalizer

enum class OrdenCatalogo { NOMBRE_ASC, NOMBRE_DESC, PRECIO_ASC, PRECIO_DESC, STOCK_ASC, STOCK_DESC }

enum class FiltroStock { DISPONIBLE, STOCK_BAJO, AGOTADO }

data class FiltroCatalogo(
    val consulta: String = "",
    val categorias: Set<Long> = emptySet(),
    val tipos: Set<TipoProducto> = emptySet(),
    val precioMinimo: Double? = null,
    val precioMaximo: Double? = null,
    val estadosStock: Set<FiltroStock> = emptySet(),
    val orden: OrdenCatalogo = OrdenCatalogo.NOMBRE_ASC
) {

    val cantidadActivos: Int
        get() = listOf(
            categorias.isNotEmpty(),
            tipos.isNotEmpty(),
            precioMinimo != null || precioMaximo != null,
            estadosStock.isNotEmpty(),
            orden != OrdenCatalogo.NOMBRE_ASC
        ).count { it }

    val tieneCriterios: Boolean
        get() = cantidadActivos > 0 || consulta.isNotBlank()

    fun sinFiltros(): FiltroCatalogo = FiltroCatalogo(consulta = consulta)

    fun aplicarA(items: List<Item>): List<Item> {
        val termino = consulta.normalizada()
        val filtrados = items.filter { item ->
            coincideConsulta(item, termino) &&
                coincideCategoria(item) &&
                (tipos.isEmpty() || item.tipo in tipos) &&
                coincidePrecio(item) &&
                coincideStock(item)
        }
        return when (orden) {
            OrdenCatalogo.NOMBRE_ASC -> filtrados.sortedBy { it.titulo.normalizada() }
            OrdenCatalogo.NOMBRE_DESC -> filtrados.sortedByDescending { it.titulo.normalizada() }
            OrdenCatalogo.PRECIO_ASC -> filtrados.sortedBy { it.precioFinal() }
            OrdenCatalogo.PRECIO_DESC -> filtrados.sortedByDescending { it.precioFinal() }
            OrdenCatalogo.STOCK_ASC -> filtrados.sortedBy { it.stock ?: 0 }
            OrdenCatalogo.STOCK_DESC -> filtrados.sortedByDescending { it.stock ?: 0 }
        }
    }

    private fun coincideConsulta(item: Item, termino: String): Boolean {
        if (termino.isBlank()) return true
        return listOfNotNull(item.titulo, item.marca, item.categoria, item.descripcion)
            .any { it.normalizada().contains(termino) }
    }

    private fun coincideCategoria(item: Item): Boolean =
        categorias.isEmpty() || item.categoriaId in categorias

    private fun coincidePrecio(item: Item): Boolean {
        val precio = item.precioFinal()
        return (precioMinimo == null || precio >= precioMinimo) &&
            (precioMaximo == null || precio <= precioMaximo)
    }

    private fun coincideStock(item: Item): Boolean {
        if (estadosStock.isEmpty()) return true
        val grupo = when (item.estadoStock()) {
            EstadoStock.DISPONIBLE, EstadoStock.SOBRE_MAXIMO -> FiltroStock.DISPONIBLE
            EstadoStock.CERCA_MINIMO, EstadoStock.BAJO_MINIMO -> FiltroStock.STOCK_BAJO
            EstadoStock.AGOTADO -> FiltroStock.AGOTADO
            EstadoStock.SIN_DATO -> null
        }
        return grupo in estadosStock
    }
}

fun String.normalizada(): String =
    Normalizer.normalize(trim().lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
