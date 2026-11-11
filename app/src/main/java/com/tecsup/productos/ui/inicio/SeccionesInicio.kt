package com.tecsup.productos.ui.inicio

import com.tecsup.productos.domain.model.Categoria
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.TipoProducto
import com.tecsup.productos.domain.model.precioFinal

sealed interface BannerInicio {
    data class Oferta(val producto: Item) : BannerInicio
    data class Tipo(
        val tipo: TipoProducto,
        val imagenUrl: String?,
        val precioDesde: Double,
        val cantidad: Int
    ) : BannerInicio
}

data class SeccionesInicio(
    val banners: List<BannerInicio>,
    val ofertas: List<Item>,
    val nuevos: List<Item>,
    val mejorValorados: List<Item>,
    val marcas: List<Categoria>,
    val cantidadPorTipo: Map<TipoProducto, Int>
) {
    val estaVacio: Boolean get() = banners.isEmpty() && ofertas.isEmpty() && mejorValorados.isEmpty()
}

fun construirInicio(items: List<Item>, categorias: List<Categoria>): SeccionesInicio {
    val disponibles = items.filter { (it.stock ?: 0) > 0 }
    val nuevos = disponibles
        .filter { it.tipo != TipoProducto.SMARTPHONE }
        .sortedByDescending { it.descuento ?: 0.0 }
        .take(MAXIMO_POR_FILA)
    val destacado = nuevos.maxByOrNull { it.descuento ?: 0.0 }
        ?: disponibles.maxByOrNull { it.descuento ?: 0.0 }
    val bannersTipo = ORDEN_BANNERS.mapNotNull { tipo ->
        val delTipo = disponibles.filter { it.tipo == tipo }
        if (delTipo.isEmpty()) {
            null
        } else {
            BannerInicio.Tipo(
                tipo = tipo,
                imagenUrl = delTipo.maxByOrNull { it.rating ?: 0.0 }?.imagenUrl,
                precioDesde = delTipo.minOf { it.precioFinal() },
                cantidad = delTipo.size
            )
        }
    }
    return SeccionesInicio(
        banners = listOfNotNull(destacado?.let { BannerInicio.Oferta(it) }) + bannersTipo,
        ofertas = disponibles
            .filter { (it.descuento ?: 0.0) >= DESCUENTO_MINIMO_OFERTA && it.id != destacado?.id }
            .sortedByDescending { it.descuento ?: 0.0 }
            .take(MAXIMO_POR_FILA),
        nuevos = nuevos,
        mejorValorados = disponibles
            .sortedByDescending { it.rating ?: 0.0 }
            .take(MAXIMO_POR_FILA),
        marcas = categorias.filter { it.cantidadProductos > 0 },
        cantidadPorTipo = items.groupingBy { it.tipo }.eachCount()
    )
}

private val ORDEN_BANNERS = listOf(
    TipoProducto.TABLET,
    TipoProducto.RELOJ,
    TipoProducto.ACCESORIO,
    TipoProducto.SMARTPHONE
)
private const val MAXIMO_POR_FILA = 8
private const val DESCUENTO_MINIMO_OFERTA = 5.0
