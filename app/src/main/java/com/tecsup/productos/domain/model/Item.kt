package com.tecsup.productos.domain.model

data class Item(
    val id: Int,
    val titulo: String,
    val marca: String,
    val precio: Double,
    val imagenUrl: String?,
    val esFavorito: Boolean = false,
    val descripcion: String? = null,
    val rating: Double? = null,
    val stock: Int? = null,
    val descuento: Double? = null,
    val imagenes: List<String> = emptyList(),
    val categoriaId: Long? = null,
    val categoria: String? = null,
    val stockMinimo: Int = STOCK_MINIMO_PREDETERMINADO,
    val stockMaximo: Int = STOCK_MAXIMO_PREDETERMINADO,
    val unidadesPorPedido: Int = UNIDADES_POR_PEDIDO_PREDETERMINADAS,
    val editadoLocalmente: Boolean = false,
    val tipo: TipoProducto = TipoProducto.SMARTPHONE
)

const val STOCK_MINIMO_PREDETERMINADO = 10
const val STOCK_MAXIMO_PREDETERMINADO = 80
const val UNIDADES_POR_PEDIDO_PREDETERMINADAS = 1
